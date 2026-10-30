package ml.mypals.lucidity.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies a predictable 50% alpha to fluid vertices without replacing Sodium's buffers. */
@Mixin(FluidRenderer.class)
public abstract class LucidityFluidTransparencyMixin {
    @Redirect(method = "tesselate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/FluidRenderer$Output;getBuilder(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer lucidity$wrapFluidBuilder(FluidRenderer.Output output, ChunkSectionLayer layer) {
        VertexConsumer delegate = output.getBuilder(layer);
        return LucidityConfig.isEnabled(LucidityConfig.Feature.FLUID_TRANSPARENCY_OVERRIDE)
                ? new AlphaVertexConsumer(delegate) : delegate;
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private AlphaVertexConsumer(VertexConsumer delegate) { this.delegate = delegate; }
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a / 2); return this; }
        @Override public VertexConsumer setColor(int color) { delegate.setColor((color & 0x00FFFFFF) | (((color >>> 24) / 2) << 24)); return this; }
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
        @Override public VertexConsumer setUv3(float u, float v) { delegate.setUv3(u, v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
        @Override public VertexConsumer setLineWidth(float width) { delegate.setLineWidth(width); return this; }
    }
}

