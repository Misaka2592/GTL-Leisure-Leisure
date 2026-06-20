package com.misaka.gtlleisureaddon.util;

import com.misaka.gtlleisureaddon.mixin.gtceu.MBPatternAccessor;
import com.misaka.gtlleisureaddon.mixin.gtceu.PatternPreviewWidgetAccessor;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LeisurePreviewClickHandler {

    private LeisurePreviewClickHandler() {}

    public static SlotWidget[] handle(
                                      PatternPreviewWidgetAccessor widget,
                                      int pageIndex,
                                      Object[] patternPages,
                                      List<SimplePredicate> predicateBuffer,
                                      SlotWidget[] existingCandidates,
                                      SlotWidget[] partSlots,
                                      Function<Widget, Widget> addWidget,
                                      Consumer<Widget> removeWidget,
                                      BlockPos pos) {
        if (patternPages == null || pageIndex < 0 || pageIndex >= patternPages.length) {
            return null;
        }

        MBPatternAccessor page = (MBPatternAccessor) patternPages[pageIndex];
        BlockInfo blockInfo = page.getBlockMap().get(pos);
        if (blockInfo == null) {
            return null;
        }

        MultiblockMachineDefinition definition = widget.getControllerDefinition();
        BlockState state = blockInfo.getBlockState();
        TraceabilityPredicate predicate = LeisurePreviewPredicates.resolveForState(definition, state);
        if (predicate == null) {
            return null;
        }

        predicateBuffer.clear();
        predicateBuffer.addAll(predicate.common);
        predicateBuffer.addAll(predicate.limited);
        predicateBuffer.removeIf(simple -> simple == null || simple.candidates == null);

        if (existingCandidates != null) {
            for (SlotWidget candidate : existingCandidates) {
                removeWidget.accept(candidate);
            }
        }

        List<List<ItemStack>> candidateStacks = new ArrayList<>();
        List<List<Component>> predicateTips = new ArrayList<>();
        for (SimplePredicate simplePredicate : predicateBuffer) {
            List<ItemStack> stacks = simplePredicate.getCandidates();
            if (!stacks.isEmpty()) {
                candidateStacks.add(stacks);
                predicateTips.add(simplePredicate.getToolTips(predicate));
            }
        }

        if (candidateStacks.isEmpty()) {
            var level = Minecraft.getInstance().level;
            ItemStack fallback = state.getBlock().getCloneItemStack(level, pos, state);
            if (!fallback.isEmpty()) {
                candidateStacks.add(List.of(fallback));
                predicateTips.add(List.of());
            }
        }

        if (candidateStacks.isEmpty()) {
            return null;
        }

        int maxCol = Math.max(1, (160 - (((partSlots.length - 1) / 9 + 1) * 18) - 35) % 18);
        SlotWidget[] created = new SlotWidget[candidateStacks.size()];
        CycleItemStackHandler itemHandler = new CycleItemStackHandler(candidateStacks);
        for (int slotIndex = 0; slotIndex < candidateStacks.size(); slotIndex++) {
            int finalIndex = slotIndex;
            created[slotIndex] = new SlotWidget(itemHandler, slotIndex, 3 + (slotIndex / maxCol) * 18,
                    3 + (slotIndex % maxCol) * 18, false, false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setBackgroundTexture(new ColorRectTexture(0x4fffffff))
                    .setOnAddedTooltips((slot, list) -> list.addAll(predicateTips.get(finalIndex)));
            addWidget.apply(created[slotIndex]);
        }
        return created;
    }
}