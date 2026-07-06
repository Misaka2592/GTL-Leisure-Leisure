package com.misaka.gtlleisureaddon.dyson;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Long-safe module stack counting for modded inventories (e.g. infinity cells) where
 * {@link ItemStack#getCount()} is capped at {@code int} but the real amount is stored in NBT.
 */
public final class ModuleItemCounts {

    public static final long MAX = Long.MAX_VALUE;

    private static final String[] LONG_COUNT_KEYS = {
            "longCount", "LongCount", "stackSize", "StackSize", "itemCount", "ItemCount", "Count"
    };

    private ModuleItemCounts() {}

    public static long readCount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        long count = stack.getCount();
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            for (String key : LONG_COUNT_KEYS) {
                if (tag.contains(key, Tag.TAG_LONG)) {
                    return clamp(tag.getLong(key));
                }
            }
            for (String key : LONG_COUNT_KEYS) {
                if (tag.contains(key, Tag.TAG_INT)) {
                    count = Math.max(count, Integer.toUnsignedLong(tag.getInt(key)));
                }
            }
        }
        return clamp(count);
    }

    public static long add(long total, long amount) {
        if (amount <= 0L) {
            return total;
        }
        if (total >= MAX - amount) {
            return MAX;
        }
        return total + amount;
    }

    public static long multiply(long value, long multiplier) {
        if (value <= 0L || multiplier <= 0L) {
            return 0L;
        }
        if (value > MAX / multiplier) {
            return MAX;
        }
        return value * multiplier;
    }

    private static long clamp(long value) {
        if (value <= 0L) {
            return 0L;
        }
        return Math.min(value, MAX);
    }
}
