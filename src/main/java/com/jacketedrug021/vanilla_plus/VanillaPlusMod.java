package com.jacketedrug021.vanilla_plus;

import com.jacketedrug021.vanilla_plus.config.ModConfig;
import com.jacketedrug021.vanilla_plus.entity.ai.HealGolemGoal;
import com.jacketedrug021.vanilla_plus.event.ModEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(VanillaPlusMod.MOD_ID)
public class VanillaPlusMod {
    public static final String MOD_ID = "vanilla_plus";
    //public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaPlusMod() {
        MinecraftForge.EVENT_BUS.addListener(this::handleInteractEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }
    private void handleInteractEvent(final PlayerInteractEvent.RightClickBlock event) {
        if (ModEvents.attemptAnvilRepair(event.getEntity(), event.getLevel(), event.getHand(),
                event.getHitVec()) == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Villager villager) {
            villager.goalSelector.addGoal(1, new HealGolemGoal(villager));
        }
    }
}