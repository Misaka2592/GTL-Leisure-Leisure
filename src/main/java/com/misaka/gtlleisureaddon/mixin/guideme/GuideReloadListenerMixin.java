package com.misaka.gtlleisureaddon.mixin.guideme;

import guideme.internal.GuideMEClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "guideme.internal.GuideReloadListener", remap = false)
public class GuideReloadListenerMixin {

    @Redirect(
              method = "prepare",
              at = @At(
                       value = "INVOKE",
                       target = "Lguideme/internal/GuideMEClient;isIgnoreTranslatedGuides()Z"),
              remap = false)
    private boolean lleisure$safeIsIgnoreTranslatedGuides(GuideMEClient instance) {
        try {
            return instance.isIgnoreTranslatedGuides();
        } catch (IllegalStateException ignored) {
            // GuideME reads client config during the loading overlay, before Forge has loaded it in dev.
            return false;
        }
    }
}