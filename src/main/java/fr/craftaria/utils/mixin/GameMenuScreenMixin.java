package fr.craftaria.utils.mixin;

import fr.craftaria.utils.screen.CraftariaConfigScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void craftariaUtils$addButton(CallbackInfo ci) {
        GameMenuScreen screen = (GameMenuScreen)(Object)this;
        int y = screen.height / 4 + 144;
        for (var child : screen.children()) {
            if (child instanceof ButtonWidget button && button.getMessage().getString().equals(Text.translatable("menu.disconnect").getString())) {
                y = button.getY();
                button.setY(button.getY() + 24);
                break;
            }
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("craftaria_utils.title"), b -> MinecraftClient.getInstance().setScreen(new CraftariaConfigScreen(screen))).dimensions(screen.width / 2 - 100, y, 200, 20).build());
    }
}
