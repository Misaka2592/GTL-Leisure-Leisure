package com.misaka.gtlleisureaddon.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "org.gtlcore.gtlcore.api.gui.PatternPreviewWidget$MBPattern", remap = false)
public interface MBPatternAccessor {

    @Accessor
    Map<BlockPos, BlockInfo> getBlockMap();

    @Accessor
    Map<BlockPos, TraceabilityPredicate> getPredicateMap();

    @Mutable
    @Accessor
    void setPredicateMap(Map<BlockPos, TraceabilityPredicate> predicateMap);

    @Accessor
    IMultiController getControllerBase();
}