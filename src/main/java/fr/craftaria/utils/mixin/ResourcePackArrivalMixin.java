package fr.craftaria.utils.mixin;

import fr.craftaria.utils.ServerArrival;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ResourcePackArrivalMixin {
    @Shadow @Final protected ClientConnection connection;
    @Inject(method="sendPacket",at=@At("HEAD"))
    private void craftariaUtils$packStatus(Packet<?> packet, CallbackInfo ci) {
        if(packet instanceof ResourcePackStatusC2SPacket status) ServerArrival.resourcePackStatus(connection,status);
    }
}
