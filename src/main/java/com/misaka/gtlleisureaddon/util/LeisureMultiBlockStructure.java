package com.misaka.gtlleisureaddon.util;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import com.gtladd.gtladditions.api.machine.GTLAddPartAbility;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.gregtechceu.gtceu.common.data.GTMachines.ENERGY_INPUT_HATCH;
import static com.gregtechceu.gtceu.common.data.GTMachines.FLUID_EXPORT_HATCH;
import static com.gregtechceu.gtceu.common.data.GTMachines.FLUID_IMPORT_HATCH;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_EXPORT_BUS;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;
import static com.gregtechceu.gtceu.common.data.GTMachines.LASER_INPUT_HATCH_256;

public final class LeisureMultiBlockStructure {

    public static final String QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID = "quantum_nucleon_stabilizer_synthesizer";
    public static final String QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_BIN =
            "multiblock/" + QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID + ".bin";

    private static final char STABILIZER_CASING_SYMBOL = 'F';
    private static final char STABILIZER_CONTROLLER_SYMBOL = 'G';

    private static final char PREVIEW_ENERGY = '0';
    private static final char PREVIEW_ITEM_IN = '1';
    private static final char PREVIEW_ITEM_OUT = '2';
    private static final char PREVIEW_FLUID_IN = '3';
    private static final char PREVIEW_FLUID_OUT = '4';
    private static final char PREVIEW_LASER = '5';

    private static final char[] PREVIEW_HATCH_SYMBOLS = {
            PREVIEW_ENERGY,
            PREVIEW_ITEM_IN,
            PREVIEW_ITEM_OUT,
            PREVIEW_FLUID_IN,
            PREVIEW_FLUID_OUT,
            PREVIEW_LASER
    };

    /** Live pattern only validates casing, controller, and air. Decorative sheets stay {@code any()}. */
    private static final Set<Character> STABILIZER_PATTERN_SYMBOLS =
            Set.of(' ', STABILIZER_CASING_SYMBOL, STABILIZER_CONTROLLER_SYMBOL);

    private static final RelativeDirection[] STABILIZER_DIRECTIONS = {
            RelativeDirection.BACK,
            RelativeDirection.UP,
            RelativeDirection.LEFT
    };

    /** JEI preview uses GT's conventional outward facing for {@code checkPatternAt}. */
    private static final Direction PREVIEW_OUTWARD_FACING = Direction.NORTH;
    private static final Direction PREVIEW_HATCH_FACING = Direction.SOUTH;
    private static final int PREVIEW_HATCH_TIER = GTValues.LuV;

    private LeisureMultiBlockStructure() {}

    public static BlockPattern quantumNucleonStabilizerSynthesizer(MultiblockMachineDefinition definition) {
        List<String[]> aisles = new ArrayList<>();
        for (String[] aisle : LeisureStructureResourceLoader.loadAisles(
                QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_BIN,
                QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID)) {
            aisles.add(retainSymbols(aisle, STABILIZER_PATTERN_SYMBOLS));
        }

        FactoryBlockPattern pattern = FactoryBlockPattern.start(
                STABILIZER_DIRECTIONS[0],
                STABILIZER_DIRECTIONS[1],
                STABILIZER_DIRECTIONS[2]);
        for (String[] aisle : aisles) {
            pattern.aisle(aisle);
        }

        return pattern
                .where(STABILIZER_CONTROLLER_SYMBOL, Predicates.controller(Predicates.blocks(definition.getBlock()))
                        .setPreviewCount(1))
                .where(STABILIZER_CASING_SYMBOL, Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                        .setMinGlobalLimited(1)
                        .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(Predicates.abilities(GTLAddPartAbility.INSTANCE.getTHREAD_MODIFIER())
                                .setMaxGlobalLimited(1)
                                .setPreviewCount(1)))
                .build();
    }

    /**
     * Explicit {@link MultiblockShapeInfo} with reversed aisles (see GTLCore {@code STEAM_PISTON_HAMMER}) plus preview
     * hatch injection so {@code checkPatternAt} can succeed and populate click predicates.
     */
    public static List<MultiblockShapeInfo> quantumNucleonStabilizerShapeInfos(MultiblockMachineDefinition definition) {
        String[][] aisles = LeisureStructureResourceLoader.loadAisles(
                QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_BIN,
                QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID);
        String[][] previewAisles = injectPreviewHatches(aisles);

        MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                .where(STABILIZER_CONTROLLER_SYMBOL, definition, PREVIEW_OUTWARD_FACING)
                .where(STABILIZER_CASING_SYMBOL, GTBlocks.CASING_TITANIUM_STABLE.get())
                .where(PREVIEW_ENERGY, ENERGY_INPUT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_ITEM_IN, ITEM_IMPORT_BUS[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_ITEM_OUT, ITEM_EXPORT_BUS[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_FLUID_IN, FLUID_IMPORT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_FLUID_OUT, FLUID_EXPORT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_LASER, LASER_INPUT_HATCH_256[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where('A', LeisureStructureBlocks.BLACK_METAL_SHEET)
                .where('B', LeisureStructureBlocks.BROWN_METAL_SHEET)
                .where('C', LeisureStructureBlocks.GRAY_METAL_SHEET)
                .where('D', LeisureStructureBlocks.GREEN_METAL_SHEET)
                .where('E', LeisureStructureBlocks.LIGHT_GRAY_METAL_SHEET)
                .where(' ', Blocks.AIR.defaultBlockState());

        for (int aisleIndex = previewAisles.length - 1; aisleIndex >= 0; aisleIndex--) {
            builder = builder.aisle(previewAisles[aisleIndex]);
        }
        return List.of(builder.build());
    }

    private static String[][] injectPreviewHatches(String[][] aisles) {
        String[][] copy = new String[aisles.length][];
        for (int aisleIndex = 0; aisleIndex < aisles.length; aisleIndex++) {
            copy[aisleIndex] = new String[aisles[aisleIndex].length];
            for (int rowIndex = 0; rowIndex < aisles[aisleIndex].length; rowIndex++) {
                copy[aisleIndex][rowIndex] = aisles[aisleIndex][rowIndex];
            }
        }

        int hatchIndex = 0;
        outer:
        for (int aisleIndex = 0; aisleIndex < copy.length; aisleIndex++) {
            for (int rowIndex = 0; rowIndex < copy[aisleIndex].length; rowIndex++) {
                char[] row = copy[aisleIndex][rowIndex].toCharArray();
                boolean changed = false;
                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    if (row[columnIndex] != STABILIZER_CASING_SYMBOL || hatchIndex >= PREVIEW_HATCH_SYMBOLS.length) {
                        continue;
                    }
                    row[columnIndex] = PREVIEW_HATCH_SYMBOLS[hatchIndex++];
                    changed = true;
                    if (hatchIndex >= PREVIEW_HATCH_SYMBOLS.length) {
                        if (changed) {
                            copy[aisleIndex][rowIndex] = new String(row);
                        }
                        break outer;
                    }
                }
                if (changed) {
                    copy[aisleIndex][rowIndex] = new String(row);
                }
            }
        }
        return copy;
    }

    private static String[] retainSymbols(String[] aisle, Set<Character> allowed) {
        String[] sanitized = new String[aisle.length];
        for (int rowIndex = 0; rowIndex < aisle.length; rowIndex++) {
            char[] row = aisle[rowIndex].toCharArray();
            for (int colIndex = 0; colIndex < row.length; colIndex++) {
                if (!allowed.contains(row[colIndex])) {
                    row[colIndex] = ' ';
                }
            }
            sanitized[rowIndex] = new String(row);
        }
        return sanitized;
    }
}
