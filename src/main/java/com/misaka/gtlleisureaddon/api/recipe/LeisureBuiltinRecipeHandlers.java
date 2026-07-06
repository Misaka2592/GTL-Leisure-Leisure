package com.misaka.gtlleisureaddon.api.recipe;

import com.misaka.gtlleisureaddon.api.recipe.handlers.DysonSwarmModuleRecipeHandler;

final class LeisureBuiltinRecipeHandlers {

    private LeisureBuiltinRecipeHandlers() {}

    static void register() {
        LeisureRecipeAPI.modify(DysonSwarmModuleRecipeHandler::modify);
    }
}