package com.misaka.gtlleisureaddon.util;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.gtladd.gtladditions.api.machine.GTLAddPartAbility;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static org.gtlcore.gtlcore.utils.Registries.getBlock;

/**
 * 归墟·定元仪 / 浑天仪 ({@code quantum_nucleon_stabilizer_synthesizer}) multiblock layout.
 * Pattern and symbol bindings are hardcoded below for manual editing.
 */
public final class LeisureMultiBlockStructure {

    public static final String QUANTUM_NUCLEON_STABILIZER_SYNTHESIZER_ID = "quantum_nucleon_stabilizer_synthesizer";

    private static final char CONTROLLER = 'G';

    private static final char LASER_ONLY = 'K';
    private static final char REPLACEABLE_CASING = 'U';
    private static final char IRREPLACEABLE_CASING = 'J';

    private static final RelativeDirection[] DIRECTIONS = {
            RelativeDirection.BACK,
            RelativeDirection.UP,
            RelativeDirection.LEFT
    };

    private static final Map<Character, BlockState> QUANTUM_SYMBOLS = Map.ofEntries(
            Map.entry('A', block("gtceu:fusion_glass")),
            Map.entry('B', block("gtlcore:molecular_casing")),
            Map.entry('C', block("kubejs:spacetime_assembly_line_casing")),
            Map.entry('D', block("gtlcore:rhenium_reinforced_energy_glass")),
            Map.entry('E', block("kubejs:accelerated_pipeline")),
            Map.entry('F', block("kubejs:speeding_pipe")),
            Map.entry('G', block("gtceu:fusion_glass")),
            Map.entry('H', block("gtceu:ptfe_pipe_casing")),
            Map.entry('I', block("gtceu:incoloy_ma_956_frame")),
            Map.entry('J', block("gtceu:atomic_casing")),
            Map.entry('K', block("gtceu:zpm_16384a_laser_target_hatch")),
            Map.entry('L', block("gtlcore:enhance_hyper_mechanical_casing")),
            Map.entry('M', block("gtceu:maraging_steel_300_frame")),
            Map.entry('N', block("gtceu:fusion_casing_mk3")),
            Map.entry('O', block("kubejs:laser_cooling_casing")),
            Map.entry('P', block("gtlcore:multi_functional_casing")),
            Map.entry('Q', block("gtlcore:component_assembly_line_casing_uv")),
            Map.entry('R', block("gtceu:computer_heat_vent")),
            Map.entry('S', block("gtceu:heat_vent")),
            Map.entry('T', block("kubejs:molecular_coil")),
            Map.entry('U', block("gtceu:atomic_casing")),
            Map.entry('W', block("kubejs:magic_core")));

    private static final String[][] QUANTUM_AISLES = {
            // aisle 0
            {
                    "   BBB   ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "   BBB   ",
                    "         ",
                    "         ",
                    "         "
            },
            // aisle 1
            {
                    "  BBBBB  ",
                    "   CD    ",
                    "    D    ",
                    "    DC   ",
                    "    C    ",
                    "   CD    ",
                    "    D    ",
                    "    DC   ",
                    "    C    ",
                    "   CD    ",
                    "  BBBBB  ",
                    "    D    ",
                    "    D    ",
                    "         "
            },
            // aisle 2
            {
                    "  BEEEB  ",
                    "   DFD   ",
                    "   CFC   ",
                    "   DFD   ",
                    "   DFD   ",
                    "   DFD   ",
                    "   CFC   ",
                    "   DFD   ",
                    "   DFD   ",
                    "   DFD   ",
                    "  BBEBB  ",
                    "   DHD   ",
                    "   DHD   ",
                    "    D    "
            },
            // aisle 3
            {
                    "  BBBBB  ",
                    "    DC   ",
                    "    D    ",
                    "   CD    ",
                    "    C    ",
                    "    DC   ",
                    "    D    ",
                    "   CD    ",
                    "    C    ",
                    "    DC   ",
                    "  BBBBB  ",
                    "   D D   ",
                    "   DHD   ",
                    "    D    "
            },
            // aisle 4
            {
                    "   BBB   ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "   BBB   ",
                    "    H    ",
                    "    D    ",
                    "         "
            },
            // aisle 5
            {
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 6
            {
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 7
            {
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 8
            {
                    "II  J  II",
                    "    J    ",
                    "    J    ",
                    "    K    ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 9
            {
                    "I ILJLI I",
                    "U  JJJ   ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 10
            {
                    "UILLJLLI ",
                    "U J A J  ",
                    "   AAA   ",
                    "   AAA   ",
                    "    A    ",
                    "         ",
                    "         ",
                    "    M    ",
                    "         ",
                    "         ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 11
            {
                    "ULLNNNLL ",
                    "UJ AOA J ",
                    "U A P A  ",
                    "  AA AA  ",
                    "   AQA   ",
                    "    R    ",
                    "    S    ",
                    "   MRM   ",
                    "    A    ",
                    "    J    ",
                    "         ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 12
            {
                    "UJJNNNJJJ",
                    "GJAOTOAJJ",
                    "U APTPA J",
                    "K A T A K",
                    "  AQOQA  ",
                    "   RTR   ",
                    "   STS   ",
                    "  MRTRM  ",
                    "   AWA   ",
                    "   JHJ   ",
                    "    H    ",
                    "    H    ",
                    "         ",
                    "         "
            },
            // aisle 13
            {
                    "ULLNNNLL ",
                    "UJ AOA J ",
                    "U A P A  ",
                    "  AA AA  ",
                    "   AQA   ",
                    "    R    ",
                    "    S    ",
                    "   MRM   ",
                    "    A    ",
                    "    J    ",
                    "         ",
                    "         ",
                    "         ",
                    "         "
            },
            // aisle 14
            {
                    "UILLJLLI ",
                    "U J A J  ",
                    "   AAA   ",
                    "   AAA   ",
                    "    A    ",
                    "         ",
                    "         ",
                    "    M    ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         "
            },
            // aisle 15
            {
                    "I ILJLI I",
                    "U  JJJ   ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         "
            },
            // aisle 16
            {
                    "II  J  II",
                    "    J    ",
                    "    J    ",
                    "    K    ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         ",
                    "         "
            }
    };

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
            BlockState state = entry.getValue();
            if (symbol == CONTROLLER) {
                pattern.where(CONTROLLER, controller(blocks(definition.getBlock())).setPreviewCount(1));
                continue;
            }
            if (symbol == LASER_ONLY) {
                pattern.where(
                        LASER_ONLY,
                        blocks(getBlock("gtceu:atomic_casing"))
                                .or(abilities(PartAbility.INPUT_LASER)
                                        .setMaxGlobalLimited(4)));
                continue;
            }
            if (symbol == REPLACEABLE_CASING) {
                pattern.where(
                        REPLACEABLE_CASING,
                        blocks(state.getBlock())
                                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(GTLAddPartAbility.INSTANCE.getTHREAD_MODIFIER())
                                        .setMaxGlobalLimited(1)));
                continue;
            }
            pattern.where(symbol, blocks(state.getBlock()));
        }
        return pattern.build();
    }

    private static BlockState block(String id) {
        Block resolved = getBlock(id);
        if (resolved == null || resolved == Blocks.AIR) {
            throw new IllegalStateException("Invalid structure block id '" + id + "'");
        }
        return resolved.defaultBlockState();
    }
}