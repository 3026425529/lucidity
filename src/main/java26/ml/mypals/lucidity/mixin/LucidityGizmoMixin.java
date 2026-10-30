package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.gizmos.Gizmos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hooks the new 26.3 per-frame gizmo collection. This replaces the old RenderType/
 * MultiBufferSource overlay path and keeps feature outlines compatible with Sodium. */
@Mixin(LevelRenderer.class)
public final class LucidityGizmoMixin {
    @Inject(method = "collectPerFrameRenderThreadGizmos", at = @At("TAIL"))
    private void lucidity$emitFeatureGizmos(CallbackInfoReturnable<Gizmos.TemporaryCollection> callback) {
        LucidityFeatureRuntime.emitGizmos();
    }
}

