package com.misaka.gtlleisureaddon.api.recipe.handler;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Fully replaces a matched recipe, typically by rebuilding with
 * {@link com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder}.
 */
@FunctionalInterface
public interface RecipeReplacer {

    @NotNull
    GTRecipe replace(@NotNull GTRecipe original);

    static RecipeReplacer when(Predicate<ResourceLocation> matcher, Function<GTRecipe, GTRecipe> replacer) {
        return original -> matcher.test(original.id) ? replacer.apply(original) : original;
    }

    static RecipeReplacer whenId(ResourceLocation id, Function<GTRecipe, GTRecipe> replacer) {
        return when(recipeId -> recipeId.equals(id), replacer);
    }
}