package fr.craftaria.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Resolves and caches Mojang skin textures for usernames in the connection tracker. */
public final class TrackedPlayerSkins {
    private static final ConcurrentMap<String, SkinTextures> SKINS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Boolean> PENDING = new ConcurrentHashMap<>();

    private TrackedPlayerSkins() { }

    public static SkinTextures get(String name) { return SKINS.get(key(name)); }

    public static void request(String name) {
        if (name == null || name.isBlank()) return;
        String key = key(name);
        if (SKINS.containsKey(key) || PENDING.putIfAbsent(key, true) != null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        var online = client.getNetworkHandler() == null ? null : client.getNetworkHandler().getPlayerListEntry(name);
        if (online != null) {
            SKINS.put(key, online.getSkinTextures());
            PENDING.remove(key);
            return;
        }
        net.minecraft.block.entity.SkullBlockEntity.fetchProfileByName(name).thenCompose(profile -> {
            if (profile.isEmpty()) return java.util.concurrent.CompletableFuture.<SkinTextures>completedFuture(null);
            return client.getSkinProvider().fetchSkinTextures(profile.get());
        }).whenComplete((textures, error) -> client.execute(() -> {
            PENDING.remove(key);
            if (error == null && textures != null) SKINS.put(key, textures);
        }));
    }

    private static String key(String name) { return name.toLowerCase(Locale.ROOT); }
}
