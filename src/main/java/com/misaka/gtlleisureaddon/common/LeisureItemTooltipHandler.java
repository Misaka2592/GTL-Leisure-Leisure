package com.misaka.gtlleisureaddon.common;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;
import com.misaka.gtlleisureaddon.common.material.LeisureMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GTLLeisureAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LeisureItemTooltipHandler {

    private static final TagPrefix[] MATERIAL_PREFIXES = { TagPrefix.dustTiny, TagPrefix.dustSmall, TagPrefix.dust };

    // Lazily resolved once; tooltip events fire every frame while hovering.
    private static volatile List<TagKey<Item>> leisureMaterialTags;

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

        if (GTLLeisureAddon.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("nucleon_aggregation_catalyst_")) {
            LeisureTooltips.LEISURE_ADD.accept(stack, event.getToolTip());
            return;
        }

        if (LeisureMaterials.PROTON != null && LeisureMaterials.NEUTRON != null && isLeisureMaterialItem(stack)) {
            LeisureTooltips.LEISURE_ADD.accept(stack, event.getToolTip());
        }
    }

    private static boolean isLeisureMaterialItem(ItemStack stack) {
        for (TagKey<Item> tag : getLeisureMaterialTags()) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static List<TagKey<Item>> getLeisureMaterialTags() {
        List<TagKey<Item>> tags = leisureMaterialTags;
        if (tags == null) {
            tags = new ArrayList<>(MATERIAL_PREFIXES.length * 2);
            for (TagPrefix prefix : MATERIAL_PREFIXES) {
                addIfPresent(tags, ChemicalHelper.getTag(prefix, LeisureMaterials.PROTON));
                addIfPresent(tags, ChemicalHelper.getTag(prefix, LeisureMaterials.NEUTRON));
            }
            leisureMaterialTags = tags;
        }
        return tags;
    }

    private static void addIfPresent(List<TagKey<Item>> tags, TagKey<Item> tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }
}