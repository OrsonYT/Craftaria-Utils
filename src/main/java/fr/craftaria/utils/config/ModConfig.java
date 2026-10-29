package fr.craftaria.utils.config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("craftaria-utils.json");
    private static Data data = new Data();
    private ModConfig() {}
    public static Data get() { return data; }
    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) { Data loaded = GSON.fromJson(reader, Data.class); if (loaded != null) data = loaded; }
        catch (Exception ignored) { data = new Data(); }
        data.cookingDelayTicks = Math.clamp(Math.round(data.cookingDelayTicks / 2.0f) * 2, 2, 100);
        data.repairThresholdPercent = Math.clamp(data.repairThresholdPercent, 1, 100);
        if (data.wallpaper == null) data.wallpaper = "";
        if (data.trackedPlayers == null) data.trackedPlayers = "";
        if (data.protectedItems == null) data.protectedItems = new ArrayList<>();
        data.protectedItems.removeIf(id -> id == null || id.isBlank());
        var homes = new LinkedHashMap<String, QuickHome>();
        if (data.quickHomes != null) for (QuickHome home : data.quickHomes) {
            if (home != null && QuickHome.validName(home.name)) {
                // Migrate older shortcuts: their former single name was also the command argument.
                if (home.displayName == null) home.displayName = "";
                homes.putIfAbsent(home.name.toLowerCase(Locale.ROOT), home);
            }
        }
        data.quickHomes = new ArrayList<>(homes.values());
    }
    public static void save() {
        try { Files.createDirectories(FILE.getParent()); try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(data, writer); } }
        catch (Exception ignored) { /* Keep current-session settings if disk is unavailable. */ }
    }
    public static final class Data {
        public boolean autoRepair = false;
        public int repairThresholdPercent = 50;
        public String wallpaper = "";
        public String password = "";
        public int cookingDelayTicks = 16;
        public boolean connectionTrackerEnabled = false;
        public boolean trackerNotifyJoin = true;
        public boolean trackerNotifyLeave = true;
        public boolean trackerNotifyAlreadyOnline = true;
        /** Comma or semicolon separated Minecraft usernames to watch. */
        public String trackedPlayers = "";
        public boolean antiDropEnabled = true;
        public List<String> protectedItems = new ArrayList<>();
        public List<QuickHome> quickHomes = new ArrayList<>();
    }
}
