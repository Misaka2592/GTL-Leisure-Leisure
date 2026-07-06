package com.misaka.gtlleisureaddon.mixin.gtceu;

import com.misaka.gtlleisureaddon.common.LeisureTooltips;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.network.chat.Component;

import org.gtlcore.gtlcore.common.machine.multiblock.generator.DysonSphereMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public class WorkableElectricMultiblockMachineMixin {

    @Inject(method = "addDisplayText", at = @At("RETURN"), remap = false)
    private void gtlLeisure$appendDisplayText(List<Component> textList, CallbackInfo ci) {
        WorkableElectricMultiblockMachine machine = (WorkableElectricMultiblockMachine) (Object) this;
        if (machine instanceof DysonSphereMachine) {
            return;
        }
        if (machine.isFormed() && machine.recipeLogic.isActive()) {
            textList.add(LeisureTooltips.fullColor("tooltip.lleisure.enhanced_runtime"));
        }
    }
}