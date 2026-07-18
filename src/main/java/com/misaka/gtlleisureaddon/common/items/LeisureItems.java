package com.misaka.gtlleisureaddon.common.items;

import com.misaka.gtlleisureaddon.registry.LeisureRegistration;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

public final class LeisureItems {

    public static final int NUCLEON_AGGREGATION_CATALYST_COUNT = 9;
    @SuppressWarnings("unchecked")
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