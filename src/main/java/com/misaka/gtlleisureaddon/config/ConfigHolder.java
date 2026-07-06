package com.misaka.gtlleisureaddon.config;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GTLLeisureAddon.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    @Configurable.Comment("Recipe overrides (including KubeJS recipes)")
    public Recipes recipes = new Recipes();

    @Configurable
    @Configurable.Comment("Dyson sphere launch behaviour overrides (hot-reloadable)")
    public DysonSphere dysonSphere = new DysonSphere();

    public static class Recipes {

        @Configurable
        @Configurable.Comment("Override kubejs dyson_swarm_module: scale input fluids to 10%, output 1024 modules. Reload recipes after change.")
        public boolean dysonSwarmModuleOverride = true;
    }

    public static class DysonSphere {

        @Configurable
        @Configurable.Comment("Enable lleisure Dyson sphere launch mixins")
        public boolean enabled = true;

        @Configurable
        @Configurable.Comment("Maximum launched module count")
        @Configurable.Range(min = 10000, max = 100000000)
        public int maxLaunchModules = 1_000_000;

        @Configurable
        @Configurable.Comment("Minimum modules consumed per launch")
        @Configurable.Range(min = 1, max = 1000000)
        public int minModulesPerLaunch = 64;

        @Configurable
        @Configurable.Comment("Fraction of remaining launch capacity deployed per launch, and consumed per damage repair tick (also sets launch count gain)")
        public double remainingModuleFraction = 0.03D;

        @Configurable
        @Configurable.Comment("Module cost divisor at 0% progress (10 = consume batch/10 modules per launch)")
        public double maxLaunchEfficiency = 10.0D;

        @Configurable
        @Configurable.Comment("Module cost divisor at >= minEfficiencyProgress (0.2 = consume batch*5 modules)")
        public double minLaunchEfficiency = 0.2D;

        @Configurable
        @Configurable.Comment("Progress fraction where efficiency reaches its minimum (0.8 = 80%)")
        public double minEfficiencyProgress = 0.8D;
    }
}