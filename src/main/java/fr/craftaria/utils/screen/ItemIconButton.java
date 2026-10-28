package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ItemIconButton extends ClickableWidget {
    private final ItemStack icon;
    private final boolean selected;
    private final Runnable action;
    public ItemIconButton(int x,int y,ItemStack icon,boolean selected,Text tooltip,Runnable action) {
        super(x,y,24,24,tooltip); this.icon=icon; this.selected=selected; this.action=action;
        setTooltip(Tooltip.of(tooltip));
    }
    @Override protected void renderWidget(DrawContext c,int mouseX,int mouseY,float delta) {
        int x=getX(),y=getY();
        c.fill(x,y,x+24,y+24,0xC0101720);
        int color=selected?0xFFFFD383:isHovered()||isFocused()?0xFFE4D3AC:0xFF514D45;
        c.fill(x,y,x+24,y+1,color); c.fill(x,y+23,x+24,y+24,color);
        c.fill(x,y+1,x+1,y+23,color); c.fill(x+23,y+1,x+24,y+23,color);
        c.drawItem(icon,x+4,y+4);
    }
    @Override public void onClick(double mouseX, double mouseY) { action.run(); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (active && isFocused() && (keyCode==GLFW.GLFW_KEY_ENTER || keyCode==GLFW.GLFW_KEY_SPACE || keyCode==GLFW.GLFW_KEY_KP_ENTER)) {
            playDownSound(MinecraftClient.getInstance().getSoundManager()); action.run(); return true;
        }
        return false;
    }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
