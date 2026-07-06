package com.misaka.gtlleisureaddon.api.recipe;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Collects datagen recipe registrars and applies them during {@code IGTAddon#addRecipes}.
 */
public final class LeisureRecipeRegistration {

    private static final List<Consumer<Consumer<FinishedRecipe>>> REGISTRARS = new ArrayList<>();

    private LeisureRecipeRegistration() {}

    public static void register(Consumer<Consumer<FinishedRecipe>> registrar) {
        REGISTRARS.add(registrar);
    }

    public static void apply(Consumer<FinishedRecipe> provider) {
        for (Consumer<Consumer<FinishedRecipe>> registrar : REGISTRARS) {
            registrar.accept(provider);
        }
    }

    static void clear() {
        REGISTRARS.clear();
    }
}