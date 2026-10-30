package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import fr.craftaria.utils.protection.ProtectedStacks;

/** Selects individual inventory stacks; selection never sends an inventory click. */
public final class AntiDropScreen extends HandledScreen<PlayerScreenHandler> {
    private final Screen parent;
    private final java.util.Set<String> selected=new java.util.LinkedHashSet<>(ModConfig.get().protectedStackCounts.keySet());
    private ButtonWidget protectButton;
    public AntiDropScreen(Screen parent) {
        super(MinecraftClient.getInstance().player.playerScreenHandler,MinecraftClient.getInstance().player.getInventory(),
                Text.translatable("craftaria_utils.settings.antidrop"));
        this.parent=parent;
    }
    @Override protected void init() {
        super.init();
        protectButton=addDrawableChild(ButtonWidget.builder(selectionLabel(),b->{
            // Explicit confirmation replaces unresolved snapshots with the visible selection.
            selected.removeIf(key->findCount(key)==0);
            ModConfig.get().protectedStackCounts.clear();
            for(String key:selected) {
                ModConfig.get().protectedStackCounts.putIfAbsent(key,findCount(key));
            }
            ModConfig.get().protectedItems=new java.util.ArrayList<>(selected);
            ModConfig.get().antiDropEnabled=true;
            ModConfig.save(); close();
        }).dimensions(x,y+backgroundHeight+6,110,20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"),b->close())
                .dimensions(x+114,y+backgroundHeight+6,62,20).build());
    }
    private Text selectionLabel() { return Text.translatable("craftaria_utils.antidrop.confirm",selected.size()); }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button) {
        for(Slot slot:handler.slots) if(mouseX>=x+slot.x && mouseX<x+slot.x+16 && mouseY>=y+slot.y && mouseY<y+slot.y+16) {
            if(button==0 && slot.inventory==client.player.getInventory() && slot.hasStack()) {
                String id=ProtectedStacks.fingerprint(slot.getStack(),client.world.getRegistryManager());
                if(!selected.remove(id)) selected.add(id);
                protectButton.setMessage(selectionLabel());
            }
            return true;
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override protected void onMouseClick(Slot slot,int slotId,int button,SlotActionType action) { }
    @Override protected void drawSlot(DrawContext context,Slot slot) {
        super.drawSlot(context,slot);
        if(slot.hasStack() && slot.inventory==client.player.getInventory()
                && selected.contains(ProtectedStacks.fingerprint(slot.getStack(),client.world.getRegistryManager())))
            context.fill(slot.x,slot.y,slot.x+16,slot.y+16,0x8033DD55);
    }
    @Override protected void drawBackground(DrawContext context,float delta,int mouseX,int mouseY) {
        GuiTextures.draw(context,Identifier.ofVanilla("textures/gui/container/inventory.png"),x,y,0,0,backgroundWidth,backgroundHeight,256,256);
        InventoryScreen.drawEntity(context,x+26,y+8,x+75,y+78,30,.0625f,mouseX,mouseY,client.player);
    }
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        super.render(context,mouseX,mouseY,delta);
        context.drawCenteredTextWithShadow(textRenderer,Text.translatable("craftaria_utils.antidrop.select_hint"),width/2,y-23,0xFFF0E9DA);
        context.drawCenteredTextWithShadow(textRenderer,Text.translatable("craftaria_utils.antidrop.individual"),width/2,y-11,0xFFFFD383);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }
    @Override protected void applyBlur(float delta) { }
    @Override public void close() { client.setScreen(parent); }

    private int findCount(String fingerprint) {
        for(Slot slot:handler.slots) if(slot.inventory==client.player.getInventory() && slot.hasStack()
                && fingerprint.equals(ProtectedStacks.fingerprint(slot.getStack(),client.world.getRegistryManager())))
            return slot.getStack().getCount();
        return 0;
    }
}
