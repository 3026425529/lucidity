package ml.mypals.lucidity.mixin;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Disables obfuscated (Zalgo-style) glyph selection while enabled. */
@Mixin(Style.class)
public abstract class LucidityZalgoMixin {
    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    private void lucidity$deobfuscate(CallbackInfoReturnable<Boolean> callback) {
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ZALGO_TEXT_DEOBF)) {
            callback.setReturnValue(false);
        }
    }
}

