package fr.craftaria.utils.screen;

import fr.craftaria.utils.CraftariaUtilsClient;
import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class LoginConfirmScreen extends CraftariaScreen {
    private final Screen parent;
    public LoginConfirmScreen(Screen parent) { super(Text.translatable("craftaria_utils.login.title")); this.parent = parent; }
    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.yes"), b -> {
            CraftariaUtilsClient.sendCommand(MinecraftClient.getInstance(), "login " + ModConfig.get().password);
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(width / 2 - 104, height / 2 + 12, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.no"), b -> MinecraftClient.getInstance().setScreen(parent)).dimensions(width / 2 + 4, height / 2 + 12, 100, 20).build());
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("craftaria_utils.login.confirm"), width / 2, height / 2 - 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
