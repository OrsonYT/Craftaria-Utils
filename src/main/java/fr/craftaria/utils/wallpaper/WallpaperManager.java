package fr.craftaria.utils.wallpaper;

import fr.craftaria.utils.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import fr.craftaria.utils.screen.GuiTextures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Dynamic textures stay available while resource packs reload. */
public final class WallpaperManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Craftaria Utils/Wallpaper");
    private static final Path DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("Wallpaper").toAbsolutePath().normalize();
    private static final String BUILTIN = "builtin:";
    private static WallpaperCatalog.Catalog catalog;
    private static final Identifier TEXTURE = Identifier.of("craftaria_utils", "selected_wallpaper");
    private record ImageTexture(Identifier id, int width, int height) { }
    private static final Map<String, ImageTexture> THUMBNAILS = new HashMap<>();
    private static final Set<String> FAILED_THUMBNAILS = new HashSet<>();
    private static int nextThumbnail;
    private static boolean ready;
    private static int imageWidth = 2560, imageHeight = 1600;
    private static boolean loadFailed;

    private WallpaperManager() { }

    public static void initializeDirectory() {
        try { Files.createDirectories(DIRECTORY); }
        catch (IOException e) { LOGGER.warn("Could not create Wallpaper folder", e); }
    }

    public static Path directory() { return DIRECTORY; }
    public static boolean hasLoadError() { return loadFailed; }

    private static WallpaperCatalog.Catalog catalog() {
        if (catalog == null) {
            initializeDirectory();
            try {
                catalog = WallpaperCatalog.read(FabricLoader.getInstance().getModContainer("craftaria_utils").orElseThrow().getRootPaths(), DIRECTORY);
            } catch (IOException e) { throw new IllegalStateException("Could not read wallpaper catalog", e); }
        }
        return catalog;
    }

    public static List<WallpaperCatalog.Category> categories() { return catalog().categories(); }
    public static List<String> list() { return new ArrayList<>(catalog().entries().keySet()); }
    public static Text label(String name) {
        var entry = catalog().entries().get(name);
        if (entry != null && !entry.name().isBlank()) return Text.literal(entry.name());
        return name.isEmpty() ? Text.translatable("craftaria_utils.wallpaper.default") : Text.literal(name);
    }
    public static Text tooltip(String name) {
        var text = label(name).copy();
        var entry = catalog().entries().get(name);
        if (entry != null) {
            if (!entry.description().isBlank()) text.append("\n"+entry.description());
            if (!entry.author().isBlank()) text.append("\n").append(Text.translatable("craftaria_utils.wallpaper.author", entry.author()));
        }
        return text;
    }
    public static boolean isBundled(String name) { return name.isEmpty() || name.startsWith("catalog:") || name.startsWith(BUILTIN); }

    private static String resolveSelection(String name) {
        if (name == null || name.isEmpty()) return "";
        if (catalog().entries().containsKey(name)) return name;
        // Migrate old built-in IDs by filename where the image moved to the new tree.
        if (name.startsWith(BUILTIN)) {
            String filename = name.substring(BUILTIN.length());
            filename = filename.substring(filename.lastIndexOf('/')+1);
            for (String id : catalog().entries().keySet())
                if (id.startsWith("catalog:") && id.endsWith("/"+filename)) return id;
        }
        return "";
    }

    private static boolean supported(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private static InputStream open(String name) throws IOException {
        if (name == null || name.isEmpty() || name.startsWith("catalog:")) {
            String resource = name == null || name.isEmpty() ? catalog().defaultResource() : name.substring("catalog:".length());
            if (resource == null) throw new IOException("The Default wallpaper folder contains no image");
            if (!resource.startsWith("assets/") || resource.contains("..") || resource.contains("\\"))
                throw new IOException("Invalid wallpaper resource path");
            Path path = FabricLoader.getInstance().getModContainer("craftaria_utils").orElseThrow()
                    .findPath(resource).orElseThrow(() -> new IOException("Wallpaper resource is missing"));
            return Files.newInputStream(path);
        }
        Path file = DIRECTORY.resolve(name).normalize();
        if (!DIRECTORY.equals(file.getParent()) || !supported(name)) throw new IOException("Invalid wallpaper filename");
        return Files.newInputStream(file);
    }

    private static BufferedImage decode(String name) throws IOException {
        try (InputStream stream = open(name)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null || image.getWidth() > 8192 || image.getHeight() > 8192)
                throw new IOException("Unreadable image or dimensions exceed 8192 pixels");
            return image;
        }
    }

    private static ImageTexture upload(Identifier id, BufferedImage decoded) {
        NativeImage image = new NativeImage(decoded.getWidth(), decoded.getHeight(), false);
        NativeImageBackedTexture texture = null;
        try {
            for (int y = 0; y < decoded.getHeight(); y++)
                for (int x = 0; x < decoded.getWidth(); x++) image.setColor(x, y, Integer.reverseBytes(decoded.getRGB(x, y)) >>> 8 | (decoded.getRGB(x, y) & 0xFF000000));
            texture = new NativeImageBackedTexture(image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
            return new ImageTexture(id, decoded.getWidth(), decoded.getHeight());
        } catch (RuntimeException e) {
            if (texture != null) texture.close();
            else image.close();
            throw e;
        }
    }

    public static boolean select(String name) {
        if (!load(name)) return false;
        ModConfig.get().wallpaper = name;
        ModConfig.save();
        return true;
    }

    public static void clearThumbnails() {
        var manager = MinecraftClient.getInstance().getTextureManager();
        THUMBNAILS.values().forEach(texture -> manager.destroyTexture(texture.id()));
        THUMBNAILS.clear();
        FAILED_THUMBNAILS.clear();
    }

    public static void refresh() {
        ready = false;
        catalog = null;
        clearThumbnails();
    }

    private static boolean load(String name) {
        try {
            ImageTexture texture = upload(TEXTURE, decode(name));
            imageWidth = texture.width();
            imageHeight = texture.height();
            ready = true;
            loadFailed = false;
            return true;
        } catch (Exception e) {
            loadFailed = true;
            LOGGER.warn("Could not load wallpaper {}", name, e);
            return false;
        }
    }

    public static void drawThumbnail(DrawContext context, String name, int x, int y, int width, int height) {
        ImageTexture texture = THUMBNAILS.get(name);
        if (texture == null && !FAILED_THUMBNAILS.contains(name)) {
            try {
                BufferedImage full = decode(name);
                double scale = Math.min(1, Math.min(320.0 / full.getWidth(), 180.0 / full.getHeight()));
                BufferedImage small = new BufferedImage(Math.max(1, (int)Math.round(full.getWidth()*scale)),
                        Math.max(1, (int)Math.round(full.getHeight()*scale)), BufferedImage.TYPE_INT_ARGB);
                var graphics = small.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(full, 0, 0, small.getWidth(), small.getHeight(), null);
                } finally { graphics.dispose(); }
                texture = upload(Identifier.of("craftaria_utils", "wallpaper_thumbnail_" + nextThumbnail++), small);
                THUMBNAILS.put(name, texture);
            } catch (Exception e) {
                FAILED_THUMBNAILS.add(name);
                LOGGER.warn("Could not load wallpaper thumbnail {}", name, e);
            }
        }
        if (texture != null) drawTexture(context, texture, x, y, width, height, 255);
        else {
            context.fill(x, y, x+width, y+height, 0xFF22252B);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("?"), x+width/2, y+height/2-4, 0xFFFF8888);
        }
    }

    public static void draw(DrawContext context, int width, int height) { draw(context, 0, 0, width, height, 255); }

    public static void draw(DrawContext context, int x, int y, int width, int height, int alpha) {
        if (width <= 0 || height <= 0 || alpha <= 0) return;
        if (!ready) {
            String selected = resolveSelection(ModConfig.get().wallpaper);
            if (!selected.equals(ModConfig.get().wallpaper)) {
                ModConfig.get().wallpaper = selected;
                ModConfig.save();
            }
            if (!load(selected)) {
                if (!selected.isEmpty() && load("")) {
                    ModConfig.get().wallpaper = "";
                    ModConfig.save();
                } else {
                    context.fill(x, y, x+width, y+height, 0xFF101720);
                    return;
                }
            }
        }
        drawTexture(context, new ImageTexture(TEXTURE, imageWidth, imageHeight), x, y, width, height, alpha);
    }

    private static void drawTexture(DrawContext context, ImageTexture texture, int x, int y, int width, int height, int alpha) {
        if (width <= 0 || height <= 0) return;
        double scale = Math.max((double)width/texture.width(), (double)height/texture.height());
        int rw = Math.max(1, Math.min(texture.width(), (int)Math.round(width/scale)));
        int rh = Math.max(1, Math.min(texture.height(), (int)Math.round(height/scale)));
        float u = (texture.width()-rw)/2f, v = (texture.height()-rh)/2f;
        GuiTextures.draw(context, texture.id(), x, y, u, v, width, height,
                rw, rh, texture.width(), texture.height(), (Math.clamp(alpha, 0, 255)<<24)|0xFFFFFF);
    }
}
