package fr.craftaria.utils;

import fr.craftaria.utils.config.ModConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Watches server join announcements for usernames explicitly listed by the player. */
public final class ConnectionTracker {
    private static final Pattern JOIN = Pattern.compile("(?i)\\b([A-Za-z0-9_]{3,16})\\s+(?:vient de se connect(?:é|e)|s'est connect(?:é|e)(?:e)?|a rejoint (?:le jeu|le serveur)|joined(?: the game| the server)?|has joined the game)\\b");
    private static String playerName;
    private static long expiresAt;

    private ConnectionTracker() { }

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> onServerMessage(message.getString()));
    }

    private static void onServerMessage(String message) {
        if (!ModConfig.get().connectionTrackerEnabled || MinecraftClient.getInstance().player == null) return;
        Matcher matcher = JOIN.matcher(message.replace('\u00a0', ' '));
        if (!matcher.find()) return;
        String candidate = matcher.group(1);
        Set<String> tracked = Arrays.stream(ModConfig.get().trackedPlayers.split("[,;\\n]"))
                .map(String::strip).filter(name -> !name.isEmpty()).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        if (!tracked.contains(candidate.toLowerCase(Locale.ROOT))) return;
        MinecraftClient client = MinecraftClient.getInstance();
        playerName = candidate;
        expiresAt = System.currentTimeMillis() + 5500;
        client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 0.85f));
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (playerName == null) return;
        if (System.currentTimeMillis() >= expiresAt || client.player == null) { playerName = null; return; }
        if (client.currentScreen != null) return;

        int width = 230, height = 48;
        int x = (context.getScaledWindowWidth() - width) / 2;
        // Sit below the server's boss bars and announcements, leaving the crosshair unobstructed.
        int y = 52;
        context.fill(x, y, x+width, y+height, 0xE6101720);
        context.fill(x, y, x+width, y+2, 0xFFFFD383);
        context.fill(x, y+height-1, x+width, y+height, 0xFF756B57);
        context.fill(x, y+2, x+1, y+height-1, 0xFF756B57);
        context.fill(x+width-1, y+2, x+width, y+height-1, 0xFF756B57);
        PlayerListEntry entry = client.getNetworkHandler() == null ? null : client.getNetworkHandler().getPlayerListEntry(playerName);
        if (entry != null) PlayerSkinDrawer.draw(context, entry.getSkinTextures(), x+8, y+8, 32);
        else context.drawItem(Items.PLAYER_HEAD.getDefaultStack(), x+12, y+12);

        context.drawTextWithShadow(client.textRenderer, Text.literal(playerName), x+49, y+10, 0xFFFFD383);
        context.drawTextWithShadow(client.textRenderer, Text.translatable("craftaria_utils.tracker.joined"), x+49, y+26, 0xFFF0E9DA);
        int remaining = (int)Math.clamp((expiresAt-System.currentTimeMillis())*100/5500, 0, 100);
        context.fill(x+49, y+40, x+width-9, y+42, 0xFF45454B);
        context.fill(x+49, y+40, x+49+(width-58)*remaining/100, y+42, 0xFFFFD383);
    }
}
