package ml.mypals.lucidity.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides entity submissions whose world position lies in the selected region. */
@Mixin(EntityRenderDispatcher.class)
public final class LuciditySelectiveEntityMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void lucidity$hideSelectedEntity(EntityRenderState state, CameraRenderState camera,
                                              double x, double y, double z, PoseStack pose,
                                              SubmitNodeCollector output, CallbackInfo callback) {
        if (LucidityFeatureRuntime.isSelectivePosition(BlockPos.containing(state.x, state.y, state.z))) callback.cancel();
    }
}

