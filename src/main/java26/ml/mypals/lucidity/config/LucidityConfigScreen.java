package ml.mypals.lucidity.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Native 26.3 extraction-based feature screen. */
public final class LucidityConfigScreen extends Screen {
    private final Screen parent;

    public LucidityConfigScreen(Screen parent) {
        super(Component.literal("Lucidity features"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = Math.max(8, (this.width - 600) / 2);
        int top = 28;
        int columnWidth = 150;
        LucidityConfig.Feature[] features = LucidityConfig.Feature.values();
        for (int index = 0; index < features.length; index++) {
            LucidityConfig.Feature feature = features[index];
            int column = index % 4;
            int row = index / 4;
            int x = left + column * columnWidth;
            int y = top + row * 22;
            this.addRenderableWidget(Button.builder(label(feature), button -> {
                LucidityConfig.toggle(feature);
                button.setMessage(label(feature));
            }).bounds(x, y, 146, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Save and close"), button -> onClose())
                .bounds(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    private static Component label(LucidityConfig.Feature feature) {
        return Component.literal((LucidityConfig.isEnabled(feature) ? "[ON] " : "[OFF] ") + feature.label());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, "Lucidity 2.0.2 • Minecraft 26.3", this.width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        LucidityConfig.save();
        Minecraft.getInstance().setScreenAndShow(parent);
    }
}





