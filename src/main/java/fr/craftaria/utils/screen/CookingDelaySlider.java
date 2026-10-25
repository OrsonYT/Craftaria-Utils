package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import java.util.Locale;

public final class CookingDelaySlider extends SliderWidget {
    public CookingDelaySlider(int x, int y, int width) {
        super(x, y, width, 20, Text.empty(), (ModConfig.get().cookingDelayTicks - 2) / 98.0);
        updateMessage();
    }

    @Override public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var font = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(font, getMessage(), getX(), getY(), 0xFFF0E9DA);
        int trackY=getY()+16;
        context.fill(getX(), trackY, getX()+getWidth(), trackY+2, 0xFF514D45);
        int handleX=getX()+(int)Math.round(value*(getWidth()-8));
        context.fill(getX(), trackY, handleX+4, trackY+2, 0xFFCFAD6D);
        context.fill(handleX, trackY-3, handleX+8, trackY+5, isHovered() || isFocused() ? 0xFFFFD383 : 0xFFE4D3AC);
    }

    @Override protected void updateMessage() {
        setMessage(Text.translatable("craftaria_utils.settings.delay",
                String.format(Locale.getDefault(), "%.1f", ModConfig.get().cookingDelayTicks / 20.0)));
    }

    @Override protected void applyValue() {
        // Fifty values, one for each tenth of a second from 0.1 to 5.0.
        ModConfig.get().cookingDelayTicks = 2 + (int)Math.round(value * 49) * 2;
        ModConfig.save();
        updateMessage();
    }
}
