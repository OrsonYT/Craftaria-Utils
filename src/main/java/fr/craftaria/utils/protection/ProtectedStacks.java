package fr.craftaria.utils.protection;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tracks selected stacks individually using their complete item components and count. */
public final class ProtectedStacks {
    private static final String PREFIX = "stack:";
    private ProtectedStacks() { }

    public static String fingerprint(ItemStack stack, DynamicRegistryManager registries) {
        if (stack == null || stack.isEmpty()) return "";
        var normalized = stack.copyWithCount(1);
        normalized.remove(net.minecraft.component.DataComponentTypes.DAMAGE);
        var encoded = ItemStack.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), normalized)
                .result().orElse(null);
        if (encoded == null) return "";
        return PREFIX + Registries.ITEM.getId(stack.getItem()) + ":"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(encoded.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static int count(ItemStack stack, DynamicRegistryManager registries) {
        String key = fingerprint(stack, registries);
        return key.isEmpty() ? 0 : ModConfig.get().protectedStackCounts.getOrDefault(key, 0);
    }

    public static boolean isProtected(ItemStack stack, DynamicRegistryManager registries) {
        return !stack.isEmpty() && count(stack, registries) > 0;
    }

    public static void add(ItemStack stack, DynamicRegistryManager registries) {
        String key = fingerprint(stack, registries);
        if (key.isEmpty()) return;
        ModConfig.get().protectedStackCounts.put(key, stack.getCount());
        syncList();
    }

    public static void remove(ItemStack stack, DynamicRegistryManager registries) {
        ModConfig.get().protectedStackCounts.remove(fingerprint(stack, registries));
        syncList();
    }

    /** Missing and ambiguous selections are retained; never guess a replacement by item type. */
    public static boolean needsReselection(List<ItemStack> inventory, DynamicRegistryManager registries) {
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        for (ItemStack stack : inventory) if (!stack.isEmpty())
            occurrences.merge(fingerprint(stack, registries), 1, Integer::sum);
        for (String key : ModConfig.get().protectedStackCounts.keySet())
            if (occurrences.getOrDefault(key, 0) != 1) return true;
        return !ModConfig.get().protectedItems.isEmpty() && ModConfig.get().protectedStackCounts.isEmpty();
    }

    public static String decodePayload(String key) {
        int separator = key.indexOf(':', key.indexOf(':', PREFIX.length()) + 1);
        return new String(Base64.getUrlDecoder().decode(key.substring(separator + 1)), java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Upgrade old full snapshots without guessing which inventory item they describe. */
    public static void migrate(DynamicRegistryManager registries) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        for (var entry : ModConfig.get().protectedStackCounts.entrySet()) {
            try {
                var stack = ItemStack.fromNbt(registries, net.minecraft.nbt.StringNbtReader.parse(decodePayload(entry.getKey()))).orElse(ItemStack.EMPTY);
                String key = fingerprint(stack, registries);
                normalized.merge(key.isEmpty() ? entry.getKey() : key, entry.getValue(), Integer::sum);
            } catch (Exception ignored) { normalized.put(entry.getKey(), entry.getValue()); }
        }
        if (!normalized.equals(ModConfig.get().protectedStackCounts)) {
            ModConfig.get().protectedStackCounts = normalized;
            syncList();
            ModConfig.save();
        }
    }

    public static void removeKey(String key) {
        ModConfig.get().protectedStackCounts.remove(key);
        syncList();
    }

    public static ItemStack previewStack(String key) {
        if (key.startsWith("legacy:")) return fr.craftaria.utils.config.QuickHome.iconStack(key.substring("legacy:".length()));
        try {
            String encoded = decodePayload(key);
            var nbt = net.minecraft.nbt.StringNbtReader.parse(encoded);
            var client = MinecraftClient.getInstance();
            if (client.world != null) return ItemStack.fromNbt(client.world.getRegistryManager(), nbt).orElse(ItemStack.EMPTY);
        } catch (Exception ignored) { }
        return ItemStack.EMPTY;
    }

    private static String itemId(String fingerprint) {
        if (!fingerprint.startsWith(PREFIX)) return "";
        int separator = fingerprint.indexOf(':', PREFIX.length());
        return separator < 0 ? fingerprint.substring(PREFIX.length()) : fingerprint.substring(PREFIX.length(), separator);
    }

    private static void syncList() {
        ModConfig.get().protectedItems = new ArrayList<>(ModConfig.get().protectedStackCounts.keySet());
    }
}
