package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.config.QuickHome;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.List;

/** The same settings page is accessible from the mod settings and the chat popup. */
public final class QuickHomesScreen extends CraftariaScreen {
    private final Screen parent;
    private String commandDraft="",labelDraft="",icon="minecraft:red_bed",editing;
    private TextFieldWidget commandField,labelField;
    private SettingsButton save;
    private String status;
    private int left,contentWidth,page,pageSize=1,pageCount=1;
    public QuickHomesScreen(Screen parent) { super(Text.translatable("craftaria_utils.quick_homes.title")); this.parent=parent; }
    public void editHome(QuickHome home) { edit(home); }
    @Override protected void init() {
        contentWidth=Math.min(680,width-28); left=(width-contentWidth)/2;
        int commandWidth=Math.max(100,contentWidth/3-8);
        int labelWidth=contentWidth-commandWidth-54-100;
        commandField=addDrawableChild(new TextFieldWidget(textRenderer,left,60,commandWidth,22,Text.translatable("craftaria_utils.quick_homes.command")));
        commandField.setPlaceholder(Text.translatable("craftaria_utils.quick_homes.command"));
        commandField.setMaxLength(32); commandField.setText(commandDraft);
        commandField.setChangedListener(value -> commandDraft=value);
        labelField=addDrawableChild(new TextFieldWidget(textRenderer,left+commandWidth+8,60,labelWidth,22,Text.translatable("craftaria_utils.quick_homes.friendly_name")));
        labelField.setPlaceholder(Text.translatable("craftaria_utils.quick_homes.friendly_name"));
        labelField.setMaxLength(32); labelField.setText(labelDraft);
        labelField.setChangedListener(value -> labelDraft=value);
        int iconX=left+commandWidth+8+labelWidth+8;
        addDrawableChild(new ItemIconButton(iconX,59,QuickHome.iconStack(icon),false,
                Text.translatable("craftaria_utils.quick_homes.choose_icon"),() -> client.setScreen(new ItemIconPickerScreen(this,icon,value -> icon=value))));
        save=addDrawableChild(new SettingsButton(left+contentWidth-100,60,100,
                () -> Text.translatable(editing==null?"craftaria_utils.quick_homes.save":"craftaria_utils.quick_homes.update"),this::saveHome));
        List<QuickHome> homes=ModConfig.get().quickHomes;
        pageSize=Math.max(1,(height-174)/32); pageCount=Math.max(1,(homes.size()+pageSize-1)/pageSize); page=Math.clamp(page,0,pageCount-1);
        for (int i=0;i<pageSize && page*pageSize+i<homes.size();i++) {
            QuickHome home=homes.get(page*pageSize+i); int y=132+i*32;
            addDrawableChild(new ItemIconButton(left,y,QuickHome.iconStack(home.icon),false,Text.literal(home.label()+" — /home "+home.name),() -> edit(home)));
            addDrawableChild(new SettingsButton(left+30,y+1,contentWidth-110,() -> Text.literal(home.label()+"  →  /home "+home.name),() -> edit(home)));
            addDrawableChild(new SettingsButton(left+contentWidth-72,y+1,72,() -> Text.translatable("craftaria_utils.quick_homes.delete"),() -> {
                ModConfig.get().quickHomes.remove(home);
                if (home.name.equals(editing)) resetDraft();
                ModConfig.save(); clearAndInit();
            }));
        }
        addDrawableChild(new SettingsButton(left,height-28,60,() -> Text.translatable("gui.back"),this::close));
        addDrawableChild(new SettingsButton(left+contentWidth-60,height-28,60,() -> Text.translatable("craftaria_utils.quick_homes.new"),() -> { resetDraft(); clearAndInit(); }));
        var previous=addDrawableChild(new SettingsButton(width/2-84,height-28,28,() -> Text.literal("<"),() -> { page--; clearAndInit(); }));
        var next=addDrawableChild(new SettingsButton(width/2+56,height-28,28,() -> Text.literal(">"),() -> { page++; clearAndInit(); }));
        previous.active=page>0; next.active=page+1<pageCount; previous.visible=next.visible=pageCount>1;
        setInitialFocus(commandField);
    }
    private void edit(QuickHome home) { commandDraft=home.name; labelDraft=home.label(); icon=home.icon; editing=home.name; status=null; clearAndInit(); }
    private void resetDraft() { commandDraft=""; labelDraft=""; icon="minecraft:red_bed"; editing=null; status=null; }
    private void saveHome() {
        String name=commandDraft.strip();
        String label=labelDraft.strip();
        if (!QuickHome.validName(name)) { status="invalid"; return; }
        List<QuickHome> homes=ModConfig.get().quickHomes;
        if (homes.stream().anyMatch(home -> home.name.equalsIgnoreCase(name) && (editing==null || !home.name.equalsIgnoreCase(editing)))) {
            status="duplicate"; return;
        }
        QuickHome target=editing==null?null:homes.stream().filter(home -> home.name.equals(editing)).findFirst().orElse(null);
        if (target==null) { homes.add(new QuickHome(name,label,icon)); page=(homes.size()-1)/pageSize; }
        else { target.name=name; target.displayName=label; target.icon=icon; }
        ModConfig.save(); resetDraft(); status="saved"; clearAndInit();
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        if (y>=122 && y<height-36 && vertical!=0) { page+=vertical>0?-1:1; clearAndInit(); return true; }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public void renderBackground(DrawContext c,int x,int y,float delta) { }
    @Override public void render(DrawContext c,int x,int y,float delta) {
        WallpaperManager.draw(c,width,height); c.fill(0,0,width,height,0xBF0A1017);
        MenuTextButton.drawLabel(c,title,left,13,1.35f,false,true);
        c.drawTextWithShadow(textRenderer,textRenderer.trimToWidth(Text.translatable("craftaria_utils.quick_homes.description").getString(),contentWidth),left,35,0xFFC5C1B8);
        String hint=status==null?"hint":status;
        c.drawTextWithShadow(textRenderer,textRenderer.trimToWidth(Text.translatable("craftaria_utils.quick_homes."+hint).getString(),contentWidth),left,94,
                "invalid".equals(status)||"duplicate".equals(status)?0xFFFF8888:0xFFCFAD6D);
        c.drawTextWithShadow(textRenderer,Text.translatable("craftaria_utils.quick_homes.list",ModConfig.get().quickHomes.size()),left,115,0xFFB8B4AA);
        if (ModConfig.get().quickHomes.isEmpty()) c.drawTextWithShadow(textRenderer,Text.translatable("craftaria_utils.quick_homes.empty"),left,139,0xFF99968F);
        c.drawCenteredTextWithShadow(textRenderer,Text.literal((page+1)+" / "+pageCount),width/2,height-21,0xFFF0E9DA);
        save.active=QuickHome.validName(commandDraft.strip());
        super.render(c,x,y,delta);
    }
    @Override public void close() { client.setScreen(parent); }
}
