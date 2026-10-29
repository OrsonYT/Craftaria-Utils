package fr.craftaria.utils;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Detects new UUIDs in the server player list after the initial roster has loaded. */
public final class ConnectionTracker {
    private static String playerName;
    private static long expiresAt;
    private static boolean preview;
    private static ConnectionRoster.Kind activeKind=ConnectionRoster.Kind.JOINED;
    private static net.minecraft.client.network.ClientPlayNetworkHandler connection;
    private static final ConnectionRoster roster = new ConnectionRoster();
    private static final java.util.ArrayDeque<ConnectionRoster.Notice> pending = new java.util.ArrayDeque<>();

    private ConnectionTracker() { }

    public static void tick(MinecraftClient client) {
        var network = client.getNetworkHandler();
        if (client.player == null || network == null) {
            connection=null; roster.reset(); pending.clear(); playerName=null; return;
        }
        if (network != connection) {
            connection=network; pending.clear(); playerName=null;
        }
        var current = network.getPlayerList().stream()
                .filter(entry -> !entry.getProfile().getId().equals(client.player.getUuid()))
                .collect(Collectors.toMap(entry -> entry.getProfile().getId(),entry -> entry.getProfile().getName(),(a,b)->a));
        Set<String> tracked = TrackedPlayers.parse(ModConfig.get().trackedPlayers).stream()
                .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        boolean ready=client.currentScreen==null && client.getOverlay()==null && client.world!=null;
        for(var notice:roster.update(network,current,tracked,ModConfig.get().connectionTrackerEnabled,
                ModConfig.get().trackerNotifyAlreadyOnline,ready)) {
            if(enabled(notice.kind()) && pending.size()<100) pending.addLast(notice);
        }
        if (!ModConfig.get().connectionTrackerEnabled) { pending.clear(); if(!preview) playerName=null; return; }
        if(!ready && playerName!=null) expiresAt+=50;
        if (ready && (playerName == null || System.currentTimeMillis()>=expiresAt)) {
            while(!pending.isEmpty()) {
                var notice=pending.removeFirst();
                if(!enabled(notice.kind()) || !tracked.contains(notice.name().toLowerCase(Locale.ROOT))) continue;
                if(notice.kind()==ConnectionRoster.Kind.ALREADY_ONLINE && !isOnline(client,notice.name())) continue;
                show(client,notice.name(),notice.kind(),false); break;
            }
        }
    }

    private static boolean enabled(ConnectionRoster.Kind kind) {
        return switch(kind) {
            case JOINED -> ModConfig.get().trackerNotifyJoin;
            case LEFT -> ModConfig.get().trackerNotifyLeave;
            case ALREADY_ONLINE -> ModConfig.get().trackerNotifyAlreadyOnline;
        };
    }

    private static void show(MinecraftClient client,String name,ConnectionRoster.Kind kind,boolean isPreview) {
        playerName=name; activeKind=kind; preview=isPreview; expiresAt=System.currentTimeMillis()+5500;
        TrackedPlayerSkins.request(name);
        client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,kind==ConnectionRoster.Kind.LEFT?.7f:1f,1f));
    }

    public static void preview(MinecraftClient client) {
        if(client.player==null) return;
        var tracked=TrackedPlayers.parse(ModConfig.get().trackedPlayers);
        show(client,tracked.isEmpty()?client.player.getGameProfile().getName():tracked.getFirst(),ConnectionRoster.Kind.JOINED,true);
        client.setScreen(null);
    }

    public static boolean isOnline(MinecraftClient client,String name) {
        return client.getNetworkHandler()!=null && client.getNetworkHandler().getPlayerList().stream()
                .anyMatch(entry->entry.getProfile().getName().equalsIgnoreCase(name));
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (playerName == null) return;
        if (System.currentTimeMillis() >= expiresAt || client.player == null) { playerName = null; return; }
        if (client.currentScreen != null || client.getOverlay()!=null) return;

        context.draw();
        context.getMatrices().push();
        context.getMatrices().translate(0,0,800);
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
        else if (TrackedPlayerSkins.get(playerName)!=null) PlayerSkinDrawer.draw(context,TrackedPlayerSkins.get(playerName),x+8,y+8,32);
        else context.drawItem(Items.PLAYER_HEAD.getDefaultStack(), x+12, y+12);

        context.drawTextWithShadow(client.textRenderer, Text.literal(playerName), x+49, y+10, 0xFFFFD383);
        context.drawTextWithShadow(client.textRenderer, Text.translatable(preview?"craftaria_utils.tracker.preview_message":switch(activeKind) {
            case JOINED -> "craftaria_utils.tracker.joined";
            case LEFT -> "craftaria_utils.tracker.left";
            case ALREADY_ONLINE -> "craftaria_utils.tracker.already_online";
        }), x+49, y+26, 0xFFF0E9DA);
        int remaining = (int)Math.clamp((expiresAt-System.currentTimeMillis())*100/5500, 0, 100);
        context.fill(x+49, y+40, x+width-9, y+42, 0xFF45454B);
        context.fill(x+49, y+40, x+49+(width-58)*remaining/100, y+42, 0xFFFFD383);
        context.draw();
        context.getMatrices().pop();
    }
}
