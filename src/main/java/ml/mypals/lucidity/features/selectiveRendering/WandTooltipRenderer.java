package ml.mypals.lucidity.features.selectiveRendering;

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
 //?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.*;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.wand;
import static ml.mypals.lucidity.features.selectiveRendering.WandActionsManager.pos1;
import static ml.mypals.lucidity.features.selectiveRendering.WandActionsManager.pos2;
import static ml.mypals.lucidity.hotkeys.HotkeyCallbacks.*;

public class WandTooltipRenderer {
    private static final List<ToolTipItem> hudItems = new ArrayList<>();
    private static class ToolTipItem {
        String text;
        int color;

        @Nullable
        ResourceLocation icon;
        public ToolTipItem(String text, Color color, @Nullable ResourceLocation icon) {
            this.text = text;
            this.color = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            this.icon = icon;
        }
    }
    public static void addTooltip(String text, Color color, ResourceLocation icon) {
        hudItems.add(new ToolTipItem(text, color, icon));
    }

    public static void generateTooltip() {
        hudItems.clear();

        TriConsumer<String,Color,String> addTooltip = (key, color, icon) ->
                WandTooltipRenderer.addTooltip(Component.translatable(key).getString(), color, ResourceLocation.fromNamespaceAndPath(MOD_ID, icon));

        TriConsumer<KeyMapping,Color ,String> addKeyTooltip = (key, color, icon) ->
                addTooltip.accept(Component.translatable(key.getName()).getString() + "(" + key.getTranslatedKeyMessage().getString() + ")", color, icon);

        if (switchRenderMode.isDown()) {
            addKeyTooltip.accept(switchRenderMode, new Color(200, 255, 200, 200),"textures/gui/hotkey.png");
            addTooltip.accept("lucidity.info.wand.switchWandMode", new Color(255, 255, 255, 200), "textures/gui/mouse_middle.png");
            addTooltip.accept("lucidity.info.wand.switchRenderingNext", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            addTooltip.accept("lucidity.info.wand.switchRenderingLast", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
        } else if (addArea.isDown()) {
            addKeyTooltip.accept(addArea, new Color(200, 255, 200, 200),"textures/gui/hotkey.png");
            if (APPLY_TARGET_MODE.getOptionListValue() != WandActionsManager.WandApplyToMode.APPLY_TO_PARTICLES) {
                addTooltip.accept("lucidity.info.wand.addSpecific", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            }
            if (pos1 != null && pos2 != null) {
                addTooltip.accept("lucidity.info.wand.addArea", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
            }
        } else if (deleteArea.isDown()) {
            addKeyTooltip.accept(deleteArea,new Color(200, 255, 200, 200), "textures/gui/hotkey.png");
            addTooltip.accept("lucidity.info.wand.delete", new Color(255, 180, 180, 200), "textures/gui/mouse_right.png");
            if (pos1 != null && pos2 != null) {
                addTooltip.accept("lucidity.info.wand.cut", new Color(255, 200, 200, 200), "textures/gui/mouse_left.png");
            }
        } else {
            if (pos1 == null) {
                addTooltip.accept("lucidity.info.wand.selectP1", new Color(255, 255, 255, 200), "textures/gui/mouse_left.png");
            }
            if (pos2 == null) {
                addTooltip.accept("lucidity.info.wand.selectP2", new Color(255, 255, 255, 200), "textures/gui/mouse_right.png");
            }
            if (pos1 != null || pos2 != null) {
                addTooltip.accept("lucidity.info.wand.clearSelection", new Color(255, 255, 255, 200), "textures/gui/hotkey.png");
            }
            if (pos1 != null && pos2 != null) {
                if (!addArea.isDown()) {
                    addKeyTooltip.accept(addArea, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
                }
                if (!deleteArea.isDown()) {
                    addKeyTooltip.accept(deleteArea, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
                }
            }
            if (!switchRenderMode.isDown()) {
                addKeyTooltip.accept(switchRenderMode, new Color(255, 255, 255, 200),"textures/gui/hotkey.png");
            }
        }
    }
    public static void renderWandTooltip(GuiGraphics context) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        assert player != null;
        boolean shouldSelect = player.getMainHandItem().getItem() == wand;
        if (!shouldSelect || client.options.hideGui) {
            return;
        }
        generateTooltip();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGui))
                .add(right.scale(size / 2.0)));

        return triangle;
    }

    private int getColorWithAlpha(float alpha) {
        int a = (int)(alpha * 255);
        int r = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().r*255);
        int g = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().g*255);
        int b = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().b*255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    @Override
    public void discard(){
        subRayShape.discard();
        super.discard();
    }
}
