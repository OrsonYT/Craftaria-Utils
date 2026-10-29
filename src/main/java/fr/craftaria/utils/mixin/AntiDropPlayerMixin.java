package fr.craftaria.utils.mixin;

import fr.craftaria.utils.protection.AntiDrop;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class AntiDropPlayerMixin {
    @Inject(method="dropSelectedItem",at=@At("HEAD"),cancellable=true)
    private void craftariaUtils$protectDrop(boolean entireStack,CallbackInfoReturnable<Boolean> cir) {
        if(AntiDrop.isProtected(((ClientPlayerEntity)(Object)this).getMainHandStack())) {
            AntiDrop.alert(false); cir.setReturnValue(false);
        }
    }
    @Inject(method="closeHandledScreen",at=@At("HEAD"),cancellable=true)
    private void craftariaUtils$protectCursorOnClose(CallbackInfo ci) {
        if(AntiDrop.isProtected(((ClientPlayerEntity)(Object)this).currentScreenHandler.getCursorStack())) {
            AntiDrop.alert(true); ci.cancel();
        }
    }
}
