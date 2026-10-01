package org.refabricated.itemlandingsound.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigManager Persistence & Error Recovery Verification")
public class ConfigManagerTest {

    @TempDir
    Path tempGameDir;

    private Path configDir;
    private Path configFile;

    @BeforeEach
    void setUp() {
        configDir = tempGameDir.resolve("config");
        configFile = configDir.resolve("item_landing_sound_refabricated.json");
        ConfigManager.setConfigDirForTesting(configDir);
    }

    @AfterEach
    void tearDown() {
        ConfigManager.resetConfigDirForTesting();
    }

    @Test
    @DisplayName("Config path resolves correctly under Fabric config directory")
    void testConfigPathResolution() {
        Path resolved = ConfigManager.getConfigPath();
        assertEquals(configFile, resolved, "ConfigManager.getConfigPath() must resolve within Fabric config dir");
    }

    @Test
    @DisplayName("Load on missing file creates default config file with valid content")
    void testLoadMissingFileCreatesDefault() throws IOException {
        assertFalse(Files.exists(configFile), "Config file should not exist initially");

        ConfigManager.load();

        assertTrue(Files.exists(configFile), "ConfigManager.load() must create config file if missing");
        String content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"volume\": 1.0"), "Default file must contain volume 1.0");
        assertTrue(content.contains("\"pitch\": 1.0"), "Default file must contain pitch 1.0");
        assertTrue(content.contains("\"enabled\": true"), "Default file must contain enabled true");

        ModConfig config = ConfigManager.getConfig();
        assertNotNull(config);
        assertEquals(1.0f, config.volume, 1e-6f);
        assertEquals(1.0f, config.pitch, 1e-6f);
        assertTrue(config.enabled);
    }

    @Test
    @DisplayName("setVolume() modifies active volume, clamps appropriately, and persists to disk")
    void testSetVolumePersistenceAndClamping() throws IOException {
        ConfigManager.load();

        // Standard valid volume
        ConfigManager.setVolume(1.75f);
        assertEquals(1.75f, ConfigManager.getConfig().volume, 1e-6f);
        String content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"volume\": 1.75"), "Config file on disk must reflect 1.75");

        // Clamping negative to 0.0
        ConfigManager.setVolume(-0.8f);
        assertEquals(0.0f, ConfigManager.getConfig().volume, 1e-6f);
        content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"volume\": 0.0"), "Config file on disk must reflect 0.0");

        // Clamping overflow to 2.0
        ConfigManager.setVolume(5.5f);
        assertEquals(2.0f, ConfigManager.getConfig().volume, 1e-6f);
        content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"volume\": 2.0"), "Config file on disk must reflect 2.0");

        // Setting NaN volume resets to safe default 1.0 via validate()
        ConfigManager.setVolume(Float.NaN);
        assertEquals(1.0f, ConfigManager.getConfig().volume, 1e-6f);
        content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"volume\": 1.0"), "Config file on disk must reflect 1.0 after NaN");
    }

    @Test
    @DisplayName("Corrupted JSON file (malformed syntax) is backed up and replaced with clean defaults")
    void testCorruptedJsonMalformedSyntaxRecovery() throws IOException {
        Files.createDirectories(configDir);
        String malformedJson = "{\n  \"volume\": 1.5,\n  \"enabled\": [broken syntax";
        Files.writeString(configFile, malformedJson, StandardCharsets.UTF_8);

        ConfigManager.load();

        // 1. Config returned must be reset to defaults
        ModConfig config = ConfigManager.getConfig();
        assertEquals(1.0f, config.volume, 1e-6f);
        assertTrue(config.enabled);

        // 2. A backup file matching *.corrupted.* must have been created
        List<Path> backups = findCorruptedBackups(configDir);
        assertFalse(backups.isEmpty(), "A .corrupted.<timestamp> backup file must be generated");
        Path backupFile = backups.get(0);
        assertEquals(malformedJson, Files.readString(backupFile, StandardCharsets.UTF_8),
                "Backup file must contain original corrupted content");

        // 3. The main config file must be rewritten with valid JSON
        assertTrue(Files.exists(configFile), "New config file must be saved");
        String restoredContent = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(restoredContent.contains("\"volume\": 1.0"), "Recreated config must have default volume");
    }

    @Test
    @DisplayName("Corrupted JSON file (empty/whitespace) is backed up and replaced with defaults")
    void testCorruptedEmptyFileRecovery() throws IOException {
        Files.createDirectories(configDir);
        Files.writeString(configFile, "   \n\t  ", StandardCharsets.UTF_8);

        ConfigManager.load();

        ModConfig config = ConfigManager.getConfig();
        assertEquals(1.0f, config.volume, 1e-6f);
        assertTrue(config.enabled);

        List<Path> backups = findCorruptedBackups(configDir);
        assertFalse(backups.isEmpty(), "Backup must be created for empty file");
        assertTrue(Files.exists(configFile));
    }

    @Test
    @DisplayName("Corrupted JSON file (incompatible types like object instead of float) recovers safely")
    void testCorruptedTypeMismatchRecovery() throws IOException {
        Files.createDirectories(configDir);
        String invalidTypeJson = "{\n  \"volume\": { \"nested\": \"invalid\" }\n}";
        Files.writeString(configFile, invalidTypeJson, StandardCharsets.UTF_8);

        ConfigManager.load();

        ModConfig config = ConfigManager.getConfig();
        assertEquals(1.0f, config.volume, 1e-6f);

        List<Path> backups = findCorruptedBackups(configDir);
        assertFalse(backups.isEmpty(), "Backup must be created for type-mismatched file");
    }

    @Test
    @DisplayName("Round-trip persistence survives reload cycle")
    void testPersistenceAcrossReloads() {
        ConfigManager.load();
        ConfigManager.getConfig().enabled = false;
        ConfigManager.getConfig().pitch = 0.65f;
        ConfigManager.setVolume(1.35f);

        // Reset memory instance to verify it actually loads from file
        ConfigManager.load();

        ModConfig reloaded = ConfigManager.getConfig();
        assertEquals(1.35f, reloaded.volume, 1e-6f);
        assertEquals(0.65f, reloaded.pitch, 1e-6f);
        assertFalse(reloaded.enabled);
    }

    @Test
    @DisplayName("Concurrent multithreaded setVolume and load operations are thread-safe")
    void testConcurrentAccessThreadSafety() throws InterruptedException {
        ConfigManager.load();
        int threadCount = 10;
        int operationsPerThread = 25;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        float vol = (threadIdx * 10 + j) % 20 / 10.0f; // in [0.0, 1.9]
                        ConfigManager.setVolume(vol);
                        float readVol = ConfigManager.getConfig().volume;
                        assertTrue(readVol >= 0.0f && readVol <= 2.0f);
                    }
                } catch (Throwable t) {
                    failureCount.incrementAndGet();
                    t.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent operations must finish within timeout");
        assertEquals(0, failureCount.get(), "Zero exceptions must occur during concurrent config updates");
        assertTrue(Files.exists(configFile), "Config file must remain intact and valid");
    }

    private List<Path> findCorruptedBackups(Path dir) throws IOException {
        List<Path> backups = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "item_landing_sound_refabricated.json.corrupted.*")) {
            for (Path entry : stream) {
                backups.add(entry);
            }
        }
        return backups;
    }

    @Test
    @DisplayName("toggleEnabled() toggles state, persists to disk, and returns new value")
    void testToggleEnabled() throws IOException {
        ConfigManager.load();
        assertTrue(ConfigManager.getConfig().enabled);

        boolean newState1 = ConfigManager.toggleEnabled();
        assertFalse(newState1);
        assertFalse(ConfigManager.getConfig().enabled);
        String content1 = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content1.contains("\"enabled\": false"));

        boolean newState2 = ConfigManager.toggleEnabled();
        assertTrue(newState2);
        assertTrue(ConfigManager.getConfig().enabled);
        String content2 = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(content2.contains("\"enabled\": true"));
    }

    @Test
    @DisplayName("Test override mechanism sets and resets config directory safely")
    void testConfigDirOverrideMechanism() {
        Path customDir = tempGameDir.resolve("custom_test_dir");
        ConfigManager.setConfigDirForTesting(customDir);
        assertEquals(customDir.resolve("item_landing_sound_refabricated.json"), ConfigManager.getConfigPath());

        ConfigManager.resetConfigDirForTesting();
        assertNotNull(ConfigManager.getConfigPath());
    }
}
