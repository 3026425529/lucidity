package ml.mypals.lucidity.features.visualizers.explosion;

import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayerHLIGHT.getBooleanValue()) {
                BlockState blockState = view.getBlockState(pos);
                if (blockState.hasProperty(LiquidBlock.LEVEL) && blockState.getValue(LiquidBlock.LEVEL) == 0) {
                    return lavaSourceSpites;
                }
            }
            return defaultLavaSourceSpites;
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.LAVA, lavaSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_LAVA, lavaSourceRenderHandler);

        //? if >=1.21.9 {
        defaultWaterOverlaySprite = sprite(ModelBakery.WATER_OVERLAY);
        //?} else {
        /*defaultWaterOverlaySprite = ModelBakery.WATER_OVERLAY.sprite();
         *///?}


        waterSourceStillSprite = atlas.apply(STILL_WATER_SPRITE_ID);
        waterSourceFlowSprite = atlas.apply(FLOWING_WATER_SPRITE_ID);
        waterSourceSpites[0] = waterSourceStillSprite;
        waterSourceSpites[1] = waterSourceFlowSprite;
        waterSourceSpites[2] = defaultWaterOverlaySprite;

        bubbleDownSprite = atlas.apply(DOWN_BUBBLE_SPRITE_ID);
        bubbleUpSprite = atlas.apply(UP_BUBBLE_SPRITE_ID);
        bubbleWaterSpitesDown[0] = waterSourceStillSprite;
        bubbleWaterSpitesDown[1] = bubbleDownSprite;
        bubbleWaterSpitesDown[2] = defaultWaterOverlaySprite;

        bubbleWaterSpitesUp[0] = waterSourceStillSprite;
        bubbleWaterSpitesUp[1] = bubbleUpSprite;
        bubbleWaterSpitesUp[2] = defaultWaterOverlaySprite;

        //? if >=1.21.5 {
        defaultWaterSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).particleIcon();
        //?} else {
        /*defaultWaterSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        *///?}
        //? if >=1.21.9 {
        defaultWaterSourceFlowSprite = sprite(ModelBakery.WATER_FLOW);
        //?} else {
        /*defaultWaterSourceFlowSprite = ModelBakery.WATER_FLOW.sprite();
        *///?}

        defaultWaterSourceSpites[0] = defaultWaterSourceStillSprite;
        defaultWaterSourceSpites[1] = defaultWaterSourceFlowSprite;
        defaultWaterSourceSpites[2] = defaultWaterOverlaySprite;

        FluidRenderHandler waterSourceRenderHandler = new FluidRenderHandler() {
            @Override
            public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                if (view != null && pos != null &&  FLUID_SOURCE_HIGHLIGHT.getBooleanValue()) {
                    BlockState blockState = view.getBlockState(pos);
                    if (blockState.is(Blocks.BUBBLE_COLUMN) && blockState.getValue(DRAG)  && view.getFluidState(pos).isSource()) {
                        return bubbleWaterSpitesDown;
                    }
                    if (blockState.is(Blocks.BUBBLE_COLUMN) && !blockState.getValue(DRAG)  && view.getFluidState(pos).isSource()) {
                        return bubbleWaterSpitesUp;
                    }
                    if (view.getFluidState(pos).isSource()) {
                        return waterSourceSpites;
                    }
                }
                return defaultWaterSourceSpites;
            }
            @Override
            public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                return BiomeColors.getAverageWaterColor(view == null? Minecraft.getInstance().level : view, pos == null?new BlockPos(0,0,0):pos);
            }
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, waterSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_WATER, waterSourceRenderHandler);
    }
    //? if >=1.21.9 {

    public TextureAtlasSprite sprite(Material material) {
        return (TextureAtlasSprite)Minecraft.getInstance().getAtlasManager().get(material);
    }
    //?}
}
