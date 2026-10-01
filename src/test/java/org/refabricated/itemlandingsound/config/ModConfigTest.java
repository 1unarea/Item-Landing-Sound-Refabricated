package org.refabricated.itemlandingsound.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModConfig Model Verification")
public class ModConfigTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    @DisplayName("Verify default configuration values")
    void testDefaultValues() {
        ModConfig config = new ModConfig();
        assertTrue(config.enabled, "Mod should be enabled by default");
        assertEquals(1.0f, config.volume, 1e-6f, "Default volume must be 1.0");
        assertEquals(1.0f, config.pitch, 1e-6f, "Default pitch must be 1.0");
        assertEquals(0.1f, config.pitchVariation, 1e-6f, "Default pitch variation must be 0.1");
        assertEquals(0.04, config.minVelocity, 1e-6, "Default min velocity must be 0.04");
    }

    @Test
    @DisplayName("Volume below 0.0 clamps to 0.0")
    void testVolumeClampsToLowerBound() {
        float[] underflowValues = {-100.0f, -1.0f, -0.001f, -0.0f};
        for (float val : underflowValues) {
            ModConfig config = new ModConfig();
            config.volume = val;
            config.validate();
            assertEquals(0.0f, config.volume, 1e-6f, "Volume " + val + " < 0.0 must clamp to 0.0");
        }
    }

    @Test
    @DisplayName("Volume above 2.0 clamps to 2.0")
    void testVolumeClampsToUpperBound() {
        float[] overflowValues = {2.0001f, 2.5f, 10.0f, 1000.0f};
        for (float val : overflowValues) {
            ModConfig config = new ModConfig();
            config.volume = val;
            config.validate();
            assertEquals(2.0f, config.volume, 1e-6f, "Volume " + val + " > 2.0 must clamp to 2.0");
        }
    }

    @Test
    @DisplayName("Valid volume values within [0.0, 2.0] remain unchanged")
    void testValidVolumeUnchanged() {
        float[] validValues = {0.0f, 0.25f, 0.5f, 1.0f, 1.5f, 1.999f, 2.0f};
        for (float val : validValues) {
            ModConfig config = new ModConfig();
            config.volume = val;
            config.validate();
            assertEquals(val, config.volume, 1e-6f, "Volume " + val + " within [0.0, 2.0] must remain unchanged");
        }
    }

    @Test
    @DisplayName("Volume NaN and Infinities are reset to safe default 1.0")
    void testVolumeNaNAndInfinities() {
        ModConfig nanConfig = new ModConfig();
        nanConfig.volume = Float.NaN;
        nanConfig.validate();
        assertEquals(1.0f, nanConfig.volume, 1e-6f, "NaN volume must be reset to 1.0");

        ModConfig posInfConfig = new ModConfig();
        posInfConfig.volume = Float.POSITIVE_INFINITY;
        posInfConfig.validate();
        assertEquals(1.0f, posInfConfig.volume, 1e-6f, "+Infinity volume must be reset to 1.0");

        ModConfig negInfConfig = new ModConfig();
        negInfConfig.volume = Float.NEGATIVE_INFINITY;
        negInfConfig.validate();
        assertEquals(1.0f, negInfConfig.volume, 1e-6f, "-Infinity volume must be reset to 1.0");
    }

    @Test
    @DisplayName("Pitch bounds [0.1, 2.0] and NaN/Infinity recovery")
    void testPitchValidation() {
        ModConfig underPitch = new ModConfig();
        underPitch.pitch = 0.05f;
        underPitch.validate();
        assertEquals(0.1f, underPitch.pitch, 1e-6f, "Pitch below 0.1 must clamp to 0.1");

        ModConfig overPitch = new ModConfig();
        overPitch.pitch = 2.5f;
        overPitch.validate();
        assertEquals(2.0f, overPitch.pitch, 1e-6f, "Pitch above 2.0 must clamp to 2.0");

        ModConfig nanPitch = new ModConfig();
        nanPitch.pitch = Float.NaN;
        nanPitch.validate();
        assertEquals(1.0f, nanPitch.pitch, 1e-6f, "NaN pitch must reset to 1.0");

        ModConfig infPitch = new ModConfig();
        infPitch.pitch = Float.POSITIVE_INFINITY;
        infPitch.validate();
        assertEquals(1.0f, infPitch.pitch, 1e-6f, "Infinite pitch must reset to 1.0");
    }

    @Test
    @DisplayName("PitchVariation bounds [0.0, 0.5] and NaN/Infinity recovery")
    void testPitchVariationValidation() {
        ModConfig underVar = new ModConfig();
        underVar.pitchVariation = -0.1f;
        underVar.validate();
        assertEquals(0.0f, underVar.pitchVariation, 1e-6f);

        ModConfig overVar = new ModConfig();
        overVar.pitchVariation = 0.9f;
        overVar.validate();
        assertEquals(0.5f, overVar.pitchVariation, 1e-6f);

        ModConfig nanVar = new ModConfig();
        nanVar.pitchVariation = Float.NaN;
        nanVar.validate();
        assertEquals(0.1f, nanVar.pitchVariation, 1e-6f);
    }

    @Test
    @DisplayName("MinVelocity bounds [0.0, 1.0] and NaN/Infinity recovery")
    void testMinVelocityValidation() {
        ModConfig underVel = new ModConfig();
        underVel.minVelocity = -0.5;
        underVel.validate();
        assertEquals(0.0, underVel.minVelocity, 1e-6);

        ModConfig overVel = new ModConfig();
        overVel.minVelocity = 2.5;
        overVel.validate();
        assertEquals(1.0, overVel.minVelocity, 1e-6);

        ModConfig nanVel = new ModConfig();
        nanVel.minVelocity = Double.NaN;
        nanVel.validate();
        assertEquals(0.04, nanVel.minVelocity, 1e-6);
    }

    @Test
    @DisplayName("JSON serialization and deserialization cycle retains all values")
    void testJsonSerializationCycle() {
        ModConfig original = new ModConfig();
        original.enabled = false;
        original.volume = 1.75f;
        original.pitch = 0.85f;
        original.pitchVariation = 0.2f;
        original.minVelocity = 0.08;

        String json = gson.toJson(original);
        assertNotNull(json);

        ModConfig deserialized = gson.fromJson(json, ModConfig.class);
        assertNotNull(deserialized);
        deserialized.validate();

        assertEquals(original.enabled, deserialized.enabled);
        assertEquals(original.volume, deserialized.volume, 1e-6f);
        assertEquals(original.pitch, deserialized.pitch, 1e-6f);
        assertEquals(original.pitchVariation, deserialized.pitchVariation, 1e-6f);
        assertEquals(original.minVelocity, deserialized.minVelocity, 1e-6);
    }

    @Test
    @DisplayName("Deserialization with missing fields maintains field default values")
    void testMissingFieldsRetainDefaults() {
        String partialJson = "{\n  \"volume\": 1.4\n}";
        ModConfig deserialized = gson.fromJson(partialJson, ModConfig.class);
        assertNotNull(deserialized);
        deserialized.validate();

        assertEquals(1.4f, deserialized.volume, 1e-6f);
        assertTrue(deserialized.enabled, "Missing enabled field should keep default true");
        assertEquals(1.0f, deserialized.pitch, 1e-6f, "Missing pitch field should keep default 1.0");
        assertEquals(0.1f, deserialized.pitchVariation, 1e-6f, "Missing pitchVariation should keep default 0.1");
        assertEquals(0.04, deserialized.minVelocity, 1e-6, "Missing minVelocity should keep default 0.04");
    }

    @Test
    @DisplayName("Deserialization ignores unrecognized JSON properties")
    void testUnknownPropertiesIgnored() {
        String jsonWithExtra = "{\n  \"volume\": 1.2,\n  \"unrecognizedFeatureFlag\": true,\n  \"legacyTag\": 99\n}";
        ModConfig deserialized = gson.fromJson(jsonWithExtra, ModConfig.class);
        assertNotNull(deserialized);
        deserialized.validate();

        assertEquals(1.2f, deserialized.volume, 1e-6f);
        assertTrue(deserialized.enabled);
    }
}
