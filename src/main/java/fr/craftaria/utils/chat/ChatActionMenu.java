package fr.craftaria.utils.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Scrollable command popup shared by the teleportation and general buttons. */
public class ChatActionMenu {
    public record Action(String id, Text label, ItemStack icon, Text description, String hint) {
        public static Action command(String command, String key, Item icon) {
            return new Action(command,Text.translatable(key),icon.getDefaultStack(),Text.translatable(key+".description"),"/"+command);
        }
    }
    private static final int ROW_HEIGHT=22;
    private final int buttonX,buttonWidth;
    private final Text title;
    private final Item buttonIcon;
    private final Supplier<List<Action>> source;
    private final Consumer<String> action;
    private int buttonY,popupX,popupY,popupWidth,popupHeight,visibleRows,offset;
    private List<Action> entries=List.of();
    private boolean open;
    public ChatActionMenu(int x,int width,Text title,Item icon,Supplier<List<Action>> source,Consumer<String> action) {
        buttonX=x; buttonWidth=width; this.title=title; buttonIcon=icon; this.source=source; this.action=action;
    }
    public boolean isOpen() { return open; }
    public void close() { open=false; }
    public void layout(int screenWidth,int fieldY) {
        entries=source.get();
        buttonY=Math.max(4,fieldY-26);
        popupWidth=Math.min(296,screenWidth-8);
        popupX=Math.max(4,Math.min(buttonX,screenWidth-popupWidth-4));
        // Suggestions are rendered immediately above the chat field. Keep our popup below them,
        // using the area between the field and the bottom buttons instead of covering suggestions.
        int availableHeight=Math.max(22,fieldY-buttonY-4);
        visibleRows=Math.max(1,Math.min(entries.size(),(availableHeight-22)/ROW_HEIGHT));
        offset=Math.clamp(offset,0,Math.max(0,entries.size()-visibleRows));
        popupHeight=22+visibleRows*ROW_HEIGHT;
        popupY=Math.max(2,buttonY-popupHeight-3);
    }
    public void render(DrawContext context,int mouseX,int mouseY) {
        var font=MinecraftClient.getInstance().textRenderer;
        context.createNewRootLayer();
        boolean hover=inside(mouseX,mouseY,buttonX,buttonY,buttonWidth,20);
        context.fill(buttonX,buttonY,buttonX+buttonWidth,buttonY+20,0xDC101720);
        border(context,buttonX,buttonY,buttonWidth,20,hover||open?0xFFFFD383:0xFF756B57);
        context.drawItem(buttonIcon.getDefaultStack(),buttonX+2,buttonY+2);
        context.drawTextWithShadow(font,font.trimToWidth(title.getString(),buttonWidth-26),buttonX+23,buttonY+6,0xFFF0E9DA);
        if (!open) return;
        context.fill(popupX,popupY,popupX+popupWidth,popupY+popupHeight,0xF2101720);
        border(context,popupX,popupY,popupWidth,popupHeight,0xFFCFAD6D);
        context.drawTextWithShadow(font,title,popupX+7,popupY+6,0xFFFFD383);
        if (entries.size()>visibleRows) {
            String range=(offset+1)+"-"+(offset+visibleRows)+" / "+entries.size();
            context.drawTextWithShadow(font,range,popupX+popupWidth-8-font.getWidth(range),popupY+6,0xFFB8B4AA);
        }
        context.fill(popupX+5,popupY+18,popupX+popupWidth-5,popupY+19,0xFF514D45);
        Text tooltip=null;
        for (int i=0;i<visibleRows && offset+i<entries.size();i++) {
            Action entry=entries.get(offset+i);
            int y=popupY+20+i*ROW_HEIGHT;
            if (inside(mouseX,mouseY,popupX+2,y,popupWidth-4,ROW_HEIGHT)) {
                context.fill(popupX+2,y,popupX+popupWidth-2,y+ROW_HEIGHT,0xFF29313A);
                tooltip=entry.description().copy().append("\n"+entry.hint());
            }
            context.drawItem(entry.icon(),popupX+6,y+3);
            String hint=font.trimToWidth(entry.hint(),96);
            int labelWidth=popupWidth-44-font.getWidth(hint);
            context.drawTextWithShadow(font,font.trimToWidth(entry.label().getString(),labelWidth),popupX+28,y+7,0xFFF0E9DA);
            context.drawTextWithShadow(font,hint,popupX+popupWidth-9-font.getWidth(hint),y+7,0xFFB8B4AA);
        }
        if (entries.size()>visibleRows) {
            int track=visibleRows*ROW_HEIGHT;
            int thumb=Math.max(8,track*visibleRows/entries.size());
            int y=popupY+20+(track-thumb)*offset/(entries.size()-visibleRows);
            context.fill(popupX+popupWidth-3,y,popupX+popupWidth-2,y+thumb,0xFFCFAD6D);
        }
        if (tooltip!=null) context.drawTooltip(font,tooltip,mouseX,mouseY);
    }
    public boolean mouseClicked(double mouseX,double mouseY,int button) {
        if (inside(mouseX,mouseY,buttonX,buttonY,buttonWidth,20)) {
            if (button==0) { open=!open; clickSound(); }
            return true;
        }
        if (!open) return false;
        if (covers(mouseX,mouseY)) {
            if (button==0 && mouseY>=popupY+20) {
                int row=(int)(mouseY-popupY-20)/ROW_HEIGHT;
                if (row<visibleRows && offset+row<entries.size()) {
                    open=false; clickSound(); action.accept(entries.get(offset+row).id());
                }
            }
            return true;
        }
        open=false; return false;
    }
    public boolean mouseScrolled(double mouseX,double mouseY,double amount) {
        if (!covers(mouseX,mouseY)) return false;
        if (amount!=0) offset=Math.clamp(offset+(amount>0?-1:1),0,Math.max(0,entries.size()-visibleRows));
        return true;
    }
    public boolean covers(double mouseX,double mouseY) { return open && inside(mouseX,mouseY,popupX,popupY,popupWidth,popupHeight); }
    private static boolean inside(double mx,double my,int x,int y,int w,int h) { return mx>=x && mx<x+w && my>=y && my<y+h; }
    private static void clickSound() { MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK,1f)); }
    private static void border(DrawContext c,int x,int y,int w,int h,int color) {
        c.fill(x,y,x+w,y+1,color); c.fill(x,y+h-1,x+w,y+h,color);
        c.fill(x,y+1,x+1,y+h-1,color); c.fill(x+w-1,y+1,x+w,y+h-1,color);
    }
}
