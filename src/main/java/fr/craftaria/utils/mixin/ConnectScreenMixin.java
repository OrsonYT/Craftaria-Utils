package fr.craftaria.utils.mixin;

import fr.craftaria.utils.screen.CraftariaTitleScreen;
import fr.craftaria.utils.screen.MenuTextButton;
import net.minecraft.client.gui.Drawable;
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
            for (var child : java.util.List.copyOf(children())) if (child instanceof ButtonWidget button) {
                remove(button);
                addDrawableChild(new MenuTextButton(Math.round(64*s), Math.round(302*s),
                        Math.round(280*s), Math.max(20, Math.round(32*s)), Text.translatable("gui.cancel"),
                        2.1f*s, button::onPress));
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void craftariaUtils$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (parent instanceof CraftariaTitleScreen menu) {
            menu.renderConnecting(context, width, height, status);
            // Screen.render draws its background again in 1.21.1, hiding our logo and title.
            for (var child : children()) if (child instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
    }
}
