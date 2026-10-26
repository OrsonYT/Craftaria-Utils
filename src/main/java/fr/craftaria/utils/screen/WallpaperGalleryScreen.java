package fr.craftaria.utils.screen;

import fr.craftaria.utils.config.ModConfig;
import fr.craftaria.utils.wallpaper.WallpaperCatalog;
import fr.craftaria.utils.wallpaper.WallpaperManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.List;

/** Category headings and thumbnail rows remain in metadata order across pages. */
public final class WallpaperGalleryScreen extends CraftariaScreen {
    private record Row(Text heading, List<String> images, int height) { }
    private final Screen parent;
    private final List<List<Row>> pages = new ArrayList<>();
    private int page;
    private int left, galleryWidth;
    private boolean error;
    private boolean locateSelection = true;

    public WallpaperGalleryScreen(Screen parent) {
        super(Text.translatable("craftaria_utils.settings.wallpaper"));
        this.parent = parent;
    }

    @Override protected void init() {
        galleryWidth = Math.min(900, width-28);
        left = (width-galleryWidth)/2;
        int columns = Math.clamp(galleryWidth/130, 1, 4);
        int gap = 10;
        int tileWidth = (galleryWidth-gap*(columns-1))/columns;
        int availableHeight = Math.max(70, height-122);
        int tileHeight = Math.min(availableHeight-18, Math.clamp(tileWidth*9/16+17, 50, 76));
        pages.clear();
        List<Row> current = new ArrayList<>();
        int used = 0;
        for (WallpaperCatalog.Category category : WallpaperManager.categories()) {
            Text heading = category.name().isEmpty() ? Text.translatable("craftaria_utils.wallpaper.personal") : Text.literal(category.name());
            List<WallpaperCatalog.Entry> entries = category.entries();
            if (entries.isEmpty()) {
                if (used+36>availableHeight && !current.isEmpty()) { pages.add(current); current = new ArrayList<>(); used = 0; }
                current.add(new Row(heading, List.of(), 18));
                current.add(new Row(null, List.of(), 18));
                used += 36;
                continue;
            }
            boolean headingNeeded = true;
            for (int offset=0; offset<entries.size(); offset+=columns) {
                int required = (headingNeeded ? 18 : 0)+tileHeight+6;
                if (used+required>availableHeight && !current.isEmpty()) {
                    pages.add(current); current = new ArrayList<>(); used = 0; headingNeeded = true;
                }
                if (headingNeeded) {
                    current.add(new Row(heading, List.of(), 18)); used += 18; headingNeeded = false;
                }
                List<String> ids = entries.subList(offset, Math.min(offset+columns, entries.size())).stream()
                        .map(WallpaperCatalog.Entry::id).toList();
                current.add(new Row(null, ids, tileHeight+6));
                used += tileHeight+6;
            }
        }
        if (!current.isEmpty()) pages.add(current);
        if (pages.isEmpty()) pages.add(List.of());
        if (locateSelection) {
            for (int i=0; i<pages.size(); i++)
                if (pages.get(i).stream().anyMatch(row -> row.images().contains(ModConfig.get().wallpaper))) page=i;
            locateSelection = false;
        }
        page = Math.clamp(page, 0, pages.size()-1);
        int y = 54;
        for (Row row : pages.get(page)) {
            if (!row.images().isEmpty()) {
                for (int i=0; i<row.images().size(); i++) {
                    String name = row.images().get(i);
                    addDrawableChild(new WallpaperTile(left+i*(tileWidth+gap), y, tileWidth, row.height()-6,
                            name, () -> error = !WallpaperManager.select(name)));
                }
            }
            y += row.height();
        }
        if (pages.size()>1) {
            var previous = addDrawableChild(new SettingsButton(width/2-84, height-58, 28,
                    () -> Text.literal("<"), () -> changePage(-1)));
            previous.active = page>0;
            var next = addDrawableChild(new SettingsButton(width/2+56, height-58, 28,
                    () -> Text.literal(">"), () -> changePage(1)));
            next.active = page+1<pages.size();
        }
        addDrawableChild(new SettingsButton(left, height-28, 60, () -> Text.translatable("gui.back"), this::close));
        int folderWidth = Math.min(130, (galleryWidth-76)/2);
        int refreshWidth = Math.min(90, (galleryWidth-76)/2);
        addDrawableChild(new SettingsButton(left+galleryWidth-refreshWidth-folderWidth-8, height-28, folderWidth,
                () -> Text.translatable("craftaria_utils.wallpaper.folder"), () -> Util.getOperatingSystem().open(WallpaperManager.directory())));
        addDrawableChild(new SettingsButton(left+galleryWidth-refreshWidth, height-28, refreshWidth,
                () -> Text.translatable("craftaria_utils.wallpaper.refresh"), () -> {
            WallpaperManager.refresh(); error = false; clearAndInit();
        }));
    }

    private void changePage(int step) {
        int next = Math.clamp(page+step, 0, pages.size()-1);
        if (next != page) { page = next; clearAndInit(); }
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) { changePage(verticalAmount>0 ? -1 : 1); return true; }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        WallpaperManager.draw(context, width, height);
        context.fill(0, 0, width, height, 0xB50A1017);
        MenuTextButton.drawLabel(context, title, left, 12, 1.5f, false, true);
        boolean failed = error || WallpaperManager.hasLoadError();
        Text description = Text.translatable(failed ? "craftaria_utils.wallpaper.error" : "craftaria_utils.settings.wallpaper.description");
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(description.getString(), galleryWidth),
                left, 35, failed ? 0xFFFF8888 : 0xFFC5C1B8);
        int y = 54;
        for (Row row : pages.get(page)) {
            if (row.heading()!=null) {
                context.drawTextWithShadow(textRenderer, row.heading(), left, y+3, 0xFFFFD383);
                context.fill(left, y+15, left+galleryWidth, y+16, 0x606E6554);
            } else if (row.images().isEmpty()) {
                context.drawTextWithShadow(textRenderer, Text.translatable("craftaria_utils.wallpaper.empty_category"), left+6, y+2, 0xFF99968F);
            }
            y += row.height();
        }
        if (pages.size()>1) context.drawCenteredTextWithShadow(textRenderer, Text.literal((page+1)+" / "+pages.size()), width/2, height-51, 0xFFF0E9DA);
        else context.drawCenteredTextWithShadow(textRenderer, WallpaperManager.label(ModConfig.get().wallpaper), width/2, height-51, 0xFFFFD383);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override public void removed() { WallpaperManager.clearThumbnails(); }
    @Override public void close() { client.setScreen(parent); }
}
