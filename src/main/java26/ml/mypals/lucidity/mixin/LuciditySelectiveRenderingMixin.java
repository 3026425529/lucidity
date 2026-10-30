package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 26.3-native selective terrain suppression. The old RenderType/MultiBufferSource
 * hooks are gone, so cancellation happens before model quads are submitted. */
@Mixin(ModelBlockRenderer.class)
public final class LuciditySelectiveRenderingMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void lucidity$hideSelectedTerrain(BlockQuadOutput output, float x, float y, float z,
                                               BlockAndTintGetter level, BlockPos pos, BlockState state,
                                               BlockStateModel model, long seed, CallbackInfo callback) {
        if (LucidityFeatureRuntime.isSelectivePosition(pos)) callback.cancel();
    }
}

