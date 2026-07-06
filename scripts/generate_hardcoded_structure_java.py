#!/usr/bin/env python3
"""Generate LeisureMultiBlockStructure.java with hardcoded pattern data."""
import json
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/java/com/misaka/gtlleisureaddon/util/LeisureMultiBlockStructure.java"


def decode_bin(path: Path):
    data = path.read_bytes()
    offset = 8
    id_len = struct.unpack_from(">H", data, offset)[0]
    offset += 2 + id_len
    aisle_count, row_count, width = struct.unpack_from(">HHH", data, offset)
    offset += 6
    dict_size = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    dictionary = [
        data[offset + i * width : offset + (i + 1) * width].decode("ascii") for i in range(dict_size)
    ]
    offset += dict_size * width
    aisles = []
    for _ in range(aisle_count):
        aisle = []
        for _ in range(row_count):
            idx = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            aisle.append(dictionary[idx])
        aisles.append(aisle)
    return aisles


def emit_hardcoded(structure_id: str) -> str:
    bin_path = ROOT / f"src/main/resources/assets/lleisure/structures/multiblock/{structure_id}.bin"
    symbols_path = ROOT / f"src/main/resources/assets/lleisure/structures/{structure_id}.symbols.json"
    symbols = json.loads(symbols_path.read_text(encoding="utf-8"))
    aisles = decode_bin(bin_path)

    lines = ["    private static final Map<Character, BlockState> QUANTUM_SYMBOLS = Map.ofEntries("]
    entries = [(value, key) for key, value in symbols.items() if value != " "]
    for index, (symbol, state_key) in enumerate(sorted(entries, key=lambda item: item[0])):
        java_state = state_key.replace("#controller:", "")
        comma = "," if index < len(entries) - 1 else ""
        lines.append(f'            Map.entry(\'{symbol}\', block("{java_state}")){comma}')
    lines.append("    );")
    lines.append("")
    lines.append("    private static final String[][] QUANTUM_AISLES = {")
    for zi, aisle in enumerate(aisles):
        lines.append(f"        // aisle {zi}")
        lines.append("        {")
        for row_index, row in enumerate(aisle):
            comma = "," if row_index < len(aisle) - 1 else ""
            lines.append(f'            "{row}"{comma}')
        comma = "," if zi < len(aisles) - 1 else ""
        lines.append(f"        }}{comma}")
    lines.append("    };")
    lines.append("")
    return "\n".join(lines)


HEADER = '''package com.misaka.gtlleisureaddon.util;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.gtladd.gtladditions.api.machine.GTLAddPartAbility;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 归墟·定元仪 / 浑天仪 ({@code quantum_nucleon_stabilizer_synthesizer}) multiblock layout.
 * Pattern and symbol bindings are hardcoded below for manual editing.
 */
public final class LeisureMultiBlockStructure {

    public static final String QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID = "quantum_nucleon_stabilizer_synthesizer";

    private static final char CONTROLLER = 'G';

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

    private static final RelativeDirection[] DIRECTIONS = {
            RelativeDirection.BACK,
            RelativeDirection.UP,
            RelativeDirection.LEFT
    };

    private static final Direction PREVIEW_OUTWARD_FACING = Direction.NORTH;
    private static final Direction PREVIEW_HATCH_FACING = Direction.SOUTH;
    private static final int PREVIEW_HATCH_TIER = GTValues.LuV;

    private static final Set<ResourceLocation> HATCHABLE_CASINGS = Set.of(
            ResourceLocation.parse("gtlcore:molecular_casing"),
            ResourceLocation.parse("kubejs:spacetime_assembly_line_casing"),
            ResourceLocation.parse("gtceu:atomic_casing"),
            ResourceLocation.parse("gtlcore:enhance_hyper_mechanical_casing"),
            ResourceLocation.parse("gtlcore:multi_functional_casing"),
            ResourceLocation.parse("gtlcore:component_assembly_line_casing_uv"));

'''

FOOTER = '''
    private LeisureMultiBlockStructure() {}

    public static BlockPattern quantumNucleonStabilizerSynthesizer(MultiblockMachineDefinition definition) {
        FactoryBlockPattern pattern = FactoryBlockPattern.start(
                DIRECTIONS[0],
                DIRECTIONS[1],
                DIRECTIONS[2]);
        for (String[] aisle : QUANTUM_AISLES) {
            pattern.aisle(aisle);
        }

        for (Map.Entry<Character, BlockState> entry : QUANTUM_SYMBOLS.entrySet()) {
            char symbol = entry.getKey();
            if (symbol == CONTROLLER) {
                pattern.where(CONTROLLER, Predicates.controller(Predicates.blocks(definition.getBlock()))
                        .setPreviewCount(1));
                continue;
            }
            pattern.where(symbol, predicateForStructureBlock(entry.getValue(), definition));
        }
        return pattern.build();
    }

    public static List<MultiblockShapeInfo> quantumNucleonStabilizerShapeInfos(MultiblockMachineDefinition definition) {
        String[][] previewAisles = injectPreviewHatches(QUANTUM_AISLES);

        MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                .where(CONTROLLER, definition, PREVIEW_OUTWARD_FACING)
                .where(PREVIEW_ENERGY, com.gregtechceu.gtceu.common.data.GTMachines.ENERGY_INPUT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_ITEM_IN, com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_ITEM_OUT, com.gregtechceu.gtceu.common.data.GTMachines.ITEM_EXPORT_BUS[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_FLUID_IN, com.gregtechceu.gtceu.common.data.GTMachines.FLUID_IMPORT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_FLUID_OUT, com.gregtechceu.gtceu.common.data.GTMachines.FLUID_EXPORT_HATCH[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING)
                .where(PREVIEW_LASER, com.gregtechceu.gtceu.common.data.GTMachines.LASER_INPUT_HATCH_256[PREVIEW_HATCH_TIER], PREVIEW_HATCH_FACING);

        for (Map.Entry<Character, BlockState> entry : QUANTUM_SYMBOLS.entrySet()) {
            char symbol = entry.getKey();
            if (symbol == CONTROLLER) {
                continue;
            }
            builder = builder.where(symbol, entry.getValue().getBlock());
        }

        for (int aisleIndex = previewAisles.length - 1; aisleIndex >= 0; aisleIndex--) {
            builder = builder.aisle(previewAisles[aisleIndex]);
        }
        return List.of(builder.build());
    }

    private static BlockState block(String id) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), id, false).blockState();
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid structure block id '" + id + "'", exception);
        }
    }

    private static ResourceLocation blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    private static TraceabilityPredicate predicateForStructureBlock(
            BlockState state,
            MultiblockMachineDefinition definition) {
        ResourceLocation id = blockId(state);
        if (id.getPath().endsWith("_laser_target_hatch")) {
            return Predicates.states(state)
                    .or(Predicates.abilities(PartAbility.OUTPUT_LASER)
                            .setMaxGlobalLimited(4)
                            .setPreviewCount(1));
        }
        if (HATCHABLE_CASINGS.contains(id)) {
            return Predicates.states(state)
                    .or(hatchablePredicate(definition));
        }
        return Predicates.states(state);
    }

    private static TraceabilityPredicate hatchablePredicate(MultiblockMachineDefinition definition) {
        return Predicates.autoAbilities(definition.getRecipeTypes())
                .or(Predicates.autoAbilities(true, false, true))
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(Predicates.abilities(GTLAddPartAbility.INSTANCE.getTHREAD_MODIFIER())
                        .setMaxGlobalLimited(1)
                        .setPreviewCount(1));
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
                    BlockState state = QUANTUM_SYMBOLS.get(row[columnIndex]);
                    if (state == null
                            || !HATCHABLE_CASINGS.contains(blockId(state))
                            || hatchIndex >= PREVIEW_HATCH_SYMBOLS.length) {
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
}
'''


def main() -> int:
    hardcoded = emit_hardcoded("quantum_nucleon_stabilizer_synthesizer")
    OUT.write_text(HEADER + hardcoded + FOOTER, encoding="utf-8", newline="\n")
    print(f"wrote {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
