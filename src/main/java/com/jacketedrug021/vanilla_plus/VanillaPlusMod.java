package com.jacketedrug021.vanilla_plus;

import com.jacketedrug021.vanilla_plus.event.ModEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VanillaPlusMod.MOD_ID)
public class VanillaPlusMod {
    public static final String MOD_ID = "vanilla_plus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaPlusMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(this::handleInteractEvent);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void handleInteractEvent(final PlayerInteractEvent.RightClickBlock event) {
        if (ModEvents.attemptAnvilRepair(event.getEntity(), event.getLevel(), event.getHand(),
                event.getHitVec()) == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }
}