package com.jacketedrug021.vanilla_plus.util.config;

import com.jacketedrug021.vanilla_plus.VanillaPlusMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = VanillaPlusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    // ========== Configuration builder and specification ==================================== \\
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // ========== Configuration values ============================================================================== \\
    public static final ForgeConfigSpec.DoubleValue REPAIR_AMOUNT;
    public static final ForgeConfigSpec.LongValue INTERVAL_DAYS;
    public static final ForgeConfigSpec.DoubleValue NOTIFY_RANGE;
    public static final ForgeConfigSpec.BooleanValue NOTIFY_TOGGLE;
    public static final ForgeConfigSpec.DoubleValue SAFETY_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SIGHT_RANGE;
    public static final ForgeConfigSpec.DoubleValue SPEED_MODIFIER;
    public static final ForgeConfigSpec.IntValue CROPRADIUS;

    // ========== Configuration fields ============================================================================== \\
    public static long intervalDays;
    public static double notifyRange;
    public static boolean notifyToggle;
    public static double repairAmount;
    public static double safetyRadius;
    public static double sightRange;
    public static double speedModifier;
    public static int cropRadius;

    static {
        BUILDER.comment("Villager Settings").push("Villager");
        INTERVAL_DAYS = BUILDER
                .comment("How many days required for the villager trades to be refreshed. (3 days by default)")
                .defineInRange("intervalDays", 72000L, 1L, Integer.MAX_VALUE);
        SIGHT_RANGE = BUILDER
                .comment("Range needed to detect wounded Iron Golem. (20 range by default)")
                .defineInRange("sightRange",20.0,1.0,Double.MAX_VALUE);
        SPEED_MODIFIER = BUILDER
                .comment("Speed modifier for the selected villager to travel in order to repair the Iron Golem. (0.7 by default)")
                .defineInRange("speedModifier", 0.7, 0.1, 10.0);
        BUILDER.pop();

        BUILDER.comment("Iron Golem Settings").push("ironGolem");
        SAFETY_RADIUS = BUILDER
                .comment("Safe radius around the targeted golem in order to be repaired.")
                .defineInRange("safetyRadius", 10.0, 1.0, Double.MAX_VALUE);
        REPAIR_AMOUNT = BUILDER
                .comment("Heal amount when villager starts to repair the Iron Golem. (25% default)")
                .defineInRange("repairAmount", 0.25, 0.01,Double.MAX_VALUE);

        BUILDER.pop();

        BUILDER.comment("Other Settings").push("notification");
        NOTIFY_TOGGLE = BUILDER
                .comment("Enable notification when a villagers repaired an Iron Golem.")
                .define("notifyToggle", true);
        NOTIFY_RANGE = BUILDER
                .comment("Notification distance from the Iron Golem repaired and the player")
                .defineInRange("notifyRange", 50.0, 1.0, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("MICS Setting").push("Fertilizer Setting");
        CROPRADIUS = BUILDER
                .comment("Range the growth of the crops are when using Advanced bone meal. (3 blocks radius by default)")
                .defineInRange("cropRadius", 3, 1, Integer.MAX_VALUE);
    BUILDER.pop();

        // Build the configuration specification
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        // Load configuration values

        intervalDays = INTERVAL_DAYS.get();
        notifyRange = NOTIFY_RANGE.get();
        notifyToggle = NOTIFY_TOGGLE.get();
        repairAmount = REPAIR_AMOUNT.get();
        safetyRadius = SAFETY_RADIUS.get();
        sightRange =SIGHT_RANGE.get();
        speedModifier = SPEED_MODIFIER.get();
        cropRadius = CROPRADIUS.get();
    }
}
