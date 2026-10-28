package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import fr.craftaria.utils.screen.GuiTextures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Vanilla status protocol, with DNS resolution off the render thread. */
public final class ServerStatusPanel {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Craftaria server status");
        thread.setDaemon(true);
        return thread;
    });
    private MultiplayerServerListPinger pinger;
    private ServerInfo server;
    private boolean closed = true;
    private long startedAt;
    private long nextRefresh;

    public void open() {
        closed = false;
        refresh();
    }

    private void refresh() {
        if (pinger != null) pinger.cancel();
        MultiplayerServerListPinger current = new MultiplayerServerListPinger();
        ServerInfo info = new ServerInfo("Craftaria", CraftariaTitleScreen.SERVER, ServerInfo.ServerType.OTHER);
        info.setStatus(ServerInfo.Status.PINGING);
        info.ping = -1;
        pinger = current;
        server = info;
        startedAt = System.nanoTime();
        nextRefresh = startedAt + 30_000_000_000L;
        EXECUTOR.execute(() -> {
            try {
                current.add(info, () -> { }, () -> MinecraftClient.getInstance().execute(() -> {
                    if (!closed && pinger == current) info.setStatus(ServerInfo.Status.SUCCESSFUL);
                }));
            } catch (Exception exception) {
                MinecraftClient.getInstance().execute(() -> {
                    if (!closed && pinger == current) info.setStatus(ServerInfo.Status.UNREACHABLE);
                });
            } finally {
                MinecraftClient.getInstance().execute(() -> {
                    if (closed || pinger != current) current.cancel();
                });
            }
        });
    }

    public void tick() {
        if (closed) return;
        pinger.tick();
        long now = System.nanoTime();
        if (server.getStatus() == ServerInfo.Status.PINGING && now - startedAt > 10_000_000_000L) {
            pinger.cancel();
            server.setStatus(ServerInfo.Status.UNREACHABLE);
        }
        if (now >= nextRefresh) refresh();
    }

    public void close() {
        closed = true;
        if (pinger != null) pinger.cancel();
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        if (server == null) return;
        var font = MinecraftClient.getInstance().textRenderer;
        // Match the size, right margin and muted text of the credits below.
        float scale = Math.clamp(Math.min(screenWidth / 960f, screenHeight / 540f), 0.5f, 0.7f);
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1);
        int right = (int)((screenWidth - 6) / scale), top = (int)(8 / scale);
        boolean pending = server.getStatus() == ServerInfo.Status.PINGING;
        boolean online = server.getStatus() == ServerInfo.Status.SUCCESSFUL;
        Text players = online && server.players != null
                ? Text.translatable("craftaria_utils.server.players", server.players.online(), server.players.max())
                : Text.translatable(pending ? "craftaria_utils.server.checking" : online
                    ? "craftaria_utils.server.players_unknown" : "craftaria_utils.server.offline");
        Text latency = Text.literal(online ? Math.max(0, server.ping) + " ms" : pending ? "…" : "—");
        context.drawTextWithShadow(font, players, right-font.getWidth(players), top, 0xFFCDC7BA);
        int color = server.ping < 150 ? 0xFF69E27C : server.ping < 300 ? 0xFFB6DB66
                : server.ping < 600 ? 0xFFFFD16A : server.ping < 1000 ? 0xFFFFA45A : 0xFFFF6F6F;
        int barsX = right-10, barsY = top+10;
        if (online) {
            int bars = server.ping < 150 ? 5 : server.ping < 300 ? 4 : server.ping < 600 ? 3 : server.ping < 1000 ? 2 : 1;
            for (int i = 0; i < 5; i++) {
                int h = 2 + i*6/4;
                context.fill(barsX+i*2, barsY+8-h, barsX+i*2+1, barsY+8, i < bars ? color : 0xFF545862);
            }
        } else {
            int frame = (int)((System.nanoTime()-startedAt)/100_000_000L % 8);
            if (frame > 4) frame = 8-frame;
            Identifier sprite = Identifier.ofVanilla(pending ? "server_list/pinging_"+(frame+1) : "server_list/unreachable");
            context.drawGuiTexture(sprite, barsX, barsY, 10, 8);
        }
        context.drawTextWithShadow(font, latency, barsX-5-font.getWidth(latency), top+10,
                online || pending ? 0xFFCDC7BA : 0xFFD39E96);
        context.getMatrices().pop();
    }
}
