package com.misaka.gtlleisureaddon.api.recipe.handler;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

/**
 * Marks recipes that should not be added to {@code GTRecipeLookup} (hidden from machines and JEI).
 */
@FunctionalInterface
public interface RecipeRemoval {

    boolean shouldRemove(ResourceLocation recipeId);

    static RecipeRemoval anyOf(RecipeRemoval... removals) {
        return id -> {
            for (RecipeRemoval removal : removals) {
                if (removal.shouldRemove(id)) {
                    return true;
                }
            }
            return false;
        };
    }

    static RecipeRemoval ids(ResourceLocation... ids) {
        return id -> {
            for (ResourceLocation candidate : ids) {
                if (candidate.equals(id)) {
                    return true;
                }
            }
            return false;
        };
    }

    static RecipeRemoval when(Predicate<ResourceLocation> matcher) {
        return matcher::test;
    }

    static RecipeRemoval namespace(String namespace) {
        return id -> namespace.equals(id.getNamespace());
    }
}
