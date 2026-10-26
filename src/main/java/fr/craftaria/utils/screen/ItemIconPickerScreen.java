package fr.craftaria.utils.screen;

import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;

public final class ItemIconPickerScreen extends CraftariaScreen {
    private final Screen parent;
    private final String selected;
    private final Consumer<String> choose;
    private final List<ItemIconButton> icons=new ArrayList<>();
    private final List<Item> allItems;
    private SettingsButton previous,next;
    private String query="";
    private int left,contentWidth,page,pageCount=1,count;
    public ItemIconPickerScreen(Screen parent,String selected,Consumer<String> choose) {
        super(Text.translatable("craftaria_utils.quick_homes.choose_icon"));
        this.parent=parent; this.selected=selected; this.choose=choose;
        allItems=Registries.ITEM.stream().filter(item -> item!=Items.AIR)
                .sorted(Comparator.comparing(item -> Registries.ITEM.getId(item).toString())).toList();
    }
    @Override protected void init() {
        contentWidth=Math.min(780,width-28); left=(width-contentWidth)/2;
        var search=addDrawableChild(new TextFieldWidget(textRenderer,left,42,contentWidth,22,Text.translatable("craftaria_utils.quick_homes.search_icon")));
        search.setPlaceholder(Text.translatable("craftaria_utils.quick_homes.search_icon"));
        search.setText(query);
        search.setChangedListener(value -> { query=value; page=0; refreshIcons(); });
        addDrawableChild(new SettingsButton(left,height-28,60,() -> Text.translatable("gui.back"),this::close));
        previous=addDrawableChild(new SettingsButton(width/2-84,height-28,28,() -> Text.literal("<"),() -> { page--; refreshIcons(); }));
        next=addDrawableChild(new SettingsButton(width/2+56,height-28,28,() -> Text.literal(">"),() -> { page++; refreshIcons(); }));
        icons.clear(); refreshIcons(); setInitialFocus(search);
    }
    private void refreshIcons() {
        for (var icon:icons) remove(icon);
        icons.clear();
        String filter=query.strip().toLowerCase(Locale.ROOT);
        List<Item> filtered=allItems.stream().filter(item -> Registries.ITEM.getId(item).toString().contains(filter)
                || item.getName().getString().toLowerCase(Locale.ROOT).contains(filter)).toList();
        count=filtered.size();
        int columns=Math.max(1,contentWidth/28),rows=Math.max(1,(height-114)/28),size=columns*rows;
        pageCount=Math.max(1,(count+size-1)/size); page=Math.clamp(page,0,pageCount-1);
        previous.active=page>0; next.active=page+1<pageCount;
        for (int i=0;i<size && page*size+i<count;i++) {
            Item item=filtered.get(page*size+i);
            String id=Registries.ITEM.getId(item).toString();
            Text tooltip=item.getName().copy().append("\n"+id);
            icons.add(addDrawableChild(new ItemIconButton(left+(i%columns)*28,76+(i/columns)*28,item.getDefaultStack(),id.equals(selected),tooltip,() -> {
                choose.accept(id); client.setScreen(parent);
            })));
        }
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        if (y>=70 && y<height-36 && vertical!=0) { page+=vertical>0?-1:1; refreshIcons(); return true; }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public void renderBackground(DrawContext c,int x,int y,float delta) { }
    @Override public void render(DrawContext c,int x,int y,float delta) {
        WallpaperManager.draw(c,width,height); c.fill(0,0,width,height,0xBF0A1017);
        MenuTextButton.drawLabel(c,title,left,13,1.4f,false,true);
        if (count==0) c.drawCenteredTextWithShadow(textRenderer,Text.translatable("craftaria_utils.quick_homes.no_items"),width/2,height/2,0xFFC5C1B8);
        c.drawCenteredTextWithShadow(textRenderer,Text.literal((page+1)+" / "+pageCount),width/2,height-21,0xFFF0E9DA);
        super.render(c,x,y,delta);
    }
    @Override public void close() { client.setScreen(parent); }
}
