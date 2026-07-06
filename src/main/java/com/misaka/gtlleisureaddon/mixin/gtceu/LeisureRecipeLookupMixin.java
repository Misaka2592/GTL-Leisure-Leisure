package com.misaka.gtlleisureaddon.mixin.gtceu;

import com.misaka.gtlleisureaddon.api.recipe.LeisureRecipeAPI;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GTRecipeLookup.class, priority = 1100)
public class LeisureRecipeLookupMixin {

    @ModifyVariable(
                    method = "addRecipe",
                    at = @At("HEAD"),
                    argsOnly = true,
                    ordinal = 0,
                    remap = false)
    private GTRecipe lleisure$processRecipe(GTRecipe recipe) {
        return LeisureRecipeAPI.processRecipe(recipe);
    }
}