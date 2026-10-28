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
    private static int evacuatedHotbarSlot = -1;
    private static int evacuatedInventorySlot = -1;
    private static int originalSelectedSlot = -1;
    private static int sourceSlot = -1;
    private static int totalCount;
    private static int completedCount;
    private static int delayTicks;
    private static boolean active;
    private static boolean stopping;
    private static Text startError;

    private CookingQueue() {}

    public static boolean start(Iterable<Integer> playerInventoryIndices) {
        MinecraftClient client = MinecraftClient.getInstance();
        startError = null;
        if (active) return reject("craftaria_utils.cooking.busy");
        if (client.player == null || client.getNetworkHandler() == null) return false;

        QUEUE.clear();
        for (int index : playerInventoryIndices) {
            if (index >= 0 && index < 36 && !client.player.getInventory().getStack(index).isEmpty()
                    && !QUEUE.contains(index)) QUEUE.add(index);
        }
        if (QUEUE.isEmpty()) return false;

        int emptySlot = -1;
        for (int index = 0; index < 9; index++) {
            if (client.player.getInventory().getStack(index).isEmpty()) { emptySlot = index; break; }
        }
        int reserveSlot = -1;
        int displacedHotbarSlot = -1;
        if (emptySlot == -1) {
            for (int index = 9; index < 36; index++) {
                if (client.player.getInventory().getStack(index).isEmpty()) { reserveSlot = index; break; }
            }
            if (reserveSlot == -1) return reject("craftaria_utils.cooking.inventory_full");

            // Keep an unselected hotbar stack out of the way when possible. If every
            // hotbar slot is selected, the displaced stack is processed from reserveSlot.
            for (int index = 0; index < 9; index++) {
                if (!QUEUE.contains(index)) { displacedHotbarSlot = index; break; }
            }
            if (displacedHotbarSlot == -1) displacedHotbarSlot = 0;
            if (QUEUE.remove(displacedHotbarSlot)) QUEUE.add(reserveSlot);
        }

        owner = client.player;
        workingHotbarSlot = emptySlot == -1 ? displacedHotbarSlot : emptySlot;
        evacuatedHotbarSlot = displacedHotbarSlot;
        evacuatedInventorySlot = reserveSlot;
        originalSelectedSlot = owner.getInventory().selectedSlot;
        sourceSlot = -1;
        ticksLeft = 0;
        totalCount = QUEUE.size();
        completedCount = 0;
        delayTicks = Math.clamp(ModConfig.get().cookingDelayTicks, 2, 100);
        stopping = false;
        active = true;
        if (evacuatedInventorySlot != -1) swapWithHotbar(client, evacuatedInventorySlot, evacuatedHotbarSlot);
        return true;
    }

    private static boolean reject(String key) {
        startError = Text.translatable(key);
        return false;
    }

    public static Text getStartError() { return startError; }
    public static boolean isActive() { return active; }
    public static int getTotalCount() { return totalCount; }
    public static int getCompletedCount() { return completedCount; }
    public static float getProgress() {
        if (!active || sourceSlot == -1 || delayTicks <= 0) return 0;
        return Math.clamp(1.0f - (float) ticksLeft / delayTicks, 0.0f, 1.0f);
    }

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
            completedCount++;
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
        ticksLeft = delayTicks - 1;
    }

    private static void swap(MinecraftClient client, int inventoryIndex) {
        swapWithHotbar(client, inventoryIndex, workingHotbarSlot);
    }

    private static void swapWithHotbar(MinecraftClient client, int inventoryIndex, int hotbarIndex) {
        int screenSlot = inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
        client.interactionManager.clickSlot(owner.playerScreenHandler.syncId, screenSlot,
                hotbarIndex, SlotActionType.SWAP, owner);
    }

    private static void selectHotbar(MinecraftClient client, int slot) {
        owner.getInventory().selectedSlot = slot;
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    private static void finish(MinecraftClient client, boolean completed) {
        restoreEvacuatedStack(client);
        selectHotbar(client, originalSelectedSlot);
        if (completed) owner.sendMessage(Text.translatable("craftaria_utils.cooking.done"), false);
        reset();
    }

    private static void restoreEvacuatedStack(MinecraftClient client) {
        if (evacuatedInventorySlot == -1) return;
        if (owner.getInventory().getStack(evacuatedInventorySlot).isEmpty()
                || !owner.getInventory().getStack(workingHotbarSlot).isEmpty()) {
            owner.sendMessage(Text.translatable("craftaria_utils.cooking.inventory_changed"), false);
            return;
        }
        swapWithHotbar(client, evacuatedInventorySlot, evacuatedHotbarSlot);
    }

    private static void reset() {
        active = false;
        stopping = false;
        owner = null;
        sourceSlot = -1;
        workingHotbarSlot = -1;
        evacuatedHotbarSlot = -1;
        evacuatedInventorySlot = -1;
        originalSelectedSlot = -1;
        QUEUE.clear();
        totalCount = 0;
        completedCount = 0;
        ticksLeft = 0;
        delayTicks = 0;
    }
}
