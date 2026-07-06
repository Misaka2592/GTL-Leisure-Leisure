package com.misaka.gtlleisureaddon.mixin.gtlcore;

import com.misaka.gtlleisureaddon.api.recipe.RecipeEdits;
import com.misaka.gtlleisureaddon.common.LeisureTooltips;
import com.misaka.gtlleisureaddon.dyson.DysonSphereDamageLogic;
import com.misaka.gtlleisureaddon.dyson.DysonSphereLaunchLogic;
import com.misaka.gtlleisureaddon.dyson.DysonSpherePowerFormat;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.gtlcore.gtlcore.common.machine.multiblock.generator.DysonSphereMachine;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DysonSphereMachine.class, remap = false)
public abstract class DysonSphereMachineMixin {

    private static final ResourceLocation DYSON_SWARM_MODULE =
            ResourceLocation.fromNamespaceAndPath("kubejs", "dyson_swarm_module");

    @Shadow(remap = false)
    private int DysonSphereData;

    @Shadow(remap = false)
    private int DysonSpheredamageData;

    @Inject(method = "recipeModifier", at = @At("RETURN"), cancellable = true, remap = false)
    private static void lleisure$modifyLaunchRecipe(MetaMachine machine, GTRecipe recipe,
                                                     CallbackInfoReturnable<GTRecipe> cir) {
        if (!DysonSphereLaunchLogic.isEnabled()) {
            return;
        }
        if (!(machine instanceof DysonSphereMachine engine) || !lleisure$isLaunchRecipe(recipe)) {
            return;
        }
        GTRecipe result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        GTRecipe adjusted = result.copy();
        int modules = DysonSphereLaunchLogic.getModulesPerLaunch(engine.getDysonSphereData());
        RecipeEdits.setItemInputCount(adjusted, DYSON_SWARM_MODULE, modules);
        cir.setReturnValue(adjusted);
    }

    @ModifyConstant(method = "onWorking", constant = @Constant(intValue = 10000), require = 1, remap = false)
    private static int lleisure$maxModulesOnWorking(int ignored) {
        return DysonSphereLaunchLogic.isEnabled()
                ? DysonSphereLaunchLogic.getMaxModules()
                : ignored;
    }

    @ModifyConstant(method = "beforeWorking", constant = @Constant(intValue = 10000), require = 1, remap = false)
    private static int lleisure$maxModulesBeforeWorking(int ignored) {
        return DysonSphereLaunchLogic.isEnabled()
                ? DysonSphereLaunchLogic.getLaunchCapInclusiveLimit()
                : ignored;
    }

    @Redirect(
            method = "onWorking",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lorg/gtlcore/gtlcore/common/machine/multiblock/generator/DysonSphereMachine;DysonSphereData:I"),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "intValue=200", remap = false),
                    to = @At(value = "CONSTANT", args = "intValue=20", remap = false)),
            require = 1,
            remap = false)
    private void lleisure$applyLaunchGain(DysonSphereMachine instance, int valueAfterVanillaIncrement) {
        DysonSphereMachineMixin self = (DysonSphereMachineMixin) (Object) instance;
        if (!DysonSphereLaunchLogic.isEnabled()) {
            self.DysonSphereData = valueAfterVanillaIncrement;
            return;
        }
        int current = valueAfterVanillaIncrement - 1;
        int gain = DysonSphereLaunchLogic.computeLaunchGain(current);
        self.DysonSphereData = DysonSphereLaunchLogic.clampLaunchTotal(current, gain);
    }

    @Redirect(
            method = "onWorking",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"),
            slice = @Slice(from = @At(value = "CONSTANT", args = "intValue=20", remap = false), to = @At("RETURN")),
            require = 1,
            remap = true)
    private double lleisure$interceptDamageRoll() {
        DysonSphereMachine instance = (DysonSphereMachine) (Object) this;
        double roll = Math.random();
        if (!DysonSphereLaunchLogic.isEnabled()) {
            return roll;
        }
        if (instance.getRecipeLogic().getDuration() != 20
                || instance.getRecipeLogic().getProgress() != 19
                || instance.getDysonSphereData() <= 0) {
            return roll;
        }
        DysonSphereMachineMixin self = (DysonSphereMachineMixin) (Object) instance;
        int damage = self.DysonSpheredamageData;
        int repaired = DysonSphereDamageLogic.applyBatchRepair(instance, damage);
        if (repaired != damage) {
            self.DysonSpheredamageData = repaired;
            damage = repaired;
        }
        double threshold = 0.01D * (1.0D + (double) instance.getDysonSphereData() / 128.0D);
        if (roll >= threshold) {
            return roll;
        }
        if (damage > 99) {
            return roll;
        }
        if (DysonSphereDamageLogic.trySkipDamageTick(instance, damage)) {
            return Math.max(threshold, 1.0D);
        }
        return roll;
    }

    @Redirect(
            method = "addDisplayText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"),
            remap = true)
    private MutableComponent lleisure$redirectDisplayText(String key, Object[] args) {
        if (!DysonSphereLaunchLogic.isEnabled()) {
            return Component.translatable(key, args);
        }
        if ("gtceu.machine.dyson_sphere.number".equals(key)) {
            return Component.translatable("lleisure.dyson_sphere.launch_count", args[0], DysonSphereLaunchLogic.getMaxModules());
        }
        if ("gtceu.machine.dyson_sphere.voltage".equals(key)) {
            DysonSphereMachine machine = (DysonSphereMachine) (Object) this;
            return DysonSpherePowerFormat.formatCurrentOutput(machine.getDysonSphereData());
        }
        return Component.translatable(key, args);
    }

    @Inject(method = "addDisplayText", at = @At("RETURN"), remap = false)
    private void lleisure$appendDisplayExtras(List<Component> textList, CallbackInfo ci) {
        if (!DysonSphereLaunchLogic.isEnabled()) {
            return;
        }
        DysonSphereMachine machine = (DysonSphereMachine) (Object) this;
        if (!machine.isFormed()) {
            return;
        }
        int damage = machine.getDysonSpheredamageData();
        int launched = machine.getDysonSphereData();
        long repairCost = DysonSphereLaunchLogic.getRepairModuleCost(launched, damage);
        textList.add(Component.translatable(
                "lleisure.dyson_sphere.repair_modules",
                DysonSpherePowerFormat.formatModuleCount(repairCost)));
        textList.add(Component.translatable(
                "lleisure.dyson_sphere.repair_modules_in_bus",
                DysonSpherePowerFormat.formatModuleCount(DysonSphereDamageLogic.countAvailableModules(machine))));
        if (damage < 60) {
            textList.add(Component.translatable("lleisure.dyson_sphere.repair_modules_halved"));
        }
        if (!machine.getRecipeLogic().isActive()) {
            return;
        }
        GTRecipe recipe = machine.getRecipeLogic().getLastRecipe();
        if (recipe == null) {
            return;
        }
        if (lleisure$isLaunchRecipe(recipe)) {
            double efficiency = DysonSphereLaunchLogic.getLaunchEfficiency(machine.getDysonSphereData());
            textList.add(LeisureTooltips.fullColor(Component.translatable(
                    "lleisure.dyson_sphere.deployment_efficiency",
                    DysonSpherePowerFormat.formatEfficiencyPercent(efficiency))));
            textList.add(Component.translatable(
                    "lleisure.dyson_sphere.modules_per_launch",
                    DysonSpherePowerFormat.formatModuleCount(
                            DysonSphereLaunchLogic.getModulesPerLaunch(launched))));
            textList.add(Component.translatable(
                    "lleisure.dyson_sphere.deployment_batch",
                    DysonSpherePowerFormat.formatModuleCount(
                            DysonSphereLaunchLogic.getDeploymentBatch(launched))));
        }
    }

    private static boolean lleisure$isLaunchRecipe(GTRecipe recipe) {
        return RecipeHelper.getOutputEUt(recipe) != GTValues.V[GTValues.MAX];
    }
}
