package org.refabricated.itemlandingsound.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Thread-safe configuration manager providing atomic persistence and error recovery.
 */
public final class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLandingSound/Config");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Object LOCK = new Object();
    private static final String CONFIG_FILE_NAME = "item_landing_sound_refabricated.json";
    private static ModConfig instance = new ModConfig();

    /**
     * Optional custom config directory used exclusively for testing without bootstrapping FabricLoader.
     * When non-null, {@link #getConfigPath()} resolves against this directory.
     */
    private static volatile Path customConfigDir = null;

    private ConfigManager() {}

    /**
     * Sets a custom configuration directory override for testing environments.
     *
     * @param dir the directory path to use for loading and saving configs in tests.
     */
    public static void setConfigDirForTesting(Path dir) {
        synchronized (LOCK) {
            customConfigDir = dir;
        }
    }

    /**
     * Resets the configuration directory override, restoring production resolution via FabricLoader.
     */
    public static void resetConfigDirForTesting() {
        synchronized (LOCK) {
            customConfigDir = null;
        }
    }

    /**
     * @return the active configuration instance.
     */
    public static ModConfig getConfig() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new ModConfig();
            }
            return instance;
        }
    }

    /**
     * @return the resolved file system path for the configuration file.
     */
    public static Path getConfigPath() {
        Path custom = customConfigDir;
        if (custom != null) {
            return custom.resolve(CONFIG_FILE_NAME);
        }
        try {
            return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        } catch (Throwable t) {
            return Path.of("config").resolve(CONFIG_FILE_NAME);
        }
    }

    /**
     * Loads the configuration from disk, creating default if absent or resetting if corrupted.
     */
    public static void load() {
        synchronized (LOCK) {
            Path path = getConfigPath();
            if (!Files.exists(path)) {
                LOGGER.info("Configuration file not found, creating defaults: {}", path);
                instance = new ModConfig();
                save();
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded == null) {
                    throw new JsonParseException("Config JSON parsed to null");
                }
                loaded.validate();
                instance = loaded;
                LOGGER.info("Configuration loaded successfully (volume={}, enabled={})", instance.volume, instance.enabled);
            } catch (JsonParseException e) {
                LOGGER.warn("Corrupted configuration file detected at {}. Resetting to defaults. Reason: {}", path, e.getMessage());
                backupCorrupted(path);
                instance = new ModConfig();
                save();
            } catch (IOException e) {
                LOGGER.error("Failed to read configuration file from {}: {}", path, e.getMessage(), e);
            }
        }
    }

    /**
     * Atomically saves the current configuration to disk.
     */
    public static void save() {
        synchronized (LOCK) {
            Path path = getConfigPath();
            Path tempPath = path.resolveSibling(CONFIG_FILE_NAME + ".tmp");

            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }

                if (instance == null) {
                    instance = new ModConfig();
                }
                instance.validate();

                try (BufferedWriter writer = Files.newBufferedWriter(
                        tempPath,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {
                    GSON.toJson(instance, writer);
                }

                try {
                    Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
                }
                LOGGER.info("Configuration saved successfully.");
            } catch (IOException e) {
                LOGGER.error("Failed to write configuration file to {}: {}", path, e.getMessage(), e);
            }
        }
    }

    /**
     * Updates the landing sound volume, clamps it within range [0.0, 2.0], and persists to disk.
     *
     * @param volume the new volume multiplier
     */
    public static void setVolume(float volume) {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new ModConfig();
            }
            instance.volume = Math.clamp(volume, 0.0f, 2.0f);
            save();
        }
    }

    /**
     * Atomically toggles the mod's enabled state and persists the change to disk.
     *
     * @return the new enabled state after toggling.
     */
    public static boolean toggleEnabled() {
        synchronized (LOCK) {
            if (instance == null) {
                load();
            }
            if (instance == null) {
                instance = new ModConfig();
            }
            instance.enabled = !instance.enabled;
            save();
            return instance.enabled;
        }
    }

    /**
     * Updates the mod's enabled state and persists the change to disk.
     *
     * @param enabled the new enabled state
     */
    public static void setEnabled(boolean enabled) {
        synchronized (LOCK) {
            if (instance == null) {
                load();
            }
            if (instance == null) {
                instance = new ModConfig();
            }
            instance.enabled = enabled;
            save();
        }
    }

    private static void backupCorrupted(Path path) {
        try {
            Path backup = path.resolveSibling(CONFIG_FILE_NAME + ".corrupted." + System.currentTimeMillis());
            Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backed up corrupted config file to {}", backup);
        } catch (IOException ex) {
            LOGGER.error("Failed to create backup for corrupted config: {}", ex.getMessage());
        }
    }
}
