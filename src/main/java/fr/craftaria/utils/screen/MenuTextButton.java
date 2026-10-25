package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Text-only menu action with a pixel shadow and a visible keyboard focus marker. */
public final class MenuTextButton extends ClickableWidget {
    private final Runnable action;
    private final float textScale;

    public MenuTextButton(int x, int y, int width, int height, Text label, float textScale, Runnable action) {
        super(x, y, width, height, label);
        this.textScale = textScale;
        this.action = action;
    }

    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawLabel(context, getMessage(), getX(), getY(), textScale, isHovered() || isFocused(), active);
    }

    public static void drawLabel(DrawContext context, Text label, int x, int y, float scale, boolean highlighted, boolean active) {
        var renderer = MinecraftClient.getInstance().textRenderer;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        if (highlighted && active) context.drawText(renderer, ">", -12, 0, 0xFFFFCC65, true);
        context.drawText(renderer, label, 1, 2, 0xFF292421, false);
        context.drawText(renderer, label, 0, 0, highlighted && active ? 0xFFFFDA83 : 0xFFF7F4EA, false);
        context.getMatrices().popMatrix();
    }

    @Override public void onClick(Click click, boolean doubled) { action.run(); }
    @Override public boolean keyPressed(KeyInput input) {
        if (active && isFocused() && (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER || input.key() == GLFW.GLFW_KEY_SPACE)) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            action.run();
            return true;
        }
        return false;
    }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
