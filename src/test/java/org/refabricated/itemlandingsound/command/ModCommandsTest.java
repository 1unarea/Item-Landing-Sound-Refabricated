package org.refabricated.itemlandingsound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.refabricated.itemlandingsound.config.ConfigManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Commands & Syntax Verification")
public class ModCommandsTest {

    @TempDir
    Path tempGameDir;

    private CommandDispatcher<FabricClientCommandSource> dispatcher;
    private FabricClientCommandSource dummySource;
    private List<Component> capturedFeedback;

    @BeforeEach
    void setUp() throws Exception {
        ConfigManager.setConfigDirForTesting(tempGameDir.resolve("config"));
        ConfigManager.load();

        dispatcher = new CommandDispatcher<>();
        Method registerMethod = ModCommands.class.getDeclaredMethod("registerCommands", CommandDispatcher.class, CommandBuildContext.class);
        registerMethod.setAccessible(true);
        registerMethod.invoke(null, dispatcher, null);

        capturedFeedback = new ArrayList<>();
        InvocationHandler handler = (proxy, method, args) -> {
            if ("sendFeedback".equals(method.getName()) && args != null && args.length > 0 && args[0] instanceof Component component) {
                capturedFeedback.add(component);
                return null;
            }
            if ("sendError".equals(method.getName()) && args != null && args.length > 0 && args[0] instanceof Component component) {
                capturedFeedback.add(component);
                return null;
            }
            if ("toString".equals(method.getName())) {
                return "MockFabricClientCommandSource";
            }
            return null;
        };

        dummySource = (FabricClientCommandSource) Proxy.newProxyInstance(
                FabricClientCommandSource.class.getClassLoader(),
                new Class<?>[]{FabricClientCommandSource.class},
                handler
        );
    }

    @Test
    @DisplayName("Verify /itemlandingsound:volume is registered in dispatcher")
    void testRegistrationTree() {
        CommandNode<FabricClientCommandSource> exactCommand = dispatcher.getRoot().getChild("itemlandingsound:volume");
        assertNotNull(exactCommand, "Exact command node 'itemlandingsound:volume' must be registered");
        assertNotNull(exactCommand.getChild("value"), "Argument 'value' must exist as child of 'itemlandingsound:volume'");

        CommandNode<FabricClientCommandSource> friendlyCommand = dispatcher.getRoot().getChild("itemlandingsound");
        assertNotNull(friendlyCommand, "Friendly root command 'itemlandingsound' must be registered");
        assertNotNull(friendlyCommand.getChild("volume"));
        assertNotNull(friendlyCommand.getChild("toggle"));
        assertNotNull(friendlyCommand.getChild("reload"));
    }

    @Test
    @DisplayName("Verify command syntax for /itemlandingsound:volume with valid values")
    void testValidVolumeSyntaxParsing() {
        String[] validInputs = {
                "itemlandingsound:volume",
                "itemlandingsound:volume 0.0",
                "itemlandingsound:volume 0.5",
                "itemlandingsound:volume 1.0",
                "itemlandingsound:volume 1.75",
                "itemlandingsound:volume 2.0"
        };

        for (String input : validInputs) {
            ParseResults<FabricClientCommandSource> parse = dispatcher.parse(input, dummySource);
            assertTrue(parse.getExceptions().isEmpty(), "Command '" + input + "' should parse with zero exceptions: " + parse.getExceptions());
            assertFalse(parse.getReader().canRead(), "Command '" + input + "' should be completely consumed");
        }
    }

    @Test
    @DisplayName("Verify command syntax rejects out-of-range volume values (< 0.0 or > 2.0)")
    void testOutOfRangeVolumeRejection() {
        // Value below minimum (0.0f)
        ParseResults<FabricClientCommandSource> parseUnderflow = dispatcher.parse("itemlandingsound:volume -0.1", dummySource);
        assertFalse(parseUnderflow.getExceptions().isEmpty(), "Negative volume -0.1 must be rejected by Brigadier");

        // Value above maximum (2.0f)
        ParseResults<FabricClientCommandSource> parseOverflow = dispatcher.parse("itemlandingsound:volume 2.05", dummySource);
        assertFalse(parseOverflow.getExceptions().isEmpty(), "Volume 2.05 > 2.0 must be rejected by Brigadier");

        // Non-numeric argument
        ParseResults<FabricClientCommandSource> parseNaN = dispatcher.parse("itemlandingsound:volume invalid", dummySource);
        assertFalse(parseNaN.getExceptions().isEmpty(), "Non-numeric string must be rejected by Brigadier");
    }

    @Test
    @DisplayName("Execution of /itemlandingsound:volume [value] modifies config and disk")
    void testExecutionSetVolume() throws Exception {
        int result = dispatcher.execute("itemlandingsound:volume 1.65", dummySource);
        assertEquals(1, result, "Command execution must return 1");

        // Runtime config update
        assertEquals(1.65f, ConfigManager.getConfig().volume, 1e-6f);

        // Feedback message sent
        assertFalse(capturedFeedback.isEmpty(), "Feedback must be sent to the user");
        String messageText = capturedFeedback.get(0).getString();
        assertTrue(messageText.contains("1.65"), "Feedback should mention updated volume: " + messageText);
    }

    @Test
    @DisplayName("Execution of /itemlandingsound:volume queries current volume")
    void testExecutionGetVolume() throws Exception {
        ConfigManager.setVolume(0.85f);
        capturedFeedback.clear();

        int result = dispatcher.execute("itemlandingsound:volume", dummySource);
        assertEquals(1, result);

        assertFalse(capturedFeedback.isEmpty());
        String messageText = capturedFeedback.get(0).getString();
        assertTrue(messageText.contains("0.85"), "Feedback should report current volume: " + messageText);
    }

    @Test
    @DisplayName("Execution of /itemlandingsound toggle toggles enabled state and persists")
    void testExecutionToggle() throws Exception {
        assertTrue(ConfigManager.getConfig().enabled);

        dispatcher.execute("itemlandingsound toggle", dummySource);
        assertFalse(ConfigManager.getConfig().enabled, "First toggle must disable mod");

        dispatcher.execute("itemlandingsound toggle", dummySource);
        assertTrue(ConfigManager.getConfig().enabled, "Second toggle must enable mod");
    }

    @Test
    @DisplayName("Execution of /itemlandingsound reload flushes and reloads config from disk")
    void testExecutionReload() throws Exception {
        int result = dispatcher.execute("itemlandingsound reload", dummySource);
        assertEquals(1, result);
        assertFalse(capturedFeedback.isEmpty());
        assertTrue(capturedFeedback.get(capturedFeedback.size() - 1).getString().contains("reloaded"));
    }

    @Test
    @DisplayName("Execution of /itemlandingsound displays status overview")
    void testExecutionStatus() throws Exception {
        int result = dispatcher.execute("itemlandingsound", dummySource);
        assertEquals(1, result);
        assertTrue(capturedFeedback.size() >= 4, "Status command should print multi-line overview");
    }

    @AfterEach
    void tearDown() {
        ConfigManager.resetConfigDirForTesting();
    }
}
