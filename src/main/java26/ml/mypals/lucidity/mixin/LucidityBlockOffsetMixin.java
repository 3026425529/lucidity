package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes block model offsets deterministic when the matching Lucidity toggle is on. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LucidityBlockOffsetMixin {
    @Inject(method = "getOffset", at = @At("HEAD"), cancellable = true)
    private void lucidity$disableRandomOffset(BlockPos pos, CallbackInfoReturnable<Vec3> callback) {
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.BLOCK_NO_RANDOM_OFFSET)) {
            callback.setReturnValue(Vec3.ZERO);
        }
    }
}

