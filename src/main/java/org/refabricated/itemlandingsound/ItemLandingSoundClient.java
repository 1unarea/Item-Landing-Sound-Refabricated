package org.refabricated.itemlandingsound;

import net.fabricmc.api.ClientModInitializer;
import org.refabricated.itemlandingsound.command.ModCommands;
import org.refabricated.itemlandingsound.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for Item Landing Sound Refabricated.
 */
public class ItemLandingSoundClient implements ClientModInitializer {

    public static final String MOD_ID = "item_landing_sound_refabricated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Item Landing Sound Refabricated for Minecraft 26.3...");

        // Load configuration from disk
        ConfigManager.load();

        // Register client commands
        ModCommands.register();

        LOGGER.info("Item Landing Sound Refabricated initialized successfully.");
    }
}
