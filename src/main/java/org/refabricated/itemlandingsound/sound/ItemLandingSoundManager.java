package org.refabricated.itemlandingsound.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.refabricated.itemlandingsound.config.ConfigManager;
import org.refabricated.itemlandingsound.config.ModConfig;

/**
 * Manages surface block detection and plays landing sound effects.
 */
public final class ItemLandingSoundManager {

    private ItemLandingSoundManager() {}

    /**
     * Resolves the primary sound-producing block position, accounting for inside-step blocks
     * like carpets, moss, and snow layers resting on top of supporting blocks.
     *
     * @param level the world level
     * @param landingPos the supporting block position
     * @return the resolved sound-producing block position
     */
    public static BlockPos getPrimaryStepSoundBlockPos(Level level, BlockPos landingPos) {
        BlockPos abovePos = landingPos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) || aboveState.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
            return abovePos;
        }
        return landingPos;
    }

    /**
     * Plays the appropriate landing sound based on the block beneath the item.
     *
     * @param item the item entity that landed
     * @param impactVelocity the vertical downward velocity at impact (negative value)
     */
    public static void playLandingSound(ItemEntity item, double impactVelocity) {
        if (item == null || item.isRemoved()) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        if (!config.enabled || config.volume <= 0.0f) {
            return;
        }

        Level level = item.level();
        // Suppress landing sounds if the item is submerged in fluids
        if (item.isInWater() || item.isInLava()) {
            return;
        }

        double absVy = Math.abs(impactVelocity);
        if (absVy < config.minVelocity) {
            return;
        }

        // Determine landing block position using collision supporting pos and step sound block pos
        BlockPos landingPos = item.getOnPos();
        BlockPos soundPos = getPrimaryStepSoundBlockPos(level, landingPos);
        BlockState state = level.getBlockState(soundPos);

        if (state.isAir()) {
            state = level.getBlockState(landingPos);
        }
        if (state.isAir()) {
            state = level.getBlockState(item.getBlockPosBelowThatAffectsMyMovement());
        }
        if (state.isAir()) {
            return;
        }

        SoundType soundType = state.getSoundType();
        if (soundType == null) {
            return;
        }

        SoundEvent sound = soundType.getFallSound();
        if (sound == null) {
            sound = soundType.getStepSound();
        }
        if (sound == null) {
            return;
        }

        // Dynamic volume scaling based on impact speed
        float speedFactor = Math.min(1.0f, (float) (absVy * 1.8));
        float finalVolume = soundType.getVolume() * config.volume * speedFactor;

        // Subtle organic pitch variation (+- pitchVariation)
        float variation = (item.getRandom().nextFloat() * 2.0f - 1.0f) * config.pitchVariation;
        float finalPitch = soundType.getPitch() * config.pitch * (1.0f + variation);

        // Play on SoundSource.BLOCKS channel using client-side local sound dispatch
        level.playLocalSound(
                item.getX(), item.getY(), item.getZ(),
                sound,
                SoundSource.BLOCKS,
                Math.max(0.01f, finalVolume),
                Math.clamp(finalPitch, 0.1f, 2.0f),
                false
        );
    }
}
