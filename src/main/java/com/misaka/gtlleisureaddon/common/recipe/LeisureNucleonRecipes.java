package com.misaka.gtlleisureaddon.common.recipe;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;
import com.misaka.gtlleisureaddon.common.items.LeisureItems;
import com.misaka.gtlleisureaddon.common.material.LeisureMaterials;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import java.util.Set;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;

public final class LeisureNucleonRecipes {

    private static final int COUPLING_TIER_IX = 8;
    private static final int COUPLING_TIER_PROTOTYPE = 9;
    private static final int COUPLING_TIER_COUNT = 10;

    private static final Set<String> PROTOTYPE_CATALYST_MATERIALS = Set.of(
            "eternity",
            "rhugnor",
            "infinity",
            "hypogen",
            "chaos",
            "draconiumawakened",
            // "mtter" is GTLCore's actual registered material id (upstream typo), do not "fix" it
            "white_dwarf_mtter",
            "raw_star_matter",
            "magnetohydrodynamicallyconstrainedstarmatter",
            "crystalmatrix",
            "cosmic_mesh",
            "shirabon",
            "creon");

    private LeisureNucleonRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        LeisureMaterials.init();
        if (LeisureMaterials.PROTON == null || LeisureMaterials.NEUTRON == null) {
            return;
        }

        int[] couplingTierCircuitCounters = new int[COUPLING_TIER_COUNT];

        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (!material.isElement()) {
                continue;
            }
            if (!hasProcessableForm(material)) {
                continue;
            }

            long protons = material.getProtons();
            long neutrons = material.getNeutrons();
            if (protons <= 0 && neutrons <= 0) {
                continue;
            }

            registerDecouplingRecipe(material, protons, neutrons, provider);
            registerCouplingRecipe(material, protons, neutrons, couplingTierCircuitCounters, provider);
        }
    }

    private static boolean hasProcessableForm(Material material) {
        return material.hasProperty(PropertyKey.DUST) || material.hasFluid();
    }

    private static void registerDecouplingRecipe(Material material, long protons, long neutrons,
                                                 Consumer<FinishedRecipe> provider) {
        var builder = LeisureRecipeTypes.NUCLEON_DECOUPLING_PROTOCOL
                .recipeBuilder(GTLLeisureAddon.id("decouple_" + material.getName()));

        appendElementInput(builder, material);
        appendNucleonOutputs(builder, protons, neutrons);

        builder.duration(getDuration(protons, neutrons))
                .EUt(GTValues.VA[getVoltageTier(material)])
                .save(provider);
    }

    private static void registerCouplingRecipe(Material material, long protons, long neutrons,
                                               int[] couplingTierCircuitCounters,
                                               Consumer<FinishedRecipe> provider) {
        int couplingTierIndex = getCouplingTierIndex(material);
        boolean usePrototype = usesPrototypeCatalyst(material, couplingTierIndex);
        int circuitTierIndex = usePrototype ? COUPLING_TIER_PROTOTYPE : couplingTierIndex;
        int circuit = ++couplingTierCircuitCounters[circuitTierIndex];

        var builder = LeisureRecipeTypes.NUCLEON_COUPLING_PROTOCOL
                .recipeBuilder(GTLLeisureAddon.id("couple_" + material.getName()));

        appendNucleonInputs(builder, protons, neutrons);
        appendElementOutput(builder, material);
        builder.notConsumable(getCouplingCatalyst(usePrototype, couplingTierIndex))
                .circuitMeta(circuit);

        builder.duration(getDuration(protons, neutrons))
                .EUt(GTValues.VA[getVoltageTier(material)])
                .save(provider);
    }

    private static void appendElementInput(com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder builder, Material material) {
        if (material.hasProperty(PropertyKey.DUST)) {
            builder.inputItems(dust, material, 1);
        } else {
            builder.inputFluids(material.getFluid(1000));
        }
    }

    private static void appendElementOutput(com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder builder, Material material) {
        if (material.hasProperty(PropertyKey.DUST)) {
            builder.outputItems(dust, material, 1);
        } else {
            builder.outputFluids(material.getFluid(1000));
        }
    }

    private static void appendNucleonOutputs(com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder builder, long protons, long neutrons) {
        if (protons > 0) {
            builder.outputItems(dust, LeisureMaterials.PROTON, Math.toIntExact(protons));
        }
        if (neutrons > 0) {
            builder.outputItems(dust, LeisureMaterials.NEUTRON, Math.toIntExact(neutrons));
        }
    }

    private static void appendNucleonInputs(com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder builder, long protons, long neutrons) {
        if (protons > 0) {
            builder.inputItems(dust, LeisureMaterials.PROTON, Math.toIntExact(protons));
        }
        if (neutrons > 0) {
            builder.inputItems(dust, LeisureMaterials.NEUTRON, Math.toIntExact(neutrons));
        }
    }

    private static int getDuration(long protons, long neutrons) {
        return (int) Math.max(100, (protons + neutrons) * 20);
    }

    private static int getVoltageTier(Material material) {
        long atomicNumber = Math.max(1, material.getProtons());

        if (atomicNumber <= 20) {
            return GTValues.LuV;
        }
        if (atomicNumber <= 40) {
            return GTValues.ZPM;
        }
        if (atomicNumber <= 56) {
            return GTValues.UV;
        }
        if (atomicNumber <= 74) {
            return GTValues.UHV;
        }
        if (atomicNumber <= 92) {
            return GTValues.UEV;
        }
        if (atomicNumber <= 100) {
            return GTValues.UIV;
        }
        if (atomicNumber <= 118) {
            return GTValues.OpV;
        }
        return GTValues.MAX;
    }

    /**
     * Maps voltage tier to MK I–IX catalyst index. OpV tier (Z &le; 118) is split at Z=108 so all nine catalysts are
     * used
     * while {@link #getVoltageTier(Material)} stays unchanged.
     */
    private static int getCouplingTierIndex(Material material) {
        long atomicNumber = Math.max(1, material.getProtons());

        if (atomicNumber <= 20) {
            return 0;
        }
        if (atomicNumber <= 40) {
            return 1;
        }
        if (atomicNumber <= 56) {
            return 2;
        }
        if (atomicNumber <= 74) {
            return 3;
        }
        if (atomicNumber <= 92) {
            return 4;
        }
        if (atomicNumber <= 100) {
            return 5;
        }
        if (atomicNumber <= 108) {
            return 6;
        }
        if (atomicNumber <= 118) {
            return 7;
        }
        return 8;
    }

    private static boolean usesPrototypeCatalyst(Material material, int couplingTierIndex) {
        return couplingTierIndex == COUPLING_TIER_IX && PROTOTYPE_CATALYST_MATERIALS.contains(material.getName());
    }

    private static Item getCouplingCatalyst(boolean usePrototype, int couplingTierIndex) {
        if (usePrototype) {
            return LeisureItems.NUCLEON_AGGREGATION_CATALYST_PROTOTYPE.get();
        }
        return LeisureItems.NUCLEON_AGGREGATION_CATALYSTS[couplingTierIndex].get();
    }
}