package fr.craftaria.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** The proxy world is not considered the destination until the server pack has loaded. */
public final class ServerArrival {
    private static final Map<ClientConnection, Packs> packs = new WeakHashMap<>();
    private static Object world;
    private static int stableTicks;
    private static boolean ready;
    private static final class Packs {
        final HashSet<UUID> pending = new HashSet<>();
        boolean loaded;
    }
    private ServerArrival() { }
    public static synchronized void resourcePackStatus(ClientConnection connection, ResourcePackStatusC2SPacket packet) {
        var state=packs.computeIfAbsent(connection, key -> new Packs());
        switch(packet.status()) {
            case ACCEPTED, DOWNLOADED -> state.pending.add(packet.id());
            case SUCCESSFULLY_LOADED -> { state.pending.remove(packet.id()); state.loaded=true; }
            default -> { state.pending.remove(packet.id()); state.loaded=false; }
        }
        stableTicks=0; ready=false;
    }
    public static synchronized void tick(MinecraftClient client) {
        if(world!=client.world) { world=client.world; stableTicks=0; ready=false; }
        var network=client.getNetworkHandler();
        if(client.world==null || client.player==null || network==null) { stableTicks=0; ready=false; return; }
        var state=packs.get(network.getConnection());
        var server=client.getCurrentServerEntry();
        String host=server==null?"":server.address.toLowerCase(java.util.Locale.ROOT).split(":",2)[0];
        boolean craftaria=host.equals("craftaria.fr") || host.endsWith(".craftaria.fr");
        boolean packReady=state==null?!craftaria:state.loaded && state.pending.isEmpty();
        if(!packReady || client.getOverlay()!=null) { stableTicks=0; ready=false; return; }
        // Once arrived, opening ordinary menus does not suspend protection.
        if(!ready) {
            if(client.currentScreen!=null) { stableTicks=0; return; }
            if(++stableTicks>=40) ready=true;
        }
    }
    public static synchronized boolean isReady() { return ready; }
}
