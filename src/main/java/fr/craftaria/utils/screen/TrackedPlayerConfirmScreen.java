package fr.craftaria.utils.screen;

import fr.craftaria.utils.TrackedPlayerSkins;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/** Confirmation card shown before adding a watched username. */
public final class TrackedPlayerConfirmScreen extends CraftariaScreen {
    private final Screen parent;
    private final String playerName;
    private final Runnable confirm;

    public TrackedPlayerConfirmScreen(Screen parent, String playerName, Runnable confirm) {
        super(Text.translatable("craftaria_utils.tracker.confirm_title"));
        this.parent=parent;
        this.playerName=playerName;
        this.confirm=confirm;
        TrackedPlayerSkins.request(playerName);
    }

    @Override protected void init() {
        int center=width/2, y=height/2+42;
        addDrawableChild(new SettingsButton(center-104,y,96,() -> Text.translatable("gui.yes"),() -> {
            confirm.run(); client.setScreen(parent);
        }));
        addDrawableChild(new SettingsButton(center+8,y,96,() -> Text.translatable("gui.no"),() -> client.setScreen(parent)));
    }

    @Override public void renderBackground(DrawContext context,int mouseX,int mouseY,float delta) { }
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        WallpaperManager.draw(context,width,height);
        context.fill(0,0,width,height,0xC80A1017);
        int boxWidth=300, boxHeight=128, x=(width-boxWidth)/2, y=(height-boxHeight)/2;
        context.fill(x,y,x+boxWidth,y+boxHeight,0xEF101720);
        context.fill(x,y,x+boxWidth,y+2,0xFFFFD383);
        context.fill(x,y+boxHeight-1,x+boxWidth,y+boxHeight,0xFF756B57);
        context.drawCenteredTextWithShadow(textRenderer,title,width/2,y+12,0xFFFFD383);
        var skin=TrackedPlayerSkins.get(playerName);
        if (skin!=null) PlayerSkinDrawer.draw(context,skin,width/2-36,y+42,32);
        else context.drawItem(Items.PLAYER_HEAD.getDefaultStack(),width/2-32,y+46);
        context.drawCenteredTextWithShadow(textRenderer,Text.literal(playerName),width/2,y+79,0xFFF0E9DA);
        context.drawCenteredTextWithShadow(textRenderer,Text.translatable("craftaria_utils.tracker.confirm"),width/2,y+95,0xFFB8B4AA);
        super.render(context,mouseX,mouseY,delta);
    }
}
