package fr.craftaria.utils.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Keeps Minecraft's actual disconnect reason and offers an explicit retry. */
public final class CraftariaConnectionErrorScreen extends CraftariaScreen {
    private final CraftariaTitleScreen menu;
    private final Text reason;

    public CraftariaConnectionErrorScreen(CraftariaTitleScreen menu, Text reason) {
        super(Text.translatable("craftaria_utils.connection.failed"));
        this.menu=menu;
        this.reason=reason;
    }

    @Override protected void init() {
        float scale=Math.min(width/960f,height/540f);
        int left=Math.round(64*scale), buttonWidth=Math.max(160,Math.round(420*scale));
        int buttonY=Math.max(Math.round(325*scale),height-90);
        addDrawableChild(new MenuTextButton(left,buttonY,buttonWidth,26,
                Text.translatable("craftaria_utils.connection.retry"),1.4f*scale,menu::retryConnection));
        addDrawableChild(new MenuTextButton(left,buttonY+34,buttonWidth,26,
                Text.translatable("craftaria_utils.connection.back"),1.4f*scale,this::close));
    }

    @Override public void renderBackground(DrawContext context,int mouseX,int mouseY,float delta) { }

    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        menu.renderScene(context,width,height);
        float scale=Math.min(width/960f,height/540f);
        int left=Math.round(64*scale), top=Math.round(185*scale);
        MenuTextButton.drawLabel(context,title,left,top,2.3f*scale,false,true);
        int textWidth=Math.min(width-left-16,Math.max(180,Math.round(560*scale)));
        int reasonY=top+Math.max(26,Math.round(40*scale));
        context.enableScissor(left,reasonY,left+textWidth,Math.max(reasonY,height-104));
        context.drawTextWrapped(textRenderer,reason,left,reasonY,textWidth,0xFFE2D9C4);
        context.disableScissor();
        super.render(context,mouseX,mouseY,delta);
    }

    @Override public void close() { client.setScreen(menu); }
}
