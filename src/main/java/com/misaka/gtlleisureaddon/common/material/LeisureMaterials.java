package com.misaka.gtlleisureaddon.common.material;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

public final class LeisureMaterials {

    public static Material PROTON;
    public static Material NEUTRON;

    private LeisureMaterials() {}

    public static void init() {
        if (PROTON != null) {
            return;
        }

        PROTON = new Material.Builder(GTLLeisureAddon.id("proton"))
                .dust()
                .gas()
                .iconSet(MaterialIconSet.DULL)
                .color(0xFF4444)
                .buildAndRegister();

        NEUTRON = new Material.Builder(GTLLeisureAddon.id("neutron"))
                .dust()
                .gas()
                .iconSet(MaterialIconSet.DULL)
                .color(0x4488FF)
                .buildAndRegister();
    }
}