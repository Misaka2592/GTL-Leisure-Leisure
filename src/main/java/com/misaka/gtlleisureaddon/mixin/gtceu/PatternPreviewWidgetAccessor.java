package com.misaka.gtlleisureaddon.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = org.gtlcore.gtlcore.api.gui.PatternPreviewWidget.class, remap = false)
public interface PatternPreviewWidgetAccessor {

    @Accessor
    MultiblockMachineDefinition getControllerDefinition();
}