package com.jacketedrug021.vanilla_plus;

import com.jacketedrug021.vanilla_plus.entity.villager.goal.RepairGolemGoal;
import com.jacketedrug021.vanilla_plus.util.config.ModConfig;
import com.jacketedrug021.vanilla_plus.event.AdvBoneMealEvent;
import com.jacketedrug021.vanilla_plus.util.registry.ModBlocks;
import com.jacketedrug021.vanilla_plus.util.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VanillaPlusMod.MOD_ID)
public class VanillaPlusMod {
    public static final String MOD_ID = "vanilla_plus";
    //public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaPlusMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.addListener(this::handleFertilizerEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.NITRATE_POWDER);
            event.accept(ModItems.ADV_BONE_MEAL);
        }

        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModBlocks.NITRATE_ORE);
        }
    }
    /**
     * Handles the fertilizer event when the player right-clicks with the ADV_BONE_MEAL item.
     *
     * @param event The PlayerInteractEvent.RightClickBlock event.
     */
    public void handleFertilizerEvent(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();

        // Check only on the server side and if the item is advanced bone meal.
        if (level.isClientSide || stack.getItem() != ModItems.ADV_BONE_MEAL.get()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (!AdvBoneMealEvent.isValidCrop(state)) {
            return;
        }

        boolean applied = AdvBoneMealEvent.applyFertilizer((ServerLevel) level, pos, ModConfig.cropRadius);

        if (applied) {
            // Deduct one item from the stack
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Villager villager) {
            villager.goalSelector.addGoal(1, new RepairGolemGoal(villager));
        }
    }
}