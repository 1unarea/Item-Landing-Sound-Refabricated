package org.refabricated.itemlandingsound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import org.refabricated.itemlandingsound.config.ConfigManager;
import org.refabricated.itemlandingsound.config.ModConfig;

/**
 * Registers client-side commands for real-time sound settings management.
 */
public final class ModCommands {

    private ModCommands() {}

    /**
     * Registers client commands with the Fabric client command dispatcher.
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                         CommandBuildContext context) {
        // Required exact command: /itemlandingsound:volume [value]
        dispatcher.register(
                ClientCommands.literal("itemlandingsound:volume")
                        .executes(ModCommands::executeGetVolume)
                        .then(ClientCommands.argument("value", FloatArgumentType.floatArg(0.0f, 2.0f))
                                .executes(ModCommands::executeSetVolume)
                        )
        );

        // Friendly root command tree: /itemlandingsound
        dispatcher.register(
                ClientCommands.literal("itemlandingsound")
                        .executes(ModCommands::executeStatus)
                        .then(ClientCommands.literal("volume")
                                .executes(ModCommands::executeGetVolume)
                                .then(ClientCommands.argument("value", FloatArgumentType.floatArg(0.0f, 2.0f))
                                        .executes(ModCommands::executeSetVolume)
                                )
                        )
                        .then(ClientCommands.literal("toggle")
                                .executes(ModCommands::executeToggle)
                        )
                        .then(ClientCommands.literal("reload")
                                .executes(ModCommands::executeReload)
                        )
        );
    }

    private static int executeGetVolume(CommandContext<FabricClientCommandSource> context) {
        float volume = ConfigManager.getConfig().volume;
        context.getSource().sendFeedback(
                Component.literal("[Item Landing Sound] Current volume: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f", volume)).withStyle(ChatFormatting.AQUA))
        );
        return 1;
    }

    private static int executeSetVolume(CommandContext<FabricClientCommandSource> context) {
        float newVolume = FloatArgumentType.getFloat(context, "value");
        ConfigManager.setVolume(newVolume);

        context.getSource().sendFeedback(
                Component.literal("[Item Landing Sound] Volume updated to: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f", newVolume)).withStyle(ChatFormatting.GREEN))
        );
        return 1;
    }

    private static int executeToggle(CommandContext<FabricClientCommandSource> context) {
        boolean enabled = ConfigManager.toggleEnabled();

        ChatFormatting color = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        String status = enabled ? "ENABLED" : "DISABLED";
        context.getSource().sendFeedback(
                Component.literal("[Item Landing Sound] Mod sound playback is now: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(status).withStyle(color))
        );
        return 1;
    }

    private static int executeReload(CommandContext<FabricClientCommandSource> context) {
        ConfigManager.load();
        context.getSource().sendFeedback(
                Component.literal("[Item Landing Sound] Configuration reloaded from disk.")
                        .withStyle(ChatFormatting.YELLOW)
        );
        return 1;
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ConfigManager.getConfig();
        context.getSource().sendFeedback(
                Component.literal("--- Item Landing Sound Refabricated ---").withStyle(ChatFormatting.GOLD)
        );
        context.getSource().sendFeedback(
                Component.literal(" Status: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(config.enabled ? "ENABLED" : "DISABLED")
                                .withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
        );
        context.getSource().sendFeedback(
                Component.literal(" Volume: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f", config.volume)).withStyle(ChatFormatting.AQUA))
        );
        context.getSource().sendFeedback(
                Component.literal(" Pitch: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f", config.pitch)).withStyle(ChatFormatting.AQUA))
        );
        context.getSource().sendFeedback(
                Component.literal(" Commands: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("/itemlandingsound:volume <0.0-2.0>").withStyle(ChatFormatting.YELLOW))
        );
        return 1;
    }
}
