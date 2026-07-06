package com.misaka.gtlleisureaddon.api.recipe.handlers;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;
import com.misaka.gtlleisureaddon.api.recipe.RecipeEdits;
import com.misaka.gtlleisureaddon.api.recipe.RecipeIds;
import com.misaka.gtlleisureaddon.config.ConfigHolder;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Overrides KubeJS {@code dyson_swarm_module} precision assembler recipe.
 * <p>
 * Mutates the recipe in place so JEI (RecipeManager) and machines (GTRecipeLookup) see the same data.
 */
public final class DysonSwarmModuleRecipeHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation RECIPE_ID = RecipeIds.of("kubejs", "precision_assembler", "dyson_swarm_module");
    private static final ResourceLocation OUTPUT_ITEM = ResourceLocation.fromNamespaceAndPath("kubejs", "dyson_swarm_module");
    private static final String MODIFIED_FLAG = GTLLeisureAddon.MOD_ID + ":dyson_swarm_module_override";
    private static final int FLUID_SCALE_PERCENT = 10;
    private static final int OUTPUT_COUNT = 1024;

    private DysonSwarmModuleRecipeHandler() {}

    public static GTRecipe modify(GTRecipe original) {
        if (ConfigHolder.INSTANCE == null || !ConfigHolder.INSTANCE.recipes.dysonSwarmModuleOverride) {
            return original;
        }
        if (!matches(original) || original.data.getBoolean(MODIFIED_FLAG)) {
            return original;
        }

        RecipeEdits.scaleFluidInputs(original, FLUID_SCALE_PERCENT);
        RecipeEdits.setItemOutputCount(original, OUTPUT_ITEM, OUTPUT_COUNT);
        original.data.putBoolean(MODIFIED_FLAG, true);
        LOGGER.info("[{}] Applied dyson_swarm_module recipe override for {}", GTLLeisureAddon.MOD_ID, original.id);
        return original;
    }

    private static boolean matches(GTRecipe recipe) {
        return RECIPE_ID.equals(recipe.id) || RecipeEdits.producesItem(recipe, OUTPUT_ITEM);
    }
}