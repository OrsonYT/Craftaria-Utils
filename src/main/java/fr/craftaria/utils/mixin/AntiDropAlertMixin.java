package fr.craftaria.utils.mixin;

import fr.craftaria.utils.protection.AntiDrop;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class AntiDropAlertMixin {
    @Inject(method="renderWithTooltip",at=@At("TAIL"))
    private void craftariaUtils$alert(DrawContext context,int mouseX,int mouseY,float delta,CallbackInfo ci) {
        AntiDrop.render(context);
    }
}
