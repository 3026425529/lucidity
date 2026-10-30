package ml.mypals.lucidity.features;

import com.mojang.blaze3d.platform.NativeImage;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

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
        int index = 0;
        for (String entry : LucidityConfig.imageSources()) {
            if (entry == null || entry.isBlank()) continue;
            String[] fields = entry.split(";", 2);
            String source = fields[0].trim();
            if (source.isEmpty()) continue;
            try (InputStream input = open(source)) {
                NativeImage image = NativeImage.read(input);
                String name = fields.length > 1 && !fields[1].isBlank() ? fields[1].trim() : "image_" + index;
                Identifier id = Identifier.fromNamespaceAndPath(LucidityModInfo.MOD_ID, "generated/" + safe(name));
                client.getTextureManager().register(id, new DynamicTexture(() -> id.toString(), image));
                TEXTURES.put(name, id);
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
}


