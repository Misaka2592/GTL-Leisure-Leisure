package com.misaka.gtlleisureaddon.dyson;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import org.gtlcore.gtlcore.common.machine.multiblock.generator.DysonSphereMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class DysonSphereDamageLogic {

    private static final ResourceLocation DYSON_SWARM_MODULE =
            ResourceLocation.fromNamespaceAndPath("kubejs", "dyson_swarm_module");

    private static final int BATCH_REPAIR_THRESHOLD_MULTIPLIER = 2;
    private static final int MAX_BATCH_REPAIR_STEPS = 100;

    private DysonSphereDamageLogic() {}

    public static long countAvailableModules(DysonSphereMachine machine) {
        Item moduleItem = getModuleItem();
        if (moduleItem == null) {
            return 0L;
        }
        Set<ItemStackTransfer> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        long total = 0L;
        for (IRecipeHandler<?> handler : collectRecipeInputHandlers(machine)) {
            total = ModuleItemCounts.add(total, countInRecipeHandler(handler, moduleItem, seen));
        }
        return total;
    }

    /**
     * Consume excess modules above {@code 2 * repairCost} to reduce damage by 1% per batch.
     *
     * @return updated damage percent
     */
    public static int applyBatchRepair(DysonSphereMachine machine, int damagePercent) {
        if (!canRunOnServer(machine) || damagePercent <= 0) {
            return damagePercent;
        }
        Item moduleItem = getModuleItem();
        if (moduleItem == null) {
            return damagePercent;
        }
        List<IRecipeHandler<?>> handlers = collectRecipeInputHandlers(machine);
        if (handlers.isEmpty()) {
            return damagePercent;
        }
        long repairCost = DysonSphereLaunchLogic.getRepairModuleCost(machine.getDysonSphereData(), damagePercent);
        if (repairCost <= 0L) {
            return damagePercent;
        }
        long available = countAvailableModules(machine);
        long threshold = ModuleItemCounts.multiply(repairCost, BATCH_REPAIR_THRESHOLD_MULTIPLIER);
        if (available <= threshold) {
            return damagePercent;
        }
        long excess = available - threshold;
        long batches = Math.min(excess / repairCost, MAX_BATCH_REPAIR_STEPS);
        batches = Math.min(batches, damagePercent);
        if (batches <= 0L) {
            return damagePercent;
        }
        long toConsume = ModuleItemCounts.multiply(repairCost, batches);
        if (consumeModules(handlers, moduleItem, toConsume) < toConsume) {
            return damagePercent;
        }
        return (int) Math.max(0L, damagePercent - batches);
    }

    /**
     * Consume one repair batch to skip a single incoming +1% damage tick.
     */
    public static boolean trySkipDamageTick(DysonSphereMachine machine, int damagePercent) {
        if (!canRunOnServer(machine)) {
            return false;
        }
        Item moduleItem = getModuleItem();
        if (moduleItem == null) {
            return false;
        }
        List<IRecipeHandler<?>> handlers = collectRecipeInputHandlers(machine);
        if (handlers.isEmpty()) {
            return false;
        }
        long repairCost = DysonSphereLaunchLogic.getRepairModuleCost(machine.getDysonSphereData(), damagePercent);
        return repairCost > 0L && consumeModules(handlers, moduleItem, repairCost) >= repairCost;
    }

    private static boolean canRunOnServer(DysonSphereMachine machine) {
        return DysonSphereLaunchLogic.isEnabled()
                && machine.getLevel() != null
                && !machine.getLevel().isClientSide();
    }

    private static Item getModuleItem() {
        Item moduleItem = BuiltInRegistries.ITEM.get(DYSON_SWARM_MODULE);
        if (moduleItem == null || moduleItem == Items.AIR) {
            return null;
        }
        return moduleItem;
    }

    /**
     * Recipe-facing import handlers only (same source GTCEu uses for item inputs).
     */
    private static List<IRecipeHandler<?>> collectRecipeInputHandlers(DysonSphereMachine machine) {
        List<IRecipeHandler<?>> handlers = machine.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP);
        if (handlers != null && !handlers.isEmpty()) {
            return handlers;
        }
        List<IRecipeHandler<?>> fallback = new ArrayList<>();
        for (var part : machine.getParts()) {
            if (part instanceof ItemBusPartMachine bus && bus.getInventory().handlerIO == IO.IN) {
                fallback.add(bus.getCombinedInventory());
            }
        }
        return fallback;
    }

    private static long countInRecipeHandler(
            IRecipeHandler<?> handler, Item moduleItem, Set<ItemStackTransfer> seen) {
        if (handler instanceof ItemHandlerProxyRecipeTrait proxy) {
            long total = 0L;
            for (var child : proxy.getHandlers()) {
                if (child instanceof NotifiableItemStackHandler itemHandler) {
                    total = ModuleItemCounts.add(total, countInItemHandler(itemHandler, moduleItem, seen));
                }
            }
            return total;
        }
        if (handler instanceof NotifiableItemStackHandler itemHandler) {
            return countInItemHandler(itemHandler, moduleItem, seen);
        }
        return 0L;
    }

    private static long countInItemHandler(
            NotifiableItemStackHandler handler, Item moduleItem, Set<ItemStackTransfer> seen) {
        if (handler.handlerIO != IO.IN || !seen.add(handler.storage)) {
            return 0L;
        }
        long total = 0L;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!isConsumableModuleStack(handler, slot, stack, moduleItem)) {
                continue;
            }
            total = ModuleItemCounts.add(total, ModuleItemCounts.readCount(stack));
        }
        return total;
    }

    private static boolean isConsumableModuleStack(
            NotifiableItemStackHandler handler, int slot, ItemStack stack, Item moduleItem) {
        return !stack.isEmpty()
                && isModuleStack(stack, moduleItem)
                && handler.isItemValid(slot, stack);
    }

    private static boolean isModuleStack(ItemStack stack, Item moduleItem) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(moduleItem)) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return DYSON_SWARM_MODULE.equals(id);
    }

    private static long consumeModules(List<IRecipeHandler<?>> handlers, Item moduleItem, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long consumed = 0L;
        long remaining = amount;
        while (remaining > 0L) {
            int chunk = (int) Math.min(remaining, Integer.MAX_VALUE);
            int chunkRemaining = chunk;
            for (IRecipeHandler<?> handler : handlers) {
                chunkRemaining = consumeFromRecipeHandler(handler, moduleItem, chunkRemaining);
                if (chunkRemaining <= 0) {
                    break;
                }
            }
            long chunkConsumed = chunk - chunkRemaining;
            if (chunkConsumed <= 0L) {
                break;
            }
            consumed = ModuleItemCounts.add(consumed, chunkConsumed);
            remaining -= chunkConsumed;
        }
        return consumed;
    }

    private static int consumeFromRecipeHandler(IRecipeHandler<?> handler, Item moduleItem, int amount) {
        if (amount <= 0) {
            return 0;
        }
        List<Ingredient> toConsume = new ArrayList<>();
        toConsume.add(SizedIngredient.create(Ingredient.of(moduleItem), amount));
        List<?> leftover = handler.handleRecipe(IO.IN, null, toConsume, null, false);
        return interpretLeftoverAmount(leftover, amount);
    }

    private static int interpretLeftoverAmount(List<?> leftover, int requested) {
        if (leftover == null || leftover.isEmpty()) {
            return 0;
        }
        Object remaining = leftover.get(0);
        if (remaining instanceof SizedIngredient sized) {
            return sized.getAmount();
        }
        return requested;
    }
}
