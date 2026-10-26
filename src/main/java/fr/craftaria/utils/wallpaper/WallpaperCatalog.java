package fr.craftaria.utils.wallpaper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Reads the same category tree from development resources and packaged JAR roots. */
public final class WallpaperCatalog {
    public record Entry(String id, String name, String description, String author, int order) { }
    public record Category(String name, int order, List<Entry> entries) { }
    public record Catalog(String defaultResource, List<Category> categories, Map<String, Entry> entries) { }
    private WallpaperCatalog() { }

    public static Catalog read(List<Path> roots, Path personal) throws IOException {
        Map<String, Category> categories = new LinkedHashMap<>();
        String defaultResource = null;
        Entry defaultEntry = new Entry("", "", "", "", 0);
        String defaultName = "Défaut";
        int defaultOrder = 1;
        for (Path root : roots) {
            Path folder = root.resolve("assets/wallpaper");
            if (!Files.isDirectory(folder)) continue;
            try (var dirs = Files.list(folder)) {
                for (Path dir : dirs.filter(Files::isDirectory).sorted().toList()) {
                    boolean isDefault = dir.getFileName().toString().equalsIgnoreCase("Default");
                    if (!isDefault && !Files.isRegularFile(dir.resolve("categories.json"))) continue;
                    JsonObject category = json(dir.resolve("categories.json"));
                    String name = string(category, "categoryName", dir.getFileName().toString());
                    int order = number(category, "displayOrder", Integer.MAX_VALUE);
                    List<Path> images;
                    try (var files = Files.walk(dir)) {
                        images = files.filter(Files::isRegularFile).filter(WallpaperCatalog::image).sorted().toList();
                    }
                    List<Entry> entries = new ArrayList<>();
                    for (Path image : images) {
                        JsonObject info = json(image.getParent().resolve("info.json"));
                        String relative = root.relativize(image).toString().replace('\\', '/');
                        Entry entry = new Entry(isDefault ? "" : "catalog:"+relative,
                                string(info, "displayName", image.getFileName().toString()),
                                string(info, "shortDescription", ""), string(info, "author", ""), number(info, "displayOrder", Integer.MAX_VALUE));
                        if (isDefault) {
                            defaultResource = relative;
                            defaultEntry = entry;
                            break; // The default category always contains exactly one choice.
                        }
                        entries.add(entry);
                    }
                    entries.sort(Comparator.comparingInt(Entry::order).thenComparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
                    if (isDefault) { defaultName = name; defaultOrder = order; }
                    else categories.putIfAbsent(dir.getFileName().toString(), new Category(name, order, entries));
                }
            }
        }
        List<Category> result = new ArrayList<>(categories.values());
        result.sort(Comparator.comparingInt(Category::order).thenComparing(Category::name));
        // Default is first even if an accidentally edited category order would put it later.
        result.add(0, new Category(defaultName, defaultOrder, List.of(defaultEntry)));
        if (Files.isDirectory(personal)) {
            List<Entry> entries = new ArrayList<>();
            try (var files = Files.list(personal)) {
                for (Path file : files.filter(Files::isRegularFile).filter(WallpaperCatalog::image).sorted().toList()) {
                    String name = file.getFileName().toString();
                    entries.add(new Entry(name, name, "", "", Integer.MAX_VALUE));
                }
            }
            if (!entries.isEmpty()) result.add(new Category("", Integer.MAX_VALUE, entries));
        }
        Map<String, Entry> all = new LinkedHashMap<>();
        for (Category category : result) for (Entry entry : category.entries()) all.putIfAbsent(entry.id(), entry);
        return new Catalog(defaultResource, List.copyOf(result), Collections.unmodifiableMap(all));
    }

    private static boolean image(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }
    private static JsonObject json(Path path) {
        if (!Files.isRegularFile(path)) return new JsonObject();
        try (var reader = Files.newBufferedReader(path)) { return JsonParser.parseReader(reader).getAsJsonObject(); }
        catch (Exception e) { return new JsonObject(); }
    }
    private static String string(JsonObject object, String key, String fallback) {
        try { return object.has(key) ? object.get(key).getAsString() : fallback; }
        catch (Exception e) { return fallback; }
    }
    private static int number(JsonObject object, String key, int fallback) {
        try { return object.has(key) ? object.get(key).getAsInt() : fallback; }
        catch (Exception e) { return fallback; }
    }
}
