package com.jacketedrug021.vanilla_plus.entity.villager.goal;

import com.jacketedrug021.vanilla_plus.util.config.ModConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jacketedrug021.vanilla_plus.util.config.ModConfig.sightRange;

public class RepairGolemGoal extends Goal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<IronGolem> TARGETED_GOLEMS = Collections.synchronizedSet(new HashSet<>());
    private final Villager villager;
    private IronGolem targetGolem;

    public RepairGolemGoal(Villager villager) {
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
        // Ensure only Armorers can use this goal
        if (villager.getVillagerData().getProfession() != VillagerProfession.ARMORER) {
            return false;
        }

        // Cancel if the villager is sleeping
        if (villager.isSleeping()) {
            return false;
        }

        Level level = villager.level();
        AABB boundingBox = new AABB(villager.blockPosition()).inflate(sightRange);
        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class, boundingBox);

        synchronized (TARGETED_GOLEMS) {
            //TODO: Add an config variable to determined the required health threshold to repair the Iron Golem.
            for (IronGolem golem : golems) {
                if (golem.getHealth() < golem.getMaxHealth() && !TARGETED_GOLEMS.contains(golem)) {
                    if (isSafe(golem)) {
                        this.targetGolem = golem;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (targetGolem != null) {
            synchronized (TARGETED_GOLEMS) {
                if (TARGETED_GOLEMS.contains(targetGolem)) {
                    targetGolem = null; // Another armorer is already targeting this golem
                } else {
                    TARGETED_GOLEMS.add(targetGolem);
                    villager.getNavigation().moveTo(targetGolem, ModConfig.speedModifier); // Default speed of 0.7

                    // Set the villager to hold an iron ingot
                    villager.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));
                }
            }
        }
    }

    @Override
    public void tick() {
        if (targetGolem != null && villager.distanceTo(targetGolem) < 2.0) {
            targetGolem.heal(targetGolem.getMaxHealth() * (float) ModConfig.repairAmount);

            // Check if the Iron Golem is at max health after healing
            if (targetGolem.getHealth() >= targetGolem.getMaxHealth()) {
                sendRepairMessage();
            }
            synchronized (TARGETED_GOLEMS) {
                TARGETED_GOLEMS.remove(targetGolem);
            }
            repairSound();

            // Remove the iron ingot from the villager's hand
            villager.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

            targetGolem = null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        // Cancel if the villager is sleeping
        if (villager.isSleeping()) {
            return false;
        }
        // Continue to use the goal if there's a valid targetGolem and the villager is within 2.0 blocks of it
        return targetGolem != null && villager.distanceTo(targetGolem) <= 2.0;
    }

    @Override
    public void stop() {
        if (targetGolem != null) {
            synchronized (TARGETED_GOLEMS) {
                TARGETED_GOLEMS.remove(targetGolem);
            }
            targetGolem = null;
        }
    }
    private boolean isSafe(IronGolem golem) {
        AABB boundingBox = new AABB(golem.blockPosition()).inflate(ModConfig.safetyRadius);
        Level level = golem.level();
        List<Entity> nearbyEntities = level.getEntities((Entity) null, boundingBox, entity -> entity instanceof Monster);
        return nearbyEntities.isEmpty();
    }

    private void repairSound() {
        Level level = villager.level();
        level.playSound(null, targetGolem.getX(), targetGolem.getY(), targetGolem.getZ(),
                SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 1.0F, 1.0F);
        LOGGER.debug("Played repair sound for Iron Golem: " + targetGolem);
    }

    private void sendRepairMessage() {
        Level level = villager.level();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.players().forEach(player -> {
                if (player.distanceTo(targetGolem) <= ModConfig.notifyRange && ModConfig.notifyToggle) {
                    player.sendSystemMessage(Component.literal("Iron Golem was repaired by an Armorer Villager!"));
                }
            });
            LOGGER.debug("Sent repair message to players nearby the Iron Golem: " + targetGolem);
        }
    }
}