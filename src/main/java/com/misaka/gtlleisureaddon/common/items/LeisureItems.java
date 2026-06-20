package com.misaka.gtlleisureaddon.common.items;

import com.misaka.gtlleisureaddon.registry.LeisureRegistration;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

public final class LeisureItems {

    public static ItemEntry<Item> EXAMPLE_COMPONENT;
    public static ItemEntry<Item> EXAMPLE_CATALYST;

    public static final int NUCLEON_AGGREGATION_CATALYST_COUNT = 9;
    public static final ItemEntry<Item>[] NUCLEON_AGGREGATION_CATALYSTS = new ItemEntry[NUCLEON_AGGREGATION_CATALYST_COUNT];
    public static ItemEntry<Item> NUCLEON_AGGREGATION_CATALYST_PROTOTYPE;

    private static final String[] CATALYST_MK_ROMAN = {
            "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"
    };

    private static boolean initialized;

    private LeisureItems() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        EXAMPLE_COMPONENT = LeisureRegistration.REGISTRATE
                .item("example_component", Item::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), GTCEu.id("item/diamond_dust")))
                .lang("Example Component")
                .register();

        EXAMPLE_CATALYST = LeisureRegistration.REGISTRATE
                .item("example_catalyst", Item::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), GTCEu.id("item/gold_dust")))
                .lang("Example Catalyst")
                .register();

        for (int i = 0; i < NUCLEON_AGGREGATION_CATALYST_COUNT; i++) {
            String id = "nucleon_aggregation_catalyst_mk_" + CATALYST_MK_ROMAN[i];
            int mkNumber = i + 1;
            NUCLEON_AGGREGATION_CATALYSTS[i] = LeisureRegistration.REGISTRATE
                    .item(id, Item::new)
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                            .texture("layer0", prov.modLoc("item/" + ctx.getName())))
                    .lang("Nucleon Aggregation Catalyst MK " + toRomanDisplay(mkNumber))
                    .register();
        }

        NUCLEON_AGGREGATION_CATALYST_PROTOTYPE = LeisureRegistration.REGISTRATE
                .item("nucleon_aggregation_catalyst_prototype", Item::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                        .texture("layer0", prov.modLoc("item/nucleon_aggregation_catalyst_prototype")))
                .lang("Nucleon Aggregation Catalyst Prototype")
                .register();
    }

    private static String toRomanDisplay(int mkNumber) {
        return switch (mkNumber) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> String.valueOf(mkNumber);
        };
    }
}