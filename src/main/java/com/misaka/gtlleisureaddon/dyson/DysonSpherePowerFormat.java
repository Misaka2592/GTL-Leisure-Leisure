package com.misaka.gtlleisureaddon.dyson;

import org.gtlcore.gtlcore.utils.TextUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public final class DysonSpherePowerFormat {

    private DysonSpherePowerFormat() {}

    public static MutableComponent formatCurrentOutput(int launchCount) {
        return Component.empty()
                .append(Component.translatable("lleisure.dyson_sphere.current_output"))
                .append(Component.literal(String.valueOf(launchCount)).withStyle(ChatFormatting.BLUE))
                .append(Component.literal("A").withStyle(ChatFormatting.RED))
                .append(Component.literal(TextUtil.full_color("MAX")));
    }

    public static String formatEfficiencyPercent(double efficiency) {
        return String.format(Locale.ROOT, "%.2f%%", efficiency * 100.0D);
    }

    /**
     * Formats module totals up to {@link ModuleItemCounts#MAX} without {@code int} overflow.
     */
    public static String formatModuleCount(long count) {
        if (count <= 0L) {
            return "0";
        }
        if (count >= ModuleItemCounts.MAX) {
            return "MAX";
        }
        if (count < 1_000_000L) {
            return Long.toString(count);
        }
        if (count >= 1_000_000_000_000L) {
            return String.format(Locale.ROOT, "%.2E", (double) count);
        }
        double value = count;
        String suffix;
        if (value >= 1_000_000_000D) {
            value /= 1_000_000_000D;
            suffix = "G";
        } else {
            value /= 1_000_000D;
            suffix = "M";
        }
        return String.format(Locale.ROOT, "%.2f%s", value, suffix);
    }
}
