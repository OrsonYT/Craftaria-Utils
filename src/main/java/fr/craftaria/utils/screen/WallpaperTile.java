package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class WallpaperTile extends ClickableWidget {
    private final String wallpaper;
    private final Runnable action;

    public WallpaperTile(int x, int y, int width, int height, String wallpaper, Runnable action) {
        super(x, y, width, height, WallpaperManager.label(wallpaper));
        this.wallpaper = wallpaper;
        this.action = action;
        setTooltip(Tooltip.of(WallpaperManager.tooltip(wallpaper)));
    }

    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        WallpaperManager.drawThumbnail(context, wallpaper, x+1, y+1, w-2, h-17);
        context.fill(x, y+h-16, x+w, y+h, 0xE0101720);
        boolean selected = wallpaper.equals(ModConfig.get().wallpaper);
        int border = selected ? 0xFFFFD383 : isHovered() || isFocused() ? 0xFFE4D3AC : 0xFF60584A;
        context.fill(x, y, x+w, y+1, border);
        context.fill(x, y+h-1, x+w, y+h, border);
        context.fill(x, y+1, x+1, y+h-1, border);
        context.fill(x+w-1, y+1, x+w, y+h-1, border);
        var font = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(font, font.trimToWidth(getMessage().getString(), w-12), x+6, y+h-12,
                selected ? 0xFFFFD383 : 0xFFF0E9DA);
        if (selected) {
            context.fill(x+w-15, y+4, x+w-4, y+15, 0xDD101720);
            context.fill(x+w-13, y+9, x+w-11, y+12, 0xFFFFD383);
            context.fill(x+w-11, y+10, x+w-9, y+13, 0xFFFFD383);
            context.fill(x+w-9, y+7, x+w-7, y+11, 0xFFFFD383);
        }
    }

    @Override public void onClick(double mouseX, double mouseY) { action.run(); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (active && isFocused() && (keyCode==GLFW.GLFW_KEY_ENTER || keyCode==GLFW.GLFW_KEY_SPACE || keyCode==GLFW.GLFW_KEY_KP_ENTER)) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            action.run();
            return true;
        }
        return false;
    }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
