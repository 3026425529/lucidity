package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.features.LucidityFeatureRuntime;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

/** Removes particles in the selected region before their 26.3 extraction state is built. */
@Mixin(ParticleGroup.class)
public abstract class LuciditySelectiveParticleMixin {
    @Shadow @Final protected Queue<Particle> particles;

    @Inject(method = "tickParticles", at = @At("HEAD"))
    private void lucidity$removeSelectedParticles(CallbackInfo callback) {
        particles.removeIf(particle -> LucidityFeatureRuntime.isSelectivePosition(
                BlockPos.containing(particle.getBoundingBox().getCenter())));
    }
}

