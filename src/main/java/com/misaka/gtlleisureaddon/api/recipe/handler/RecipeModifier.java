package com.misaka.gtlleisureaddon.api.recipe.handler;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * In-place or copy-based mutation applied when a recipe enters {@code GTRecipeLookup}.
 * Return the same instance to keep, or a new {@link GTRecipe} to replace.
 */
@FunctionalInterface
public interface RecipeModifier {

    @NotNull
    GTRecipe modify(@NotNull GTRecipe recipe);

    static RecipeModifier when(Predicate<ResourceLocation> matcher, RecipeModifier modifier) {
        return recipe -> matcher.test(recipe.id) ? modifier.modify(recipe) : recipe;
    }

    static RecipeModifier whenId(ResourceLocation id, RecipeModifier modifier) {
        return when(recipeId -> recipeId.equals(id), modifier);
    }
}
