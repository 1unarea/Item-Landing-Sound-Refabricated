package org.refabricated.itemlandingsound.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.refabricated.itemlandingsound.duck.ItemLandingTracker;
import org.refabricated.itemlandingsound.sound.ItemLandingSoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin injecting landing state tracking into {@link ItemEntity}.
 * Detects instantaneous ground impact without waiting for sliding to finish.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements ItemLandingTracker {

    @Unique
    private boolean ils$wasAirborne = false;

    @Unique
    private double ils$prevVerticalVelocity = 0.0;

    @Unique
    private int ils$landingCooldown = 0;

    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean ils$wasAirborne() {
        return this.ils$wasAirborne;
    }

    @Override
    public void ils$setWasAirborne(boolean wasAirborne) {
        this.ils$wasAirborne = wasAirborne;
    }

    @Override
    public double ils$getPrevVerticalVelocity() {
        return this.ils$prevVerticalVelocity;
    }

    @Override
    public void ils$setPrevVerticalVelocity(double velocity) {
        this.ils$prevVerticalVelocity = velocity;
    }

    @Override
    public int ils$getLandingCooldown() {
        return this.ils$landingCooldown;
    }

    @Override
    public void ils$setLandingCooldown(int cooldown) {
        this.ils$landingCooldown = cooldown;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ils$beforeTick(CallbackInfo ci) {
        if (!this.level().isClientSide() || this.isRemoved()) {
            return;
        }

        if (this.ils$landingCooldown > 0) {
            this.ils$landingCooldown--;
        }

        // Capture airborne status and downward vertical velocity before movement calculation
        this.ils$prevVerticalVelocity = this.getDeltaMovement().y;
        this.ils$wasAirborne = !this.onGround();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ils$afterTick(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            return;
        }

        if (this.isRemoved()) {
            return;
        }

        // Instantaneous impact condition:
        // Entity was airborne prior to movement, is on ground after collision resolution,
        // downward impact velocity satisfies threshold (< -0.04), and cooldown cleared.
        if (this.ils$wasAirborne && this.onGround() && this.ils$landingCooldown == 0) {
            if (this.ils$prevVerticalVelocity < -0.04) {
                ItemLandingSoundManager.playLandingSound((ItemEntity) (Object) this, this.ils$prevVerticalVelocity);
                this.ils$landingCooldown = 4; // 4 ticks debounce to absorb micro-bounces
            }
        }
    }
}
