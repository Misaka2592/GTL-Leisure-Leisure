package com.misaka.gtlleisureaddon.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Small helpers for mutating an existing {@link GTRecipe} in modifiers.
 */
public final class RecipeEdits {

    private RecipeEdits() {}

    public static boolean producesItem(@NotNull GTRecipe recipe, @NotNull ResourceLocation itemId) {
        var outputs = recipe.outputs.get(ItemRecipeCapability.CAP);
        if (outputs == null) {
            return false;
        }
        for (Content content : outputs) {
            for (ItemStack stack : ItemRecipeCapability.CAP.of(content.content).getItems()) {
                if (!stack.isEmpty() && itemId.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void scaleFluidInputs(@NotNull GTRecipe recipe, int percent) {
        var fluids = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (fluids == null) {
            return;
        }
        for (Content content : fluids) {
            FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content);
            long scaledAmount = Math.max(1, ingredient.getAmount() * percent / 100L);
            content.content = rescaleFluidIngredient(ingredient, scaledAmount);
        }
    }

    public static void setItemOutputCount(@NotNull GTRecipe recipe, @NotNull ResourceLocation itemId, int count) {
        var outputs = recipe.outputs.get(ItemRecipeCapability.CAP);
        if (outputs == null) {
            return;
        }
        for (Content content : outputs) {
            for (ItemStack stack : ItemRecipeCapability.CAP.of(content.content).getItems()) {
                if (!stack.isEmpty() && itemId.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                    content.content = SizedIngredient.create(stack.copyWithCount(count));
                    return;
                }
            }
        }
    }

    public static void setItemInputCount(@NotNull GTRecipe recipe, @NotNull ResourceLocation itemId, int count) {
        var inputs = recipe.inputs.get(ItemRecipeCapability.CAP);
        if (inputs == null) {
            return;
        }
        for (Content content : inputs) {
            if (setSizedCount(content, itemId, count)) {
                return;
            }
        }
    }

    private static boolean setSizedCount(Content content, ResourceLocation itemId, int count) {
        Object raw = content.content;
        if (raw instanceof SizedIngredient sized) {
            for (ItemStack stack : sized.getItems()) {
                if (matchesItem(stack, itemId)) {
                    content.content = SizedIngredient.create(sized.getInner(), count);
                    return true;
                }
            }
            return false;
        }
        for (ItemStack stack : ItemRecipeCapability.CAP.of(raw).getItems()) {
            if (matchesItem(stack, itemId)) {
                content.content = SizedIngredient.create(Ingredient.of(stack.getItem()), count);
                return true;
            }
        }
        return false;
    }

    private static boolean matchesItem(ItemStack stack, ResourceLocation itemId) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && (itemId.equals(key) || itemId.getPath().equals(key.getPath()));
    }

    private static FluidIngredient rescaleFluidIngredient(FluidIngredient ingredient, long amount) {
        FluidStack[] stacks = ingredient.getStacks();
        if (stacks != null && stacks.length > 0) {
            Fluid[] fluids = new Fluid[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                fluids[i] = stacks[i].getFluid();
            }
            return FluidIngredient.of(amount, fluids);
        }
        if (ingredient.values != null && ingredient.values.length > 0) {
            return FluidIngredient.fromValues(
                    Arrays.stream(ingredient.values).map(FluidIngredient.Value::copy),
                    amount,
                    ingredient.getNbt());
        }
        return FluidIngredient.of(amount, Fluids.EMPTY);
    }

    public static void setDuration(@NotNull GTRecipe recipe, int duration) {
        recipe.duration = duration;
    }

    public static void setEUt(@NotNull GTRecipe recipe, long eu) {
        var euList = recipe.tickInputs.get(EURecipeCapability.CAP);
        if (euList == null || euList.isEmpty()) {
            euList = new ArrayList<>();
            euList.add(new Content(eu, ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null));
            recipe.tickInputs.put(EURecipeCapability.CAP, euList);
            return;
        }
        euList.get(0).content = eu;
    }

    public static long getEUt(@NotNull GTRecipe recipe) {
        var euList = recipe.tickInputs.get(EURecipeCapability.CAP);
        if (euList == null || euList.isEmpty()) {
            return 0;
        }
        return EURecipeCapability.CAP.of(euList.get(0).content);
    }

    public static void putDataInt(@NotNull GTRecipe recipe, String key, int value) {
        recipe.data.putInt(key, value);
    }
}