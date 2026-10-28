package fr.craftaria.utils.screen;

import fr.craftaria.utils.CraftariaUtilsClient;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import java.util.*;

/** Uses the server's live player list and Minecraft's skin cache. */
public final class TeleportPlayersScreen extends CraftariaScreen {
    public enum Mode {
        TPA("tpa"), TPAHERE("tpahere");
        public final String command;
        Mode(String command) { this.command=command; }
    }
    private final Screen parent;
    private Mode mode;
    private TextFieldWidget search;
    private String query="";
    private List<PlayerListEntry> players=List.of();
    private final List<PlayerRequestButton> buttons=new ArrayList<>();
    private List<String> roster=List.of();
    private SettingsButton previous,next;
    private int left,contentWidth,page,pageSize=1,pageCount=1,filteredCount,ticks;
    private boolean sent;
    private boolean error;

    public TeleportPlayersScreen(Screen parent,Mode mode) {
        super(Text.translatable("craftaria_utils.teleport.title"));
        this.parent=parent; this.mode=mode;
    }
    @Override protected void init() {
        contentWidth=Math.min(720,width-28); left=(width-contentWidth)/2;
        int half=(contentWidth-8)/2;
        addDrawableChild(new SettingsButton(left,41,half,() -> modeLabel(Mode.TPA),() -> { mode=Mode.TPA; refreshRows(); }));
        addDrawableChild(new SettingsButton(left+half+8,41,half,() -> modeLabel(Mode.TPAHERE),() -> { mode=Mode.TPAHERE; refreshRows(); }));
        search=new TextFieldWidget(textRenderer,left,73,contentWidth,22,Text.translatable("craftaria_utils.teleport.search"));
        search.setMaxLength(64);
        search.setPlaceholder(Text.translatable("craftaria_utils.teleport.search"));
        search.setText(query);
        search.setChangedListener(value -> { query=value; page=0; error=false; refreshRows(); });
        addDrawableChild(search);
        addDrawableChild(new SettingsButton(left,height-28,60,() -> Text.translatable("gui.back"),this::close));
        previous=addDrawableChild(new SettingsButton(width/2-84,height-28,28,() -> Text.literal("<"),() -> { page--; refreshRows(); }));
        next=addDrawableChild(new SettingsButton(width/2+56,height-28,28,() -> Text.literal(">"),() -> { page++; refreshRows(); }));
        buttons.clear();
        updatePlayers();
        setInitialFocus(search);
    }
    private Text modeLabel(Mode selected) {
        return Text.literal(mode==selected?"> ":"").append(Text.translatable("craftaria_utils.teleport."+selected.command));
    }
    private void updatePlayers() {
        if (client.getNetworkHandler()==null) players=List.of();
        else players=client.getNetworkHandler().getPlayerList().stream()
                .filter(entry -> client.player==null || !entry.getProfile().getId().equals(client.player.getUuid()))
                .sorted(Comparator.comparing(entry -> entry.getProfile().getName(),String.CASE_INSENSITIVE_ORDER)).toList();
        roster=players.stream().map(entry -> entry.getProfile().getId()+":"+entry.getProfile().getName()).toList();
        refreshRows();
    }
    private void refreshRows() {
        for (var button:buttons) remove(button);
        buttons.clear();
        String filter=query.strip().toLowerCase(Locale.ROOT);
        List<PlayerListEntry> filtered=players.stream().filter(entry -> entry.getProfile().getName().toLowerCase(Locale.ROOT).contains(filter)).toList();
        filteredCount=filtered.size();
        int columns=contentWidth>=440?2:1;
        int rows=Math.max(1,(height-146)/34);
        pageSize=columns*rows;
        pageCount=Math.max(1,(filtered.size()+pageSize-1)/pageSize);
        page=Math.clamp(page,0,pageCount-1);
        previous.active=page>0; next.active=page+1<pageCount;
        previous.visible=next.visible=pageCount>1;
        int tileWidth=(contentWidth-8*(columns-1))/columns;
        for (int i=0;i<pageSize && page*pageSize+i<filtered.size();i++) {
            PlayerListEntry entry=filtered.get(page*pageSize+i);
            String name=entry.getProfile().getName();
            var button=addDrawableChild(new PlayerRequestButton(left+(i%columns)*(tileWidth+8),112+(i/columns)*34,tileWidth,entry,
                    Text.translatable("craftaria_utils.teleport.request_hint","/"+mode.command+" "+name),() -> request(entry)));
            buttons.add(button);
        }
    }
    private void request(PlayerListEntry target) {
        if (sent) return;
        if (client.getNetworkHandler()==null || client.getNetworkHandler().getPlayerListEntry(target.getProfile().getId())==null) {
            error=true; updatePlayers(); return;
        }
        String name=target.getProfile().getName();
        if (name.isBlank() || name.chars().anyMatch(Character::isWhitespace)) { error=true; return; }
        sent=true;
        CraftariaUtilsClient.sendCommand(client,mode.command+" "+name);
        close();
    }
    @Override public void tick() {
        if (++ticks%20!=0) return;
        if (client.getNetworkHandler()==null) { close(); return; }
        List<String> current=client.getNetworkHandler().getPlayerList().stream()
                .filter(entry -> client.player==null || !entry.getProfile().getId().equals(client.player.getUuid()))
                .sorted(Comparator.comparing(entry -> entry.getProfile().getName(),String.CASE_INSENSITIVE_ORDER))
                .map(entry -> entry.getProfile().getId()+":"+entry.getProfile().getName()).toList();
        if (!current.equals(roster)) updatePlayers();
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontal,double vertical) {
        if (mouseY>=105 && mouseY<height-36 && vertical!=0) {
            page+=vertical>0?-1:1; refreshRows(); return true;
        }
        return super.mouseScrolled(mouseX,mouseY,horizontal,vertical);
    }
    @Override public void renderBackground(DrawContext context,int mouseX,int mouseY,float delta) { }
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        WallpaperManager.draw(context,width,height);
        context.fill(0,0,width,height,0xBF0A1017);
        MenuTextButton.drawLabel(context,title,left,12,1.5f,false,true);
        context.drawTextWithShadow(textRenderer,Text.translatable(error?"craftaria_utils.teleport.player_left":"craftaria_utils.teleport.consent"),left,31,error?0xFFFF8888:0xFFB8B4AA);
        context.drawTextWithShadow(textRenderer,Text.translatable("craftaria_utils.teleport.player_count",filteredCount),left,100,0xFFB8B4AA);
        if (filteredCount==0) context.drawCenteredTextWithShadow(textRenderer,Text.translatable("craftaria_utils.teleport.no_players"),width/2,height/2+22,0xFFC5C1B8);
        context.drawCenteredTextWithShadow(textRenderer,Text.literal((page+1)+" / "+pageCount),width/2,height-21,0xFFF0E9DA);
        super.render(context,mouseX,mouseY,delta);
    }
    @Override public void close() { client.setScreen(client.player!=null && client.getNetworkHandler()!=null ? parent : new TitleScreen()); }
}
