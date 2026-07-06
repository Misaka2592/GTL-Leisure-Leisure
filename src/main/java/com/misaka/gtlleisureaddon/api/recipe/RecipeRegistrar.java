package com.misaka.gtlleisureaddon.api.recipe;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen helpers for registering new GregTech recipes from {@code IGTAddon#addRecipes}.
 */
public final class RecipeRegistrar {

    private RecipeRegistrar() {}

    public static void register(@NotNull GTRecipeType type, @NotNull String path,
                                @NotNull Consumer<GTRecipeBuilder> configure,
                                @NotNull Consumer<FinishedRecipe> provider) {
        register(type, GTLLeisureAddon.id(path), configure, provider);
    }

    public static void register(@NotNull GTRecipeType type, @NotNull ResourceLocation id,
                                @NotNull Consumer<GTRecipeBuilder> configure,
                                @NotNull Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder builder = type.recipeBuilder(id);
        configure.accept(builder);
        builder.save(provider);
    }

    public static @NotNull GTRecipe build(@NotNull GTRecipeType type, @NotNull ResourceLocation id,
                                          @NotNull Consumer<GTRecipeBuilder> configure) {
        GTRecipeBuilder builder = type.recipeBuilder(id);
        configure.accept(builder);
        return builder.buildRawRecipe();
    }

    public static @NotNull GTRecipe build(@NotNull GTRecipeType type, @NotNull String path,
                                          @NotNull Consumer<GTRecipeBuilder> configure) {
        return build(type, GTLLeisureAddon.id(path), configure);
    }
}