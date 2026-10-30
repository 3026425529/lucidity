package ml.mypals.lucidity.features;

import com.mojang.blaze3d.platform.NativeImage;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads image entries from lucidity.json into client dynamic textures. Entries may be
 * local paths or HTTP(S) URLs; optional semicolon fields are accepted like the original
 * format, with the first field being the image source and the second the display name. */
public final class LucidityImageRenderer {
    private static final Map<String, Identifier> TEXTURES = new LinkedHashMap<>();
    private static final Map<String, Placement> PLACEMENTS = new LinkedHashMap<>();
    private static String loadedSignature = "";

    private LucidityImageRenderer() {}

    public static void tick(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.IMAGE_RENDERING)) return;
        String signature = String.join("\u001f", LucidityConfig.imageSources());
        if (!signature.equals(loadedSignature)) {
            loadedSignature = signature;
            reload(client);
        }
    }

    private static void reload(Minecraft client) {
        TEXTURES.clear();
        PLACEMENTS.clear();
        int index = 0;
        for (String entry : LucidityConfig.imageSources()) {
            if (entry == null || entry.isBlank()) continue;
            String[] fields = entry.split(";", -1);
            String source = fields[0].trim();
            if (source.isEmpty()) continue;
            try (InputStream input = open(source)) {
                NativeImage image = NativeImage.read(input);
                String name = fields.length > 1 && !fields[1].isBlank() ? fields[1].trim() : "image_" + index;
                Identifier id = Identifier.fromNamespaceAndPath(LucidityModInfo.MOD_ID, "generated/" + safe(name));
                client.getTextureManager().register(id, new DynamicTexture(() -> id.toString(), image));
                TEXTURES.put(name, id);
                if (fields.length >= 5) {
                    Vec3 position = parseVector(fields[2], Vec3.ZERO);
                    double[] rotation = fields.length > 3 ? parseNumbers(fields[3], 4) : new double[0];
                    double[] scale = parseNumbers(fields[4], 2);
                    PLACEMENTS.put(name, new Placement(position, Math.max(0.25, scale[0]), Math.max(0.25, scale[1]), rotation));
                }
                index++;
            } catch (Exception error) {
                Lucidity.LOGGER.warn("Unable to load Lucidity image {}", source, error);
            }
        }
    }

    private static InputStream open(String source) throws Exception {
        if (source.startsWith("http://") || source.startsWith("https://")) return URI.create(source).toURL().openStream();
        return Files.newInputStream(Path.of(source));
    }

    private static String safe(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
    }

    public static void extractHud(GuiGraphicsExtractor graphics) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.IMAGE_RENDERING) || TEXTURES.isEmpty()) return;
        int y = 4;
        int x = graphics.guiWidth() - 72;
        int shown = 0;
        for (Identifier id : TEXTURES.values()) {
            graphics.blit(id, x, y, 64, 64, 0, 0, 1, 1);
            y += 68;
            if (++shown >= 3) break;
        }
    }

    public static int loadedCount() { return TEXTURES.size(); }

    /** Emits world-space anchors for the original image entry format. The texture itself
     * remains available in the HUD; anchors make coordinates and scale inspectable on 26.3. */
    public static void emitWorldGizmos() {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.IMAGE_RENDERING)) return;
        for (Map.Entry<String, Placement> entry : PLACEMENTS.entrySet()) {
            Placement p = entry.getValue();
            AABB frame = new AABB(p.position.x - p.width * 0.5, p.position.y - p.height * 0.5, p.position.z - 0.01,
                    p.position.x + p.width * 0.5, p.position.y + p.height * 0.5, p.position.z + 0.01);
            Gizmos.cuboid(frame, GizmoStyle.stroke(0xFFFF66CC, 1.0F)).setAlwaysOnTop();
            Gizmos.billboardText(entry.getKey(), p.position.add(0, p.height * 0.5 + 0.15, 0),
                    TextGizmo.Style.forColorAndCentered(0xFFFFAADD)).setAlwaysOnTop();
        }
    }

    /** Submits an actual camera-relative textured quad through the 26.3 feature collector. */
    public static void submitWorldImages(SubmitNodeCollector output, Vec3 cameraPosition) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.IMAGE_RENDERING)) return;
        for (Map.Entry<String, Placement> entry : PLACEMENTS.entrySet()) {
            Identifier texture = TEXTURES.get(entry.getKey());
            if (texture == null) continue;
            Placement p = entry.getValue();
            PoseStack pose = new PoseStack();
            pose.translate(p.position.x - cameraPosition.x, p.position.y - cameraPosition.y, p.position.z - cameraPosition.z);
            if (p.rotation.length == 4) {
                pose.mulPose(new Transformation(new Vector3f(),
                        new Quaternionf((float) p.rotation[0], (float) p.rotation[1], (float) p.rotation[2], (float) p.rotation[3]),
                        new Vector3f(1.0F, 1.0F, 1.0F), new Quaternionf()));
            }
            output.submitCustomGeometry(pose, RenderTypes.entityTranslucent(texture), (stackPose, consumer) -> {
                float halfWidth = (float) p.width * 0.5F;
                float halfHeight = (float) p.height * 0.5F;
                consumer.addVertex(stackPose, -halfWidth, -halfHeight, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F);
                consumer.addVertex(stackPose, halfWidth, -halfHeight, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F);
                consumer.addVertex(stackPose, halfWidth, halfHeight, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F);
                consumer.addVertex(stackPose, -halfWidth, halfHeight, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F);
            });
        }
    }

    private static Vec3 parseVector(String raw, Vec3 fallback) {
        double[] values = parseNumbers(raw, 3);
        return values.length == 3 ? new Vec3(values[0], values[1], values[2]) : fallback;
    }

    private static double[] parseNumbers(String raw, int count) {
        try {
            String[] parts = raw.replace("[", "").replace("]", "").split(",");
            if (parts.length < count) return new double[0];
            double[] values = new double[count];
            for (int i = 0; i < count; i++) values[i] = Double.parseDouble(parts[i].trim());
            return values;
        } catch (Exception ignored) {
            return new double[0];
        }
    }

    private record Placement(Vec3 position, double width, double height, double[] rotation) {}
}


