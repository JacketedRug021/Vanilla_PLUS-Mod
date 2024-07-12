package com.jacketedrug021.vanilla_plus.event;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

/**
 * Utility class for handling advanced bone meal functionality.
 */
public class AdvBoneMealEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Checks if the given block state represents a valid crop for fertilization.
     *
     * @param state The block state to check.
     * @return {@code true} if the block state represents a valid crop, otherwise {@code false}.
     */
    public static boolean isValidCrop(BlockState state) {
        // Example check: Adjust this based on what you consider a valid crop
        return state.getBlock() instanceof net.minecraft.world.level.block.CropBlock;
    }

    /**
     * Applies fertilizer to the specified position and surrounding blocks within a radius.
     *
     * @param level  The server world.
     * @param pos    The position to apply fertilizer.
     * @param radius The radius within which to apply fertilizer.
     * @return {@code true} if fertilizer was applied to any block, otherwise {@code false}.
     */
    public static boolean applyFertilizer(ServerLevel level, BlockPos pos, int radius) {
        boolean applied = false;

        //Only log if debug level is enabled
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Applying fertilizer at position {}", pos);
        }

        // Apply fertilizer to the initial block
        applied |= applyFertilizerToBlock(level, pos);

        // Apply fertilizer to surrounding blocks within the specified radius
        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            if (targetPos.equals(pos)) {
                continue; // Skip the initial block, which we already fertilized
            }

            BlockState targetState = level.getBlockState(targetPos);
            if (isValidCrop(targetState)) {
                applied |= applyFertilizerToBlock(level, targetPos);
            }
        }
        return applied;
    }

    /**
     * Applies fertilizer to the block at the specified position.
     *
     * @param level The world in which the block is located.
     * @param pos The position of the block to fertilize.
     * @return True if the fertilizer was successfully applied, false otherwise.
     */
    private static boolean applyFertilizerToBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");

        if (property instanceof IntegerProperty ageProperty) {
            int currentValue = state.getValue(ageProperty);

            if (currentValue < ageProperty.getPossibleValues().size() - 1) {
                level.setBlock(pos, state.setValue(ageProperty, currentValue + 1), 3); // Increment age

                // Only log if debug level is enabled
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fertilizer applied to block at {}", pos);
                }
                return true;
            }
        }
        return false;
    }
}