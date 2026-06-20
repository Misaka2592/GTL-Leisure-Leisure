package com.misaka.gtlleisureaddon.common;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;
import com.misaka.gtlleisureaddon.common.material.LeisureMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = GTLLeisureAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LeisureItemTooltipHandler {

    private LeisureItemTooltipHandler() {}

    @SubscribeEvent
    public static void appendLeisureItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return;
        }
        if (id.getPath().equals("quantum_nucleon_stabilizer_synthesizer")) {
            return;
        }

        if (GTLLeisureAddon.MOD_ID.equals(id.getNamespace()) && (id.getPath().startsWith("example_") || id.getPath().startsWith("nucleon_aggregation_catalyst_"))) {
            LeisureTooltips.LEISURE_ADD.accept(stack, event.getToolTip());
            return;
        }

        if (LeisureMaterials.PROTON != null && LeisureMaterials.NEUTRON != null && isLeisureMaterialItem(stack)) {
            LeisureTooltips.LEISURE_ADD.accept(stack, event.getToolTip());
        }
    }

    private static boolean isLeisureMaterialItem(ItemStack stack) {
        for (TagPrefix prefix : new TagPrefix[] { TagPrefix.dustTiny, TagPrefix.dustSmall, TagPrefix.dust }) {
            if (stack.is(ChemicalHelper.getTag(prefix, LeisureMaterials.PROTON)) || stack.is(ChemicalHelper.getTag(prefix, LeisureMaterials.NEUTRON))) {
                return true;
            }
        }
        return false;
    }
}