package fr.craftaria.utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.craftaria.utils.screen.CraftariaConfigScreen;
import fr.craftaria.utils.screen.OptionsImageButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addBody(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"))
    private void craftariaUtils$addSettings(CallbackInfo ci, @Local GridWidget.Adder adder) {
        // Let Minecraft lay out this full-width row together with its own options.
        adder.add(new OptionsImageButton(() -> client.setScreen(new CraftariaConfigScreen((Screen)(Object)this))), 2);
    }
}
