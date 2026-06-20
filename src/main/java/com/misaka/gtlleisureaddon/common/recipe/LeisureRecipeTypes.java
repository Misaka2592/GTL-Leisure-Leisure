package com.misaka.gtlleisureaddon.common.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

public final class LeisureRecipeTypes {

    public static GTRecipeType NUCLEON_DECOUPLING_PROTOCOL;
    public static GTRecipeType NUCLEON_COUPLING_PROTOCOL;

    private static boolean initialized;

    private LeisureRecipeTypes() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        NUCLEON_DECOUPLING_PROTOCOL = GTRecipeTypes.register("nucleon_decoupling_protocol", GTRecipeTypes.MULTIBLOCK)
                .setEUIO(IO.IN)
                .setMaxIOSize(2, 6, 1, 3)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);

        NUCLEON_COUPLING_PROTOCOL = GTRecipeTypes.register("nucleon_coupling_protocol", GTRecipeTypes.MULTIBLOCK)
                .setEUIO(IO.IN)
                .setMaxIOSize(6, 2, 3, 1)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);
    }
}