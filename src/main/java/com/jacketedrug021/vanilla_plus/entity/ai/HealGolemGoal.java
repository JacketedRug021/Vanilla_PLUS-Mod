package com.jacketedrug021.vanilla_plus.entity.ai;

import com.jacketedrug021.vanilla_plus.util.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jacketedrug021.vanilla_plus.util.config.ModConfig.sightRange;

public class HealGolemGoal extends Goal {
    private static final Set<IronGolem> TARGETED_GOLEMS = Collections.synchronizedSet(new HashSet<>());
    private final Villager villager;
    private IronGolem targetGolem;

    public HealGolemGoal(Villager villager) {
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
            for (IronGolem golem : golems) {
                if (golem.getHealth() < golem.getMaxHealth() && !TARGETED_GOLEMS.contains(golem)) {
                    if (isSafeToHeal(golem)) {
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
                // Send chat message to nearby players in the level
                if (villager.level() instanceof ServerLevel serverLevel) {
                    String message = "Iron Golem was repaired by an Armorer Villager!";
                    serverLevel.players().forEach(player -> {
                        if (player.distanceTo(targetGolem) <= ModConfig.notifyRange) {
                            if (targetGolem.getHealth() == targetGolem.getMaxHealth()) {
                                if (ModConfig.notifyToggle) {
                                    player.sendSystemMessage(Component.literal(message));
                                }
                            }
                        }
                    });
                }
            }
            synchronized (TARGETED_GOLEMS) {
                TARGETED_GOLEMS.remove(targetGolem);
            }

            // Play repair sound
            Level level = villager.level();
            level.playSound(null, targetGolem.getX(),targetGolem.getY(), targetGolem.getZ(),
                    SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 1.0F, 1.0F);

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
    private boolean isSafeToHeal(IronGolem golem) {
        AABB boundingBox = new AABB(golem.blockPosition()).inflate(ModConfig.safetyRadius);
        Level level = golem.level();
        List<Entity> nearbyEntities = level.getEntities((Entity) null, boundingBox, entity -> entity instanceof Monster);
        return nearbyEntities.isEmpty();
    }
}