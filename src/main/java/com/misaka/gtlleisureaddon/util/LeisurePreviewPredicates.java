package com.misaka.gtlleisureaddon.util;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import com.gtladd.gtladditions.api.machine.GTLAddPartAbility;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/** Resolves JEI preview click predicates without relying on {@code checkPatternAt} coordinate alignment. */
public final class LeisurePreviewPredicates {

    private LeisurePreviewPredicates() {}

    public static TraceabilityPredicate controllerPredicate(MultiblockMachineDefinition definition) {
        return Predicates.controller(Predicates.blocks(definition.getBlock()))
                .setPreviewCount(1)
                .sort();
    }

    public static TraceabilityPredicate casingPredicate(MultiblockMachineDefinition definition) {
        return Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                .setMinGlobalLimited(1)
                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(Predicates.abilities(GTLAddPartAbility.INSTANCE.getTHREAD_MODIFIER())
                        .setMaxGlobalLimited(1)
                        .setPreviewCount(1))
                .sort();
    }

    public static TraceabilityPredicate decorativePredicate(BlockState state) {
        return Predicates.states(state).sort();
    }

    public static Map<BlockPos, TraceabilityPredicate> buildClickPredicateMap(
                                                                               MultiblockMachineDefinition definition,
                                                                               Map<BlockPos, BlockInfo> blockMap) {
        TraceabilityPredicate controller = controllerPredicate(definition);
        TraceabilityPredicate casing = casingPredicate(definition);
        Map<BlockPos, TraceabilityPredicate> predicates = new Object2ObjectOpenHashMap<>(blockMap.size());

        for (Map.Entry<BlockPos, BlockInfo> entry : blockMap.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            if (state.isAir()) {
                continue;
            }
            if (state.getBlock() == definition.getBlock()) {
                predicates.put(entry.getKey(), controller);
            } else if (LeisureStructureBlocks.isDecorativeSheet(state)) {
                predicates.put(entry.getKey(), decorativePredicate(state));
            } else {
                predicates.put(entry.getKey(), casing);
            }
        }
        return predicates;
    }

    public static TraceabilityPredicate resolveForState(MultiblockMachineDefinition definition, BlockState state) {
        if (state.isAir()) {
            return null;
        }
        if (state.getBlock() == definition.getBlock()) {
            return controllerPredicate(definition);
        }
        if (LeisureStructureBlocks.isDecorativeSheet(state)) {
            return decorativePredicate(state);
        }
        return casingPredicate(definition);
    }
}
