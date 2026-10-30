package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps invisible living entities in the extraction stream so they can be inspected. */
@Mixin(LivingEntityRenderer.class)
public abstract class LucidityInvisibleEntityMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void lucidity$showInvisible(LivingEntityRenderState state, PoseStack pose,
                                        SubmitNodeCollector output, CameraRenderState camera,
                                        CallbackInfo callback) {
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.INVISIBLE_ENTITY_OVERRIDE)) {
            state.isInvisible = false;
        }
    }
}

