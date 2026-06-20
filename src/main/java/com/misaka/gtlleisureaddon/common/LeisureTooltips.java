package com.misaka.gtlleisureaddon.common;

import org.gtlcore.gtlcore.utils.TextUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

public final class LeisureTooltips {

    public static final BiConsumer<ItemStack, List<Component>> LEISURE_ADD = (stack, components) -> components.add(fullColor("tooltip.lleisure.added_by"));

    private LeisureTooltips() {}

    public static Component fullColor(String translationKey) {
        // Resolve at tooltip render time; calling getString() during mod init returns the raw key.
        return Component.literal(TextUtil.full_color(Component.translatable(translationKey).getString()));
    }
}