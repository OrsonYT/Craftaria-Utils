package fr.craftaria.utils.screen;

import fr.craftaria.utils.cooking.CookingQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.TreeSet;

/** Uses the player's real inventory slots, but clicks only change the local selection. */
public final class CookingScreen extends HandledScreen<PlayerScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/inventory.png");
    private final Screen parent;
    private final Set<Integer> selected = new TreeSet<>();
    private ButtonWidget cookButton;
    private Text startError;
    private int mouseX, mouseY;

    public CookingScreen(Screen parent) {
        super(MinecraftClient.getInstance().player.playerScreenHandler,
                MinecraftClient.getInstance().player.getInventory(), Text.translatable("craftaria_utils.cooking.title"));
        this.parent = parent;
    }

    @Override protected void init() {
        super.init();
        cookButton = addDrawableChild(ButtonWidget.builder(startLabel(), button -> {
            if (CookingQueue.start(selected)) client.setScreen(null);
            else startError = CookingQueue.getStartError();
        }).dimensions(x, y + backgroundHeight + 6, 110, 20).build());
        cookButton.active = !selected.isEmpty();
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
                .dimensions(x + 114, y + backgroundHeight + 6, 62, 20).build());
    }

    private Text startLabel() { return Text.translatable("craftaria_utils.cooking.start", selected.size()); }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        for (Slot slot : handler.slots) {
            if (click.x() >= x + slot.x && click.x() < x + slot.x + 16
                    && click.y() >= y + slot.y && click.y() < y + slot.y + 16) {
                if (click.button() == 0 && slot.inventory == client.player.getInventory()
                        && slot.getIndex() < 36 && slot.hasStack()) {
                    if (!selected.add(slot.getIndex())) selected.remove(slot.getIndex());
                    cookButton.setMessage(startLabel());
                    cookButton.active = !selected.isEmpty();
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        // Selection never sends inventory clicks, swaps, drops, or crafting actions to the server.
    }

    @Override protected void drawSlot(DrawContext context, Slot slot, int mouseX, int mouseY) {
        super.drawSlot(context, slot, mouseX, mouseY);
        if (slot.inventory == client.player.getInventory() && selected.contains(slot.getIndex())) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x8033DD55);
        }
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
        InventoryScreen.drawEntity(context, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F,
                (float)this.mouseX, (float)this.mouseY, client.player);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, startError != null ? startError
                        : Text.translatable("craftaria_utils.cooking.hint", selected.size()),
                width / 2, y - 14, startError != null ? 0xFFFF7777 : 0xFFFFFFFF);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override protected void applyBlur(DrawContext context) { }
    @Override public void close() { client.setScreen(parent); }
}
