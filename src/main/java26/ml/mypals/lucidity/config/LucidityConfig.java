package ml.mypals.lucidity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import ml.mypals.lucidity.Lucidity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/** A small 26.3-native configuration store. It keeps the complete Lucidity feature list
 * independent of the removed pre-26.3 MaLiLib GUI and JSON helpers. */
public final class LucidityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lucidity.json");
    private static final Map<Feature, Boolean> VALUES = new EnumMap<>(Feature.class);

    static {
        reset();
    }

    private LucidityConfig() {}

    public enum Feature {
        SCULK_SENSOR_VISUALIZE("Sculk sensor range", "sculk_sensor_range"),
        VIBRATION_TRACE("Vibration trace", "vibration_trace"),
        BOAT_VIEW_RESTRICTION("Boat view restriction", "boat_restriction"),
        BODY_YAW("Entity body yaw", "body_yaw"),
        FLUID_TRANSPARENCY_OVERRIDE("Fluid transparency", "fluid_transparency_override"),
        CREAKING_HEART_INDICATOR("Creaking heart indicator", "creaking_heart_indicator"),
        ITEM_MERG_RANGE_VISUALIZE("Item merge range", "item_merge_range"),
        ENDER_DRAGON_WAYPOINTS_VISUALIZE("Ender dragon waypoints", "ender_dragon_waypoints"),
        ENDER_DRAGON_DESTRUCTION_VISUALIZE("Ender dragon destruction range", "ender_dragon_destruction"),
        WITHER_DESTRUCTION_VISUALIZE("Wither destruction range", "wither_destruction"),
        NETHER_COORDINATE_CACULATOR("Nether coordinate calculator", "nether_coordinate_calculator"),
        WORLD_EATER_MINE_HELPER("World eater mine helper", "world_eater_mine_helper"),
        VAULT_ITEM_DISPLAY("Vault item display", "vault_item_display"),
        B36_TARGET_PREVIEW("B36 target preview", "b36_target_preview"),
        FLUID_SOURCE_HIGHLIGHT("Fluid source highlight", "fluid_source_highlight"),
        ACCURATE_ENTITY_SHADOW("Accurate entity shadow", "accurate_entity_shadow"),
        CONTAINER_SIGNAL_PREVIEW("Container signal preview", "container_signal_preview"),
        EXTENDED_HITBOX("Extended hitbox", "extended_hitbox"),
        BLOCK_NO_RANDOM_OFFSET("Disable block random offset", "block_no_random_offset"),
        ZALGO_TEXT_DEOBF("Zalgo text deobfuscation", "zalgo_text_deobfuscation"),
        FALLING_BLOCK_PREVIEW("Falling block preview", "falling_block_preview"),
        SOUND_VISUALIZE("Sound visualizer", "sound_visualize"),
        BLOCK_EVENT_VISUALIZE("Block event visualizer", "block_event_visualize"),
        MOB_SPAWN_VISUALIZE("Mob spawn visualizer", "mob_spawn_visualize"),
        INVISIBLE_ENTITY_OVERRIDE("Invisible entity transparency", "invisible_entity_override"),
        MICROTIMING_MARKER_VISUALIZER("Carpet TIS microtiming markers", "microtiming_markers");

        private final String label;
        private final String key;
        Feature(String label, String key) { this.label = label; this.key = key; }
        public String label() { return label; }
        public String key() { return key; }
    }

    public static void reset() {
        VALUES.clear();
        for (Feature feature : Feature.values()) VALUES.put(feature, false);
    }

    public static boolean isEnabled(Feature feature) {
        return VALUES.getOrDefault(feature, false);
    }

    public static void setEnabled(Feature feature, boolean enabled) {
        VALUES.put(feature, enabled);
        save();
    }

    public static void toggle(Feature feature) {
        setEnabled(feature, !isEnabled(feature));
    }

    public static int enabledCount() {
        int result = 0;
        for (boolean enabled : VALUES.values()) if (enabled) result++;
        return result;
    }

    public static void load() {
        reset();
        try {
            if (!Files.isReadable(FILE)) return;
            var parsed = JsonParser.parseString(Files.readString(FILE));
            if (!parsed.isJsonObject()) return;
            JsonObject object = parsed.getAsJsonObject();
            for (Feature feature : Feature.values()) {
                if (object.has(feature.key()) && object.get(feature.key()).isJsonPrimitive()) {
                    VALUES.put(feature, object.get(feature.key()).getAsBoolean());
                }
            }
        } catch (Exception error) {
            Lucidity.LOGGER.warn("Unable to load Lucidity configuration", error);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject object = new JsonObject();
            for (Feature feature : Feature.values()) object.addProperty(feature.key(), isEnabled(feature));
            Files.writeString(FILE, GSON.toJson(object));
        } catch (IOException error) {
            Lucidity.LOGGER.warn("Unable to save Lucidity configuration", error);
        }
    }
}

