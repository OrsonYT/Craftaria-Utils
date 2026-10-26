package fr.craftaria.utils.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PlayerRequestButton extends ClickableWidget {
    private final PlayerListEntry player;
    private final Runnable action;
    public PlayerRequestButton(int x,int y,int width,PlayerListEntry player,Text tooltip,Runnable action) {
        super(x,y,width,28,Text.literal(player.getProfile().name()));
        this.player=player;
        this.action=action;
        setTooltip(Tooltip.of(tooltip));
    }
    @Override protected void renderWidget(DrawContext context,int mouseX,int mouseY,float delta) {
        int x=getX(),y=getY(),right=x+getWidth();
        boolean hover=isHovered()||isFocused();
        context.fill(x,y,right,y+getHeight(),hover?0xDE29313A:0xB0101720);
        context.fill(x,y+getHeight()-1,right,y+getHeight(),hover?0xFFFFD383:0x806E6554);
        PlayerSkinDrawer.draw(context,player.getSkinTextures(),x+4,y+4,20);
        var font=MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(font,font.trimToWidth(getMessage().getString(),getWidth()-50),x+31,y+10,0xFFF0E9DA);
        context.drawTextWithShadow(font,">",right-12,y+10,hover?0xFFFFD383:0xFF99968F);
    }
    @Override public void onClick(Click click,boolean doubled) { action.run(); }
    @Override public boolean keyPressed(KeyInput input) {
        if (active && isFocused() && (input.key()==GLFW.GLFW_KEY_ENTER || input.key()==GLFW.GLFW_KEY_SPACE || input.key()==GLFW.GLFW_KEY_KP_ENTER)) {
            playDownSound(MinecraftClient.getInstance().getSoundManager()); action.run(); return true;
        }
        return false;
    }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
