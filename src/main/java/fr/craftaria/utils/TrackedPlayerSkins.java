package fr.craftaria.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;

import java.util.Locale;
import java.util.UUID;
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
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try { return client.getApiServices().profileRepository().findProfileByName(name).orElse(null); }
            catch (Exception ignored) { return null; }
        }).thenCompose(profile -> {
            if (profile == null) return java.util.concurrent.CompletableFuture.completedFuture(null);
            return client.getSkinProvider().fetchSkinTextures(new GameProfile(profile.id(), profile.name()));
        }).whenComplete((textures, error) -> client.execute(() -> {
            PENDING.remove(key);
            if (error == null && textures != null) textures.ifPresent(value -> SKINS.put(key, value));
        }));
    }

    private static String key(String name) { return name.toLowerCase(Locale.ROOT); }
}
