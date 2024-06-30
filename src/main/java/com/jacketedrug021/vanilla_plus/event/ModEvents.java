package com.jacketedrug021.vanilla_plus.event;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

import java.lang.module.ResolutionException;
import java.util.Objects;

import static com.jacketedrug021.vanilla_plus.VanillaPlusMod.MOD_ID;

public class ModEvents {
    private static final ResourceLocation ADVANCEMENT_ID = new ResourceLocation(MOD_ID, "story/repair_anvil");
    public static final Logger LOGGER = LogUtils.getLogger();
    public static InteractionResult attemptAnvilRepair(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide && !player.isSpectator()) {
            final BlockPos pos = hitResult.getBlockPos();
            final BlockState blockState = level.getBlockState(pos);

            // Check if the block is an Anvil
            if(isRepairable(blockState)) {
                ItemStack heldItem = player.getItemInHand(hand);
                // Check if the Anvil is partially damaged or heavily damaged...
                if(isChippedAnvil(blockState) && heldItem.getItem() == Items.IRON_BLOCK && heldItem.getCount() > 1){
                    heldItem.shrink(1);

                    LOGGER.info("Anvil has a minor damage, repairing...");
                    repairAnvil(level, pos, blockState.getBlock(), blockState.getValue(AnvilBlock.FACING));

                    if (player instanceof ServerPlayer serverPlayer) {
                        awardAdvancement(serverPlayer);
                    }
                    return InteractionResult.SUCCESS;

                } else if (isDamagedAnvil(blockState) && heldItem.getItem() == Items.IRON_BLOCK && heldItem.getCount() >= 2) {
                    heldItem.shrink(2);

                    LOGGER.info("Anvil has damage everywhere, repairing...");
                    repairAnvil(level, pos, blockState.getBlock(), blockState.getValue(AnvilBlock.FACING));

                    if (player instanceof ServerPlayer serverPlayer) {
                        awardAdvancement(serverPlayer);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
    private  static boolean isChippedAnvil(BlockState state) {
        return state.is(Blocks.CHIPPED_ANVIL);
    }
    private  static boolean isDamagedAnvil(BlockState state) {
        return  state.is(Blocks.DAMAGED_ANVIL);
    }
    private  static boolean isRepairable(BlockState state) {
        return state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL);
    }

    private static void repairAnvil(Level level, BlockPos pos, Block block, Direction direction) {
        BlockState newState = null;

        if(block == Blocks.CHIPPED_ANVIL) {
            newState = Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING,direction);
        } else if (block == Blocks.DAMAGED_ANVIL) {
            newState = Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING,direction);
        }

        if(newState != null) {
            level.setBlockAndUpdate(pos,newState);
            level.levelEvent(LevelEvent.SOUND_ANVIL_USED,pos,0);
        }
    }

    private static void awardAdvancement(ServerPlayer player) {
        final Advancement toGrant = Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(ModEvents.ADVANCEMENT_ID);
        if(toGrant != null) {
            final AdvancementProgress progress = player.getAdvancements().getOrStartProgress(toGrant);
            if(!progress.isDone()) {
                for(String remainingCriteria : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(toGrant, remainingCriteria);
                }
            }
            else {
                throw new ResolutionException("No advancement found for ID: " + ModEvents.ADVANCEMENT_ID);
            }
        }
    }
}