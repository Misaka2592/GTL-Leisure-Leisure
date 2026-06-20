package com.misaka.gtlleisureaddon;

import com.misaka.gtlleisureaddon.common.items.LeisureItems;
import com.misaka.gtlleisureaddon.common.recipe.LeisureNucleonRecipes;
import com.misaka.gtlleisureaddon.registry.LeisureRegistration;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

@GTAddon
public class LeisureGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return LeisureRegistration.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        LeisureItems.init();
    }

    @Override
    public String addonModId() {
        return GTLLeisureAddon.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        LeisureNucleonRecipes.init(provider);
    }
}