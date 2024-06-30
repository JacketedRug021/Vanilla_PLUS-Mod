package com.jacketedrug021.vanilla_plus.config;

import com.jacketedrug021.vanilla_plus.VanillaPlusMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = VanillaPlusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.LongValue INTERVAL_DAYS = BUILDER
            .comment("How many days required for the villager trades to be refreshed. (3 Days by Default)")
            .defineInRange("IntervalDays",72000,1L,Integer.MAX_VALUE);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    public static long intervalDays;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        intervalDays = INTERVAL_DAYS.get();
    }
}
