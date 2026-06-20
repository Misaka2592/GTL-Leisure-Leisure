package com.misaka.gtlleisureaddon.util;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Block states referenced by structure preview symbols. */
public final class LeisureStructureBlocks {

    public static final BlockState BLACK_METAL_SHEET = block("gtceu:black_metal_sheet");
    public static final BlockState BROWN_METAL_SHEET = block("gtceu:brown_metal_sheet");
    public static final BlockState GRAY_METAL_SHEET = block("gtceu:gray_metal_sheet");
    public static final BlockState GREEN_METAL_SHEET = block("gtceu:green_metal_sheet");
    public static final BlockState LIGHT_GRAY_METAL_SHEET = block("gtceu:light_gray_metal_sheet");

    public static boolean isDecorativeSheet(BlockState state) {
        return state.getBlock() == BLACK_METAL_SHEET.getBlock()
                || state.getBlock() == BROWN_METAL_SHEET.getBlock()
                || state.getBlock() == GRAY_METAL_SHEET.getBlock()
                || state.getBlock() == GREEN_METAL_SHEET.getBlock()
                || state.getBlock() == LIGHT_GRAY_METAL_SHEET.getBlock();
    }

    private LeisureStructureBlocks() {}

    private static BlockState block(String id) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), id, false).blockState();
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid structure block id '" + id + "'", exception);
        }
    }
}
