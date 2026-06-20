package com.misaka.gtlleisureaddon;

import com.misaka.gtlleisureaddon.common.LeisureCreativeTabs;
import com.misaka.gtlleisureaddon.common.machines.LeisureMachines;
import com.misaka.gtlleisureaddon.common.material.LeisureMaterials;
import com.misaka.gtlleisureaddon.common.recipe.LeisureRecipeTypes;
import com.misaka.gtlleisureaddon.registry.LeisureRegistration;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(GTLLeisureAddon.MOD_ID)
public class GTLLeisureAddon {

    public static final String MOD_ID = "lleisure";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public GTLLeisureAddon() {
        LOGGER.info("Initializing {}", MOD_ID);

        LeisureCreativeTabs.init();

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LeisureRegistration.REGISTRATE.registerRegistrate();
        LeisureRegistration.REGISTRATE.registerEventListeners(modEventBus);

        modEventBus.addListener((MaterialRegistryEvent event) -> GTCEuAPI.materialManager.createRegistry(MOD_ID));

        modEventBus.addListener((MaterialEvent event) -> LeisureMaterials.init());

        modEventBus.addListener((PostMaterialEvent event) -> LeisureRegistration.REGISTRATE.creativeModeTab(() -> LeisureCreativeTabs.MAIN));

        modEventBus.addGenericListener(GTRecipeType.class, (GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) -> LeisureRecipeTypes.init());

        modEventBus.addGenericListener(MachineDefinition.class, (GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) -> LeisureMachines.init());
    }
}