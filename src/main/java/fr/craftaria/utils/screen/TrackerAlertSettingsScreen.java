package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.function.BooleanSupplier;

public final class TrackerAlertSettingsScreen extends CraftariaScreen {
    private final Screen parent;
    private int left,contentWidth;
    public TrackerAlertSettingsScreen(Screen parent) {
        super(Text.translatable("craftaria_utils.tracker.alerts")); this.parent=parent;
    }
    @Override protected void init() {
        contentWidth=Math.min(440,width-28); left=(width-contentWidth)/2;
        int y=Math.max(74,height/2-45);
        addToggle(y,"join",()->ModConfig.get().trackerNotifyJoin,()->ModConfig.get().trackerNotifyJoin=!ModConfig.get().trackerNotifyJoin);
        addToggle(y+32,"leave",()->ModConfig.get().trackerNotifyLeave,()->ModConfig.get().trackerNotifyLeave=!ModConfig.get().trackerNotifyLeave);
        addToggle(y+64,"initial",()->ModConfig.get().trackerNotifyAlreadyOnline,()->ModConfig.get().trackerNotifyAlreadyOnline=!ModConfig.get().trackerNotifyAlreadyOnline);
        addDrawableChild(new SettingsButton(left,height-28,contentWidth,()->Text.translatable("gui.back"),this::close));
    }
    private void addToggle(int y,String key,BooleanSupplier value,Runnable change) {
        addDrawableChild(new SettingsButton(left,y,contentWidth,
                ()->Text.translatable("craftaria_utils.tracker.notify_"+key,Text.translatable(value.getAsBoolean()?"options.on":"options.off")),()->{
            change.run(); ModConfig.save();
        }));
    }
    @Override public void renderBackground(DrawContext c,int x,int y,float delta) { }
    @Override public void render(DrawContext c,int x,int y,float delta) {
        WallpaperManager.draw(c,width,height); c.fill(0,0,width,height,0xBF0A1017);
        MenuTextButton.drawLabel(c,title,left,16,1.3f,false,true);
        c.drawTextWrapped(textRenderer,Text.translatable("craftaria_utils.tracker.alerts_hint"),left,40,contentWidth,0xFFB8B4AA);
        super.render(c,x,y,delta);
    }
    @Override public void close() { client.setScreen(parent); }
}
