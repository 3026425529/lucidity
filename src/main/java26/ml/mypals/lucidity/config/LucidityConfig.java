package ml.mypals.lucidity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import ml.mypals.lucidity.Lucidity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** A small 26.3-native configuration store. It keeps the complete Lucidity feature list
 * independent of the removed pre-26.3 MaLiLib GUI and JSON helpers. */
public final class LucidityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lucidity.json");
    private static final Map<Feature, Boolean> VALUES = new EnumMap<>(Feature.class);
    private static final List<String> IMAGE_SOURCES = new ArrayList<>();
    private static final List<Area> SELECTIVE_AREAS = new ArrayList<>();

    static {
        reset();
    }

    private LucidityConfig() {}

    public record Area(BlockPos min, BlockPos max) {
        public Area {
            min = new BlockPos(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
            max = new BlockPos(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
        }

        public boolean contains(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }

        public AABB box() {
            return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
        }
    }

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
        MICROTIMING_MARKER_VISUALIZER("Carpet TIS microtiming markers", "microtiming_markers"),
        EXPLOSION_VISUALIZE("Explosion visualizer", "explosion_visualizer"),
        IMAGE_RENDERING("Image renderer", "image_rendering"),
        SELECTIVE_RENDERING("Selective rendering", "selective_rendering");

        private final String label;
        private final String key;
        Feature(String label, String key) { this.label = label; this.key = key; }
        public String label() { return label; }
        public String key() { return key; }
    }

    public static void reset() {
        VALUES.clear();
        IMAGE_SOURCES.clear();
        SELECTIVE_AREAS.clear();
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
            IMAGE_SOURCES.clear();
            if (object.has("images") && object.get("images").isJsonArray()) {
                for (var entry : object.getAsJsonArray("images")) {
                    if (entry.isJsonPrimitive()) IMAGE_SOURCES.add(entry.getAsString());
                }
            }
            SELECTIVE_AREAS.clear();
            if (object.has("selective_areas") && object.get("selective_areas").isJsonArray()) {
                for (var entry : object.getAsJsonArray("selective_areas")) {
                    if (!entry.isJsonObject()) continue;
                    JsonObject area = entry.getAsJsonObject();
                    SELECTIVE_AREAS.add(new Area(
                            new BlockPos(area.get("min_x").getAsInt(), area.get("min_y").getAsInt(), area.get("min_z").getAsInt()),
                            new BlockPos(area.get("max_x").getAsInt(), area.get("max_y").getAsInt(), area.get("max_z").getAsInt())));
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
            JsonArray images = new JsonArray();
            IMAGE_SOURCES.forEach(images::add);
            object.add("images", images);
            JsonArray areas = new JsonArray();
            for (Area area : SELECTIVE_AREAS) {
                JsonObject value = new JsonObject();
                value.addProperty("min_x", area.min().getX());
                value.addProperty("min_y", area.min().getY());
                value.addProperty("min_z", area.min().getZ());
                value.addProperty("max_x", area.max().getX());
                value.addProperty("max_y", area.max().getY());
                value.addProperty("max_z", area.max().getZ());
                areas.add(value);
            }
            object.add("selective_areas", areas);
            Files.writeString(FILE, GSON.toJson(object));
        } catch (IOException error) {
            Lucidity.LOGGER.warn("Unable to save Lucidity configuration", error);
        }
    }

    public static List<String> imageSources() {
        return Collections.unmodifiableList(new ArrayList<>(IMAGE_SOURCES));
    }

    public static void setImageSources(List<String> sources) {
        IMAGE_SOURCES.clear();
        IMAGE_SOURCES.addAll(sources);
        save();
    }

    public static List<Area> selectiveAreas() {
        return Collections.unmodifiableList(new ArrayList<>(SELECTIVE_AREAS));
    }

    public static void addSelectiveArea(BlockPos center) {
        int minX = Math.floorDiv(center.getX(), 16) * 16;
        int minZ = Math.floorDiv(center.getZ(), 16) * 16;
        SELECTIVE_AREAS.add(new Area(new BlockPos(minX, center.getY() - 16, minZ),
                new BlockPos(minX + 15, center.getY() + 16, minZ + 15)));
        save();
    }

    public static void clearSelectiveAreas() {
        SELECTIVE_AREAS.clear();
        save();
    }
}



