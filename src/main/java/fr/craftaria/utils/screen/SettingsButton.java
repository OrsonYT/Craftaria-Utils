package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.function.Supplier;

/** Quiet, gold-accented action matching the custom title menu. */
public final class SettingsButton extends ClickableWidget {
    private final Runnable action;
    private final Supplier<Text> label;
    public SettingsButton(int x, int y, int width, Supplier<Text> label, Runnable action) {
        super(x, y, width, 22, label.get());
        this.action = action;
        this.label = label;
    }
    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        setMessage(label.get());
        boolean highlighted = isHovered() || isFocused();
        context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), highlighted ? 0x453E3020 : 0x35101720);
        context.fill(getX(), getY()+getHeight()-1, getX()+getWidth(), getY()+getHeight(), highlighted ? 0xFFFFD383 : 0x806E6554);
        var font = MinecraftClient.getInstance().textRenderer;
        String text = font.trimToWidth(getMessage().getString(), getWidth()-14);
        context.drawTextWithShadow(font, Text.literal(text), getX()+7, getY()+7, highlighted ? 0xFFFFD383 : 0xFFF0E9DA);
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
