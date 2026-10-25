package fr.craftaria.utils.mixin;

import fr.craftaria.utils.screen.CraftariaTitleScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void craftariaUtils$returnToCustomMenu(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(Text.translatable("craftaria_utils.menu.return_custom"),
                button -> client.setScreen(new CraftariaTitleScreen()))
                .dimensions(width / 2 - 100, height / 4 + 20, 200, 20).build());
    }
}
