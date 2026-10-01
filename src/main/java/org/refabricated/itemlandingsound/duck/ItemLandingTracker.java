package org.refabricated.itemlandingsound.duck;

/**
 * Duck interface injected into {@link net.minecraft.world.entity.item.ItemEntity}
 * to track landing state ephemerally without memory leaks.
 */
public interface ItemLandingTracker {

    /**
     * @return true if the item entity was in the air prior to the current tick's movement.
     */
    boolean ils$wasAirborne();

    /**
     * Sets whether the item was airborne.
     *
     * @param wasAirborne whether the item was airborne
     */
    void ils$setWasAirborne(boolean wasAirborne);

    /**
     * @return the vertical velocity (vy) before collision resolution.
     */
    double ils$getPrevVerticalVelocity();

    /**
     * Sets the vertical velocity (vy) before collision resolution.
     *
     * @param velocity the vertical velocity
     */
    void ils$setPrevVerticalVelocity(double velocity);

    /**
     * @return the debounce cooldown in ticks.
     */
    int ils$getLandingCooldown();

    /**
     * Sets the debounce cooldown in ticks.
     *
     * @param cooldown cooldown ticks
     */
    void ils$setLandingCooldown(int cooldown);
}
