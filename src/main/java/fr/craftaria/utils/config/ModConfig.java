package fr.craftaria.utils.config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
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
    }
    public static void save() {
        try { Files.createDirectories(FILE.getParent()); try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(data, writer); } }
        catch (Exception ignored) { /* Keep current-session settings if disk is unavailable. */ }
    }
    public static final class Data {
        public boolean autoRepair = false;
        public String password = "";
        public int cookingDelayTicks = 40;
    }
}
