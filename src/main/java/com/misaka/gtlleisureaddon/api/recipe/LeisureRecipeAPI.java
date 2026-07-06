package com.misaka.gtlleisureaddon.api.recipe;

import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeModifier;
import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeRemoval;
import com.misaka.gtlleisureaddon.api.recipe.handler.RecipeReplacer;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Public entry point for recipe removal, replacement, modification, and datagen registration.
 * <p>
 * Runtime handlers run when any recipe (including KubeJS) is added to {@code GTRecipeLookup}.
 */
public final class LeisureRecipeAPI {

    private static boolean initialized;

    private LeisureRecipeAPI() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LeisureBuiltinRecipeHandlers.register();
    }

    public static void remove(@NotNull ResourceLocation... ids) {
        LeisureRecipeRegistry.addRemoval(RecipeRemoval.ids(ids));
    }

    public static void removeWhen(@NotNull RecipeRemoval removal) {
        LeisureRecipeRegistry.addRemoval(removal);
    }

    public static void replace(@NotNull ResourceLocation id, @NotNull Function<GTRecipe, GTRecipe> replacer) {
        LeisureRecipeRegistry.addReplacer(RecipeReplacer.whenId(id, replacer));
    }

    public static void replace(@NotNull RecipeReplacer replacer) {
        LeisureRecipeRegistry.addReplacer(replacer);
    }

    public static void modify(@NotNull RecipeModifier modifier) {
        LeisureRecipeRegistry.addModifier(modifier);
    }

    public static void register(@NotNull Consumer<Consumer<FinishedRecipe>> registrar) {
        LeisureRecipeRegistry.addRegistration(registrar);
    }

    /**
     * @return processed recipe, or {@code null} if the recipe should be skipped
     */
    public static @Nullable GTRecipe processRecipe(@NotNull GTRecipe recipe) {
        return LeisureRecipeRegistry.process(recipe);
    }
}