package fr.craftaria.utils;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.cooking.CookingQueue;
import fr.craftaria.utils.screen.CraftariaConfigScreen;
import fr.craftaria.utils.screen.CookingScreen;
import fr.craftaria.utils.screen.LoginConfirmScreen;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class CraftariaUtilsClient implements ClientModInitializer {
    public static KeyBinding fourKey, loginKey, cookingKey;
    private static int previousDamage = -1;
    private static String previousItem = "";
    private static boolean repairSent;

    @Override public void onInitializeClient() {
        ModConfig.load();
        ConnectionTracker.initialize();
        WallpaperManager.initializeDirectory();
        fourKey = register("key.craftaria_utils.four");
        loginKey = register("key.craftaria_utils.login");
        cookingKey = register("key.craftaria_utils.cooking");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (fourKey.wasPressed()) sendCommand(client, "four");
            while (loginKey.wasPressed()) {
                if (!ModConfig.get().password.isBlank()) client.setScreen(new LoginConfirmScreen(client.currentScreen));
                else client.inGameHud.getChatHud().addMessage(Text.translatable("craftaria_utils.login.no_password"));
            }
            while (cookingKey.wasPressed()) client.setScreen(new CookingScreen(client.currentScreen));
            CookingQueue.tick(client);
            checkRepair(client);
        });
    }
    private static KeyBinding register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.craftaria_utils"));
    }
    public static void sendCommand(MinecraftClient client, String command) {
        if (client.player != null && client.getNetworkHandler() != null) client.getNetworkHandler().sendChatCommand(command);
    }
    private static void checkRepair(MinecraftClient client) {
        if (!ModConfig.get().autoRepair || client.player == null || client.getNetworkHandler() == null) return;
        var stack = client.player.getMainHandStack();
        if (!stack.isDamageable() || stack.isEmpty()) { previousDamage = -1; previousItem = ""; repairSent = false; return; }
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        int damage = stack.getDamage();
        if (!itemId.equals(previousItem) || damage < previousDamage) repairSent = false;
        int remainingPercent = (stack.getMaxDamage() - damage) * 100 / stack.getMaxDamage();
        if (!repairSent && damage > 0 && remainingPercent <= ModConfig.get().repairThresholdPercent) {
            sendCommand(client, "repair");
            repairSent = true;
        }
        previousDamage = damage; previousItem = itemId;
    }
}
