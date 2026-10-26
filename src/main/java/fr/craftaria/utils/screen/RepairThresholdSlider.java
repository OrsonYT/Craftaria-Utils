package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class RepairThresholdSlider extends SliderWidget {
    public RepairThresholdSlider(int x, int y, int width) {
        super(x, y, width, 20, Text.empty(), (ModConfig.get().repairThresholdPercent - 1) / 99.0);
        updateMessage();
    }

    @Override public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var font = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(font, getMessage(), getX(), getY(), 0xFFF0E9DA);
        int trackY = getY() + 16;
        context.fill(getX(), trackY, getX() + getWidth(), trackY + 2, 0xFF514D45);
        int handleX = getX() + (int)Math.round(value * (getWidth() - 8));
        context.fill(getX(), trackY, handleX + 4, trackY + 2, 0xFFCFAD6D);
        context.fill(handleX, trackY - 3, handleX + 8, trackY + 5,
                isHovered() || isFocused() ? 0xFFFFD383 : 0xFFE4D3AC);
    }

    @Override protected void updateMessage() {
        setMessage(Text.translatable("craftaria_utils.settings.repair_threshold",
                ModConfig.get().repairThresholdPercent));
    }

    @Override protected void applyValue() {
        ModConfig.get().repairThresholdPercent = 1 + (int)Math.round(value * 99);
        ModConfig.save();
        updateMessage();
    }
}
