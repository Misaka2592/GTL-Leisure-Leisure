package com.misaka.gtlleisureaddon.common;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;
import com.misaka.gtlleisureaddon.common.material.LeisureMaterials;
import com.misaka.gtlleisureaddon.registry.LeisureRegistration;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

public final class LeisureCreativeTabs {

    public static RegistryEntry<CreativeModeTab> MAIN;

    private static boolean initialized;

    private LeisureCreativeTabs() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        MAIN = LeisureRegistration.REGISTRATE
                .defaultCreativeTab("main", builder -> builder
                        .displayItems(new MainDisplayItemsGenerator())
                        .icon(() -> GTBlocks.CASING_TITANIUM_STABLE.asStack())
                        .title(LeisureRegistration.REGISTRATE.addLang("itemGroup", GTLLeisureAddon.id("main"), "Leisure Leisure"))
                        .build())
                .register();

        LeisureRegistration.REGISTRATE.creativeModeTab(() -> MAIN);
    }

    private static final class MainDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        private final GTCreativeModeTabs.RegistrateDisplayItemsGenerator registrateItems = new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", LeisureRegistration.REGISTRATE);

        @Override
        public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                           @NotNull CreativeModeTab.Output output) {
            registrateItems.accept(itemDisplayParameters, output);
            addMaterialItems(output);
        }

        private static void addMaterialItems(CreativeModeTab.Output output) {
            if (LeisureMaterials.PROTON == null || LeisureMaterials.NEUTRON == null) {
                return;
            }

            for (TagPrefix prefix : new TagPrefix[] { TagPrefix.dustTiny, TagPrefix.dustSmall, TagPrefix.dust }) {
                acceptIfPresent(output, ChemicalHelper.get(prefix, LeisureMaterials.PROTON));
                acceptIfPresent(output, ChemicalHelper.get(prefix, LeisureMaterials.NEUTRON));
            }
        }

        private static void acceptIfPresent(CreativeModeTab.Output output, ItemStack stack) {
            if (!stack.isEmpty()) {
                output.accept(stack);
            }
        }
    }
}