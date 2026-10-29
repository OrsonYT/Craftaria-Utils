package fr.craftaria.utils.mixin;

import fr.craftaria.utils.protection.AntiDrop;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class AntiDropInteractionMixin {
    @Inject(method="clickSlot",at=@At("HEAD"),cancellable=true)
    private void craftariaUtils$protectClick(int syncId,int slotId,int button,SlotActionType action,PlayerEntity player,CallbackInfo ci) {
        if(AntiDrop.blocksClick(player,slotId,button,action)) { AntiDrop.alert(false); ci.cancel(); }
    }
    @Inject(method="dropCreativeStack",at=@At("HEAD"),cancellable=true)
    private void craftariaUtils$protectCreativeDrop(ItemStack stack,CallbackInfo ci) {
        if(AntiDrop.isProtected(stack)) { AntiDrop.alert(false); ci.cancel(); }
    }
}
