package fr.craftaria.utils.mixin;

import fr.craftaria.utils.protection.AntiDrop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class AntiDropHandledScreenMixin {
    @Inject(method="close",at=@At("HEAD"),cancellable=true)
    private void craftariaUtils$placeProtectedCursorFirst(CallbackInfo ci) {
        var player=MinecraftClient.getInstance().player;
        if(player!=null && AntiDrop.isProtected(player.currentScreenHandler.getCursorStack())) {
            AntiDrop.alert(true); ci.cancel();
        }
    }
}
