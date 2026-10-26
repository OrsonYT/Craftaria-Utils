package fr.craftaria.utils.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.List;
import java.util.function.Consumer;

/** Inserts server chat tags into the draft; never sends a chat message. */
public final class ChatShareMenu {
    private record Entry(String token, String label, Item icon) { }
    private static final List<Entry> ENTRIES = List.of(
            new Entry("#inv", "inventory", Items.CHEST),
            new Entry("#item", "item", Items.DIAMOND_PICKAXE),
            new Entry("#ec", "enderchest", Items.ENDER_CHEST),
            new Entry("#bal", "balance", Items.EMERALD),
            new Entry("#sk", "skills", Items.EXPERIENCE_BOTTLE),
            new Entry("#guild", "guild", Items.WHITE_BANNER),
            new Entry("#gbank", "guild_bank", Items.GOLD_BLOCK));
    private static final int BUTTON_SIZE = 20;
    private static final int ROW_HEIGHT = 22;
    private final Consumer<String> insert;
    private int x, buttonY, popupY, popupWidth;
    private final int popupHeight = 22+ENTRIES.size()*ROW_HEIGHT;
    private boolean open;

    public ChatShareMenu(Consumer<String> insert) { this.insert = insert; }
    public boolean isOpen() { return open; }
    public void close() { open = false; }

    public void layout(int screenWidth, int fieldY) {
        x = 4;
        buttonY = Math.max(4, fieldY-26);
        popupWidth = Math.min(242, screenWidth-8);
        popupY = Math.max(2, buttonY-popupHeight-4);
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        var client = MinecraftClient.getInstance();
        var font = client.textRenderer;
        context.createNewRootLayer();
        boolean buttonHovered = inside(mouseX, mouseY, x, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        context.fill(x, buttonY, x+BUTTON_SIZE, buttonY+BUTTON_SIZE, 0xDC101720);
        border(context, x, buttonY, BUTTON_SIZE, BUTTON_SIZE, buttonHovered || open ? 0xFFFFD383 : 0xFF756B57);
        context.drawItem(Items.WRITABLE_BOOK.getDefaultStack(), x+2, buttonY+2);
        if (!open) {
            if (buttonHovered) context.drawTooltip(font, Text.translatable("craftaria_utils.chat.share"), mouseX, mouseY);
            return;
        }
        context.fill(x, popupY, x+popupWidth, popupY+popupHeight, 0xF2101720);
        border(context, x, popupY, popupWidth, popupHeight, 0xFFCFAD6D);
        context.drawTextWithShadow(font, Text.translatable("craftaria_utils.chat.share"), x+7, popupY+6, 0xFFFFD383);
        context.fill(x+5, popupY+18, x+popupWidth-5, popupY+19, 0xFF514D45);
        Text tooltip = null;
        for (int i=0; i<ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);
            int y = popupY+20+i*ROW_HEIGHT;
            boolean hovered = inside(mouseX, mouseY, x+2, y, popupWidth-4, ROW_HEIGHT);
            if (hovered) {
                context.fill(x+2, y, x+popupWidth-2, y+ROW_HEIGHT, 0xFF29313A);
                tooltip = Text.translatable("craftaria_utils.chat."+entry.label()+".description");
            }
            ItemStack icon = entry.icon().getDefaultStack();
            if (entry.token().equals("#item") && client.player != null && !client.player.getMainHandStack().isEmpty())
                icon = client.player.getMainHandStack();
            context.drawItem(icon, x+6, y+3);
            context.drawTextWithShadow(font, Text.translatable("craftaria_utils.chat."+entry.label()), x+28, y+7, 0xFFF0E9DA);
            context.drawTextWithShadow(font, entry.token(), x+popupWidth-8-font.getWidth(entry.token()), y+7, 0xFFB8B4AA);
        }
        if (tooltip != null) context.drawTooltip(font, tooltip, mouseX, mouseY);
    }

    /** Return true to keep clicks on the popup from activating chat links underneath. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, x, buttonY, BUTTON_SIZE, BUTTON_SIZE)) {
            if (button == 0) { open = !open; clickSound(); }
            return true;
        }
        if (!open) return false;
        if (inside(mouseX, mouseY, x, popupY, popupWidth, popupHeight)) {
            if (button == 0 && mouseY >= popupY+20) {
                int index = (int)(mouseY-popupY-20)/ROW_HEIGHT;
                if (index >= 0 && index < ENTRIES.size()) {
                    insert.accept(ENTRIES.get(index).token());
                    open = false;
                    clickSound();
                }
            }
            return true;
        }
        open = false;
        return false;
    }

    public boolean covers(double mouseX, double mouseY) {
        return open && inside(mouseX, mouseY, x, popupY, popupWidth, popupHeight);
    }
    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x+width && mouseY >= y && mouseY < y+height;
    }
    private static void clickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1f));
    }
    private static void border(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x,y,x+width,y+1,color);
        context.fill(x,y+height-1,x+width,y+height,color);
        context.fill(x,y+1,x+1,y+height-1,color);
        context.fill(x+width-1,y+1,x+width,y+height-1,color);
    }
}
