package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer.Output;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Suppresses fluid quads in a selected region along with terrain blocks. */
@Mixin(FluidRenderer.class)
public final class LuciditySelectiveFluidMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void lucidity$hideSelectedFluid(BlockAndTintGetter level, BlockPos pos, Output output,
                                             BlockState state, FluidState fluid, CallbackInfo callback) {
        if (LucidityFeatureRuntime.isSelectivePosition(pos)) callback.cancel();
    }
}

