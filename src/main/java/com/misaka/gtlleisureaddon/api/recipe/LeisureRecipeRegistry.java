package com.misaka.gtlleisureaddon.api.recipe;

import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeModifier;
import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeRemoval;
import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeReplacer;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Internal store for recipe handlers. Use {@link LeisureRecipeAPI} to register.
 */
public final class LeisureRecipeRegistry {

    private static final List<RecipeRemoval> REMOVALS = new ArrayList<>();
    private static final List<RecipeReplacer> REPLACERS = new ArrayList<>();
    private static final List<RecipeModifier> MODIFIERS = new ArrayList<>();

    private LeisureRecipeRegistry() {}

    static void addRemoval(@NotNull RecipeRemoval removal) {
        REMOVALS.add(removal);
    }

    static void addReplacer(@NotNull RecipeReplacer replacer) {
        REPLACERS.add(replacer);
    }

    static void addModifier(@NotNull RecipeModifier modifier) {
        MODIFIERS.add(modifier);
    }

    static void addRegistration(@NotNull Consumer<Consumer<FinishedRecipe>> registrar) {
        LeisureRecipeRegistration.register(registrar);
    }

    /**
     * @return processed recipe, or {@code null} if the recipe should be skipped
     */
    static @Nullable GTRecipe process(@NotNull GTRecipe recipe) {
        ResourceLocation id = recipe.id;
        for (RecipeRemoval removal : REMOVALS) {
            if (removal.shouldRemove(id)) {
                return null;
            }
        }

        GTRecipe current = recipe;
        for (RecipeReplacer replacer : REPLACERS) {
            GTRecipe replaced = replacer.replace(current);
            if (replaced != current) {
                current = replaced;
                break;
            }
        }

        for (RecipeModifier modifier : MODIFIERS) {
            current = modifier.modify(current);
        }

        return current;
    }

    static void clear() {
        REMOVALS.clear();
        REPLACERS.clear();
        MODIFIERS.clear();
        LeisureRecipeRegistration.clear();
    }
}