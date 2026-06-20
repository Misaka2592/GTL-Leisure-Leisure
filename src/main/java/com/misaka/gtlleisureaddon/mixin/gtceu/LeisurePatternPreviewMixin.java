package com.misaka.gtlleisureaddon.mixin.gtceu;

import com.misaka.gtlleisureaddon.util.LeisureMultiBlockStructure;
import com.misaka.gtlleisureaddon.util.LeisurePreviewPredicates;

import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GTLCore replaces GTCEu's JEI preview with {@code org.gtlcore.gtlcore.api.gui.PatternPreviewWidget}.
 * Patch {@code predicateMap} after preview init so click-to-replace works for decorative blocks.
 */
@Mixin(value = org.gtlcore.gtlcore.api.gui.PatternPreviewWidget.class, remap = false)
public abstract class LeisurePatternPreviewMixin {

    @Inject(method = "initializePattern", at = @At("RETURN"), remap = false)
    private void lleisure$patchPreviewPredicates(
                                                 MultiblockShapeInfo shapeInfo,
                                                 CallbackInfoReturnable<?> cir) {
        PatternPreviewWidgetAccessor widget = (PatternPreviewWidgetAccessor) (Object) this;
        if (!widget.getControllerDefinition().getId().getPath()
                .equals(LeisureMultiBlockStructure.QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID)) {
            return;
        }
        Object pattern = cir.getReturnValue();
        if (pattern == null) {
            return;
        }
        MBPatternAccessor accessor = (MBPatternAccessor) pattern;
        accessor.setPredicateMap(LeisurePreviewPredicates.buildClickPredicateMap(
                widget.getControllerDefinition(),
                accessor.getBlockMap()));
    }
}