package com.misaka.gtlleisureaddon.dyson;

import com.misaka.gtlleisureaddon.config.ConfigHolder;

/**
 * Launch module count and efficiency calculations for {@code DysonSphereMachine} mixins.
 * Reads {@link ConfigHolder} on every call so Configuration UI changes apply immediately.
 */
public final class DysonSphereLaunchLogic {

    private DysonSphereLaunchLogic() {}

    public static boolean isEnabled() {
        return ConfigHolder.INSTANCE != null && ConfigHolder.INSTANCE.dysonSphere.enabled;
    }

    public static int getMaxModules() {
        if (ConfigHolder.INSTANCE == null) {
            return 1_000_000;
        }
        int configured = ConfigHolder.INSTANCE.dysonSphere.maxLaunchModules;
        return clamp(configured, 10_000, 100_000_000);
    }

    public static int getRemainingModules(int launched) {
        return Math.max(0, getMaxModules() - Math.max(0, launched));
    }

    /**
     * Deployment batch per launch: {@code min(max(remaining * fraction, minPerLaunch), remaining)}.
     * This value is added to {@code DysonSphereData} each successful launch.
     */
    public static int getDeploymentBatch(int launched) {
        if (ConfigHolder.INSTANCE == null) {
            return 64;
        }
        var cfg = ConfigHolder.INSTANCE.dysonSphere;
        int remaining = getRemainingModules(launched);
        if (remaining <= 0) {
            return cfg.minModulesPerLaunch;
        }
        long scaled = (long) Math.ceil(remaining * cfg.remainingModuleFraction);
        int batch = (int) Math.max(cfg.minModulesPerLaunch, scaled);
        return Math.min(batch, remaining);
    }

    /**
     * Modules actually consumed per launch: {@code deploymentBatch / launchEfficiency}.
     * 10x efficiency divides cost by 10; 0.2x efficiency multiplies cost by 5.
     */
    public static int getModulesPerLaunch(int launched) {
        double efficiency = getLaunchEfficiency(launched);
        if (efficiency <= 0.0D) {
            efficiency = ConfigHolder.INSTANCE != null
                    ? ConfigHolder.INSTANCE.dysonSphere.minLaunchEfficiency
                    : 0.2D;
        }
        double raw = getDeploymentBatch(launched) / efficiency;
        return Math.max(1, (int) Math.ceil(raw));
    }

    /**
     * Exponential launch efficiency: {@code maxEff * exp(-k * x)} for {@code x < floorProgress}, else {@code minEff}.
     * {@code x = launched / maxModules}.
     */
    public static double getLaunchEfficiency(int launched) {
        if (ConfigHolder.INSTANCE == null) {
            return 10.0;
        }
        var cfg = ConfigHolder.INSTANCE.dysonSphere;
        int max = getMaxModules();
        if (max <= 0) {
            return cfg.minLaunchEfficiency;
        }
        double progress = Math.min(1.0, Math.max(0.0, (double) launched / max));
        if (progress >= cfg.minEfficiencyProgress) {
            return cfg.minLaunchEfficiency;
        }
        double decay = computeDecayConstant(cfg.maxLaunchEfficiency, cfg.minLaunchEfficiency, cfg.minEfficiencyProgress);
        return cfg.maxLaunchEfficiency * Math.exp(-decay * progress);
    }

    /**
     * Layers deployed per successful launch: the 3% remaining-capacity batch.
     */
    public static int computeLaunchGain(int launched) {
        return getDeploymentBatch(launched);
    }

    public static int clampLaunchTotal(int launched, int gain) {
        if (gain <= 0) {
            return launched;
        }
        long total = (long) launched + gain;
        return (int) Math.min(getMaxModules(), total);
    }

    /** For {@code launched <= limit} comparisons equivalent to {@code launched < maxModules}. */
    public static int getLaunchCapInclusiveLimit() {
        return Math.max(0, getMaxModules() - 1);
    }

    /**
     * Modules consumed to prevent one +1% damage tick: {@code ceil(remaining * remainingModuleFraction)},
     * halved (rounded up) while damage is below 60%.
     */
    public static long getRepairModuleCost(int launched, int damagePercent) {
        long remaining = getRemainingModules(launched);
        if (remaining <= 0L) {
            return 1L;
        }
        double fraction = ConfigHolder.INSTANCE != null
                ? ConfigHolder.INSTANCE.dysonSphere.remainingModuleFraction
                : 0.03D;
        long cost = (long) Math.ceil(remaining * fraction);
        cost = Math.max(1L, Math.min(cost, remaining));
        if (damagePercent < 60) {
            cost = Math.max(1L, (cost + 1L) / 2L);
        }
        return Math.min(cost, ModuleItemCounts.MAX);
    }

    private static double computeDecayConstant(double maxEff, double minEff, double floorProgress) {
        if (floorProgress <= 0.0D || maxEff <= 0.0D || minEff <= 0.0D || minEff >= maxEff) {
            return 1.0D;
        }
        return -Math.log(minEff / maxEff) / floorProgress;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
