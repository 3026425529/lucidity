package ml.mypals.lucidity;

import com.mojang.blaze3d.platform.InputConstants;
import ml.mypals.lucidity.config.LucidityConfig;
import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Lucidity client entry point for Minecraft 26.3. */
public final class Lucidity implements ClientModInitializer {
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Lucidity");
    private static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.lucidity.open_config", InputConstants.Type.KEYBOARD,
            InputConstants.KEY_V, KeyMapping.Category.MISC);

    @Override
    public void onInitializeClient() {
        LucidityConfig.load();
        FabricLoader.getInstance().getModContainer(LucidityModInfo.MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.fromNamespaceAndPath(LucidityModInfo.MOD_ID, "lavahighlight"),
                        container, ResourcePackActivationType.NORMAL));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LucidityModInfo.MOD_ID, "feature_status"),
                (graphics, delta) -> {
                    int y = 4;
                    for (String line : LucidityFeatureRuntime.hudLines()) {
                        graphics.text(Minecraft.getInstance().font, line, 4, y, 0xFFFFFFFF, true);
                        y += 10;
                    }
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LucidityFeatureRuntime.tick(client);
            while (OPEN_CONFIG.consumeClick()) {
                client.setScreenAndShow(new ml.mypals.lucidity.config.LucidityConfigScreen(client.screen));
            }
        });
    }
}

