package fr.craftaria.utils.protection;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/** Guards ordinary client inventory actions before vanilla mutates or sends them. */
public final class AntiDrop {
    private static long until, lastSound;
    private static Text message;
    private AntiDrop() { }

    public static boolean isProtected(ItemStack stack) {
        return ModConfig.get().antiDropEnabled && !stack.isEmpty()
                && ModConfig.get().protectedItems.contains(Registries.ITEM.getId(stack.getItem()).toString());
    }

    public static boolean blocksClick(PlayerEntity player,int slotId,int button,SlotActionType action) {
        if (!ModConfig.get().antiDropEnabled) return false;
        var handler=player.currentScreenHandler;
        var slot=slotId>=0 && slotId<handler.slots.size()?handler.slots.get(slotId):null;
        boolean foreign=slot!=null && slot.inventory!=player.getInventory();
        boolean protectedSource=slot!=null && isProtected(slot.getStack());
        boolean protectedCursor=isProtected(handler.getCursorStack());
        var destination=slot==null?AntiDropPolicy.Destination.OUTSIDE:
                foreign?AntiDropPolicy.Destination.CONTAINER:AntiDropPolicy.Destination.PLAYER;
        boolean protectedSwap=button>=0 && button<9 && isProtected(player.getInventory().getStack(button))
                || button==40 && isProtected(player.getOffHandStack());
        return AntiDropPolicy.blocks(action.name(),destination,protectedSource,protectedCursor,protectedSwap,handler instanceof PlayerScreenHandler);
    }

    public static void alert(boolean cursorMustBePlaced) {
        long now=System.currentTimeMillis();
        message=Text.translatable(cursorMustBePlaced?"craftaria_utils.antidrop.place_first":"craftaria_utils.antidrop.blocked");
        until=now+3000;
        if(now-lastSound>650) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_PLACE,1.4f,1f));
            lastSound=now;
        }
    }

    public static void render(DrawContext c) {
        var client=MinecraftClient.getInstance();
        if(client.player==null) { message=null; return; }
        if(message==null || System.currentTimeMillis()>=until) return;
        int width=Math.min(c.getScaledWindowWidth()-16,Math.max(220,client.textRenderer.getWidth(message)+24));
        int x=(c.getScaledWindowWidth()-width)/2,y=Math.min(110,c.getScaledWindowHeight()/4);
        c.draw(); c.getMatrices().push(); c.getMatrices().translate(0,0,2000);
        c.fill(x,y,x+width,y+30,0xF01B1418);
        c.fill(x,y,x+width,y+2,0xFFFF7777);
        c.drawCenteredTextWithShadow(client.textRenderer,client.textRenderer.trimToWidth(message.getString(),width-16),x+width/2,y+11,0xFFFFC9C9);
        c.draw(); c.getMatrices().pop();
    }
}
