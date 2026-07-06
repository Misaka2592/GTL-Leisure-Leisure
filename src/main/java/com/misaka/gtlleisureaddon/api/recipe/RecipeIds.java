package com.misaka.gtlleisureaddon.api.recipe;

import net.minecraft.resources.ResourceLocation;

/**
 * Helpers for GregTech recipe ids ({@code namespace:recipe_type/path}).
 */
public final class RecipeIds {

    private RecipeIds() {}

    public static ResourceLocation of(String namespace, String recipeType, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, recipeType + "/" + path);
    }

    public static ResourceLocation of(String recipeType, String path) {
        return of("gtceu", recipeType, path);
    }

    public static String recipeType(ResourceLocation id) {
        int slash = id.getPath().indexOf('/');
        return slash < 0 ? id.getPath() : id.getPath().substring(0, slash);
    }

    public static String recipePath(ResourceLocation id) {
        int slash = id.getPath().indexOf('/');
        return slash < 0 ? "" : id.getPath().substring(slash + 1);
    }

    public static boolean hasRecipeType(ResourceLocation id, String recipeType) {
        return recipeType(id).equals(recipeType);
    }
}
