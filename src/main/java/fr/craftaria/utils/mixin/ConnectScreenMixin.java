package fr.craftaria.utils.mixin;

import fr.craftaria.utils.screen.CraftariaTitleScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {
    @Shadow @Final private Screen parent;
    @Shadow private Text status;
    protected ConnectScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void craftariaUtils$positionCancel(CallbackInfo ci) {
        if (parent instanceof CraftariaTitleScreen) {
            float s = Math.min(width/960f, height/540f);
            for (var child : children()) if (child instanceof ButtonWidget button) {
                button.setX(Math.round(64*s));
                button.setY(Math.round(302*s));
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void craftariaUtils$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (parent instanceof CraftariaTitleScreen menu) {
            menu.renderConnecting(context, width, height, status);
            super.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
    }
}
