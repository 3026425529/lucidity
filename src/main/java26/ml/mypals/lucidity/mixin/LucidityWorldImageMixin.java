package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.features.LucidityImageRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds image quads to the 26.3 world feature submission pass. */
@Mixin(LevelRenderer.class)
public abstract class LucidityWorldImageMixin {
    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void lucidity$submitWorldImages(LevelRenderState state, SubmitNodeCollector output,
                                             boolean includeBlockOutline, CallbackInfo callback) {
        if (state.cameraRenderState != null && state.cameraRenderState.pos != null) {
            LucidityImageRenderer.submitWorldImages(output, state.cameraRenderState.pos);
        }
    }
}

