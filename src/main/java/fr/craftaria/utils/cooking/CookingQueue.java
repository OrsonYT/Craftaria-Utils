package fr.craftaria.utils.cooking;

import fr.craftaria.utils.CraftariaUtilsClient;
import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Queue;

public final class CookingQueue {
    private static final Queue<Integer> QUEUE = new ArrayDeque<>();
    private static ClientPlayerEntity owner;
    private static int ticksLeft;
    private static int workingHotbarSlot = -1;
    private static int originalSelectedSlot = -1;
    private static int sourceSlot = -1;
    private static boolean active;
    private static boolean stopping;
    private static Text startError;

    private CookingQueue() {}

    public static boolean start(Iterable<Integer> playerInventoryIndices) {
        MinecraftClient client = MinecraftClient.getInstance();
        startError = null;
        if (active) return reject("craftaria_utils.cooking.busy");
        if (client.player == null || client.getNetworkHandler() == null) return false;

        int emptySlot = -1;
        for (int index = 0; index < 9; index++) {
            if (client.player.getInventory().getStack(index).isEmpty()) { emptySlot = index; break; }
        }
        if (emptySlot == -1) return reject("craftaria_utils.cooking.hotbar_full");

        QUEUE.clear();
        for (int index : playerInventoryIndices) {
            if (index >= 0 && index < 36 && !client.player.getInventory().getStack(index).isEmpty()
                    && !QUEUE.contains(index)) QUEUE.add(index);
        }
        if (QUEUE.isEmpty()) return false;
        owner = client.player;
        workingHotbarSlot = emptySlot;
        originalSelectedSlot = owner.getInventory().getSelectedSlot();
        sourceSlot = -1;
        ticksLeft = 0;
        stopping = false;
        active = true;
        return true;
    }

    private static boolean reject(String key) {
        startError = Text.translatable(key);
        return false;
    }

    public static Text getStartError() { return startError; }
    public static boolean isActive() { return active; }

    /** Stop after putting the current stack back; never abandon a stack mid-swap. */
    public static void cancel() { stopping = true; QUEUE.clear(); }

    public static void tick(MinecraftClient client) {
        if (!active) return;
        if (client.player != owner || client.interactionManager == null || client.getNetworkHandler() == null) {
            reset();
            return;
        }
        if (client.player.currentScreenHandler != client.player.playerScreenHandler) {
            // Defer inventory clicks until the player's own inventory is available again.
            cancel();
            return;
        }
        if (ticksLeft > 0) { ticksLeft--; return; }

        if (sourceSlot != -1) {
            // The source was emptied by the first swap. Return the cooked stack to that exact slot.
            if (!owner.getInventory().getStack(sourceSlot).isEmpty()) {
                owner.sendMessage(Text.translatable("craftaria_utils.cooking.inventory_changed"), false);
                finish(client, false);
                return;
            }
            swap(client, sourceSlot);
            sourceSlot = -1;
        }

        if (stopping || QUEUE.isEmpty()) { finish(client, !stopping); return; }
        if (!owner.getInventory().getStack(workingHotbarSlot).isEmpty()) {
            owner.sendMessage(Text.translatable("craftaria_utils.cooking.inventory_changed"), false);
            finish(client, false);
            return;
        }

        Integer next;
        do { next = QUEUE.poll(); }
        while (next != null && owner.getInventory().getStack(next).isEmpty());
        if (next == null) { finish(client, true); return; }

        sourceSlot = next;
        swap(client, sourceSlot);
        selectHotbar(client, workingHotbarSlot);
        CraftariaUtilsClient.sendCommand(client, "four");
        // Let the server update the held stack before returning it to its source slot.
        ticksLeft = Math.clamp(ModConfig.get().cookingDelayTicks, 2, 100) - 1;
    }

    private static void swap(MinecraftClient client, int inventoryIndex) {
        int screenSlot = inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
        client.interactionManager.clickSlot(owner.playerScreenHandler.syncId, screenSlot,
                workingHotbarSlot, SlotActionType.SWAP, owner);
    }

    private static void selectHotbar(MinecraftClient client, int slot) {
        owner.getInventory().setSelectedSlot(slot);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    private static void finish(MinecraftClient client, boolean completed) {
        selectHotbar(client, originalSelectedSlot);
        if (completed) owner.sendMessage(Text.translatable("craftaria_utils.cooking.done"), false);
        reset();
    }

    private static void reset() {
        active = false;
        stopping = false;
        owner = null;
        sourceSlot = -1;
        workingHotbarSlot = -1;
        originalSelectedSlot = -1;
        QUEUE.clear();
    }
}
