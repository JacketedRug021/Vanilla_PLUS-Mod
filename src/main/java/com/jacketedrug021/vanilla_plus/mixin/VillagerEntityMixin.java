package com.jacketedrug021.vanilla_plus.mixin;

import com.jacketedrug021.vanilla_plus.util.config.ModConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(Villager.class)
public class VillagerEntityMixin {
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo info) {
        Villager villager = (Villager) (Object) this;

        // Making sure it runs on the server side only
        if (!villager.level().isClientSide && villager.level() instanceof ServerLevel) {
            long intervalDays = ModConfig.intervalDays;
            long gameTime = villager.level().getGameTime();

            // Check in-game days to reset trades.
            if(gameTime % intervalDays == 0) resetTrades(villager);
        }
    }
    @Unique
    private void resetTrades(Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        int experience = villager.getVillagerXp();
        int level = villager.getVillagerData().getLevel();

        // Clears current trades & restore villagers xp
        villager.setOffers(new MerchantOffers());
        villager.setVillagerXp(experience);

        // New trade offers for any villagers randomized based on their level, if they have any.
        addRandomTrades(villager, profession, level, villager.getRandom());
        villager.restock();
    }
    @Unique
    private void addRandomTrades(Villager villager, VillagerProfession profession, int level, RandomSource randomSource) {
        Random random;
        random = new Random(randomSource.nextLong());
        for (int i = 1; i <= level; i++) {
            VillagerTrades.ItemListing[] trades = VillagerTrades.TRADES.get(profession).get(i);
            if (trades != null && trades.length > 0) {
                List<VillagerTrades.ItemListing> tradeList = new ArrayList<>(List.of(trades));
                Collections.shuffle(tradeList, random);
                MerchantOffers offers = villager.getOffers();
                int tradeCount = Math.min(2, tradeList.size());
                for (int j = 0; j < tradeCount; j++) {
                    MerchantOffer offer = tradeList.get(j).getOffer(villager, randomSource);
                    if (offer != null) {
                        offers.add(offer);
                    }
                }
            }
        }
    }
}