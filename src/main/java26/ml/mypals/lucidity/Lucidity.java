package ml.mypals.lucidity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

/**
 * Lucidity's 26.3 client entrypoint.
 *
 * The pre-26.1 implementation is kept under src/main/java as legacy source while
 * the renderer-facing features are migrated to the 26.3 extraction pipeline.
 */
public final class Lucidity implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer(LucidityModInfo.MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.fromNamespaceAndPath(LucidityModInfo.MOD_ID, "lavahighlight"),
                        container,
                        ResourcePackActivationType.NORMAL));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Feature modules are registered here as they are migrated to 26.3.
        });
    }
}

