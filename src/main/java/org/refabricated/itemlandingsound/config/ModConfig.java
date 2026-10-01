package org.refabricated.itemlandingsound.config;

import com.google.gson.annotations.Expose;

/**
 * Mod configuration model containing user-configurable audio parameters.
 */
public class ModConfig {

    @Expose
    public boolean enabled = true;

    @Expose
    public float volume = 1.0f;

    @Expose
    public float pitch = 1.0f;

    @Expose
    public float pitchVariation = 0.1f;

    @Expose
    public double minVelocity = 0.04;

    /**
     * Sanitizes and bounds-checks all configuration fields.
     */
    public void validate() {
        if (Float.isNaN(volume) || Float.isInfinite(volume)) {
            volume = 1.0f;
        } else {
            volume = Math.clamp(volume, 0.0f, 2.0f);
        }

        if (Float.isNaN(pitch) || Float.isInfinite(pitch)) {
            pitch = 1.0f;
        } else {
            pitch = Math.clamp(pitch, 0.1f, 2.0f);
        }

        if (Float.isNaN(pitchVariation) || Float.isInfinite(pitchVariation)) {
            pitchVariation = 0.1f;
        } else {
            pitchVariation = Math.clamp(pitchVariation, 0.0f, 0.5f);
        }

        if (Double.isNaN(minVelocity) || Double.isInfinite(minVelocity)) {
            minVelocity = 0.04;
        } else {
            minVelocity = Math.clamp(minVelocity, 0.0, 1.0);
        }
    }
}
