package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import fr.craftaria.utils.screen.GuiTextures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Uses the supplied banner, including its lettering, without drawing a second label. */
public final class OptionsImageButton extends ClickableWidget {
    private static final Identifier TEXTURE = Identifier.of("craftaria_utils", "textures/gui/options_button.png");
    private final Runnable action;
    private final ButtonWidget fallback;
    private boolean hasTexture;
    private int imageWidth, imageHeight;
    private int sourceX, sourceY, sourceWidth, sourceHeight;

    public OptionsImageButton(Runnable action) {
        this(308, action);
    }

    public OptionsImageButton(int width, Runnable action) {
        super(0, 0, width, Math.max(20, (int)Math.round(width * 30.0 / 308.0)), Text.translatable("craftaria_utils.title"));
        this.action = action;
        fallback = ButtonWidget.builder(getMessage(), button -> action.run()).width(width).build();
        var resource = MinecraftClient.getInstance().getResourceManager().getResource(TEXTURE);
        if (resource.isPresent()) {
            try (var stream = resource.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                int minX = imageWidth, minY = imageHeight, maxX = -1, maxY = -1;
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        int color = image.getColor(x, y);
                        int brightness = Math.max((color >> 16) & 255, Math.max((color >> 8) & 255, color & 255));
                        if ((color >>> 24) >= 128 && brightness > 20) {
                            minX = Math.min(minX, x); minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
                        }
                    }
                }
                if (maxX >= minX && maxY >= minY) {
                    sourceX = Math.max(0, minX-2); sourceY = Math.max(0, minY-2);
                    sourceWidth = Math.min(imageWidth-sourceX, maxX-sourceX+3);
                    sourceHeight = Math.min(imageHeight-sourceY, maxY-sourceY+3);
                    setHeight(Math.max(20, (int)Math.round(getWidth()*(double)sourceHeight/sourceWidth)));
                    hasTexture = true;
                }
            } catch (Exception ignored) { /* Keep the settings accessible if a resource pack replaces the image incorrectly. */ }
        }
    }

    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!hasTexture) {
            fallback.setX(getX());
            fallback.setY(getY()+(getHeight()-20)/2);
            fallback.setFocused(isFocused());
            fallback.render(context, mouseX, mouseY, delta);
            return;
        }
        GuiTextures.draw(context, TEXTURE, getX(), getY(), sourceX, sourceY,
                getWidth(), getHeight(), sourceWidth, sourceHeight, imageWidth, imageHeight);
        if (isHovered() || isFocused()) {
            int left = getX(), top = getY(), right = left + getWidth(), bottom = top + getHeight();
            int outline = 0xFFFFD383;
            context.fill(left, top, right, top + 1, outline);
            context.fill(left, bottom - 1, right, bottom, outline);
            context.fill(left, top + 1, left + 1, bottom - 1, outline);
            context.fill(right - 1, top + 1, right, bottom - 1, outline);
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
