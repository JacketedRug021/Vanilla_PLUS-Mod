package com.jacketedrug021.vanilla_plus.mixin;

import com.jacketedrug021.vanilla_plus.config.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
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
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo info) {
        Villager villager = (Villager) (Object) this;

        // Making sure it runs on the server side only
        if (!villager.level().isClientSide && villager.level() instanceof ServerLevel) {
            long gameTime = villager.level().getGameTime();

            // Get int value from the .Minecraft/config/vanilla_plus-common file.
            long intervalDays = ModConfig.intervalDays;
            if(gameTime % intervalDays == 0) {
                // Save current experience and profession
                int experience = villager.getVillagerXp();
                VillagerProfession profession = villager.getVillagerData().getProfession();
                int level = villager.getVillagerData().getLevel();

                // Clears the current offers.
                villager.setOffers(new MerchantOffers());

                // Restore any villager's experience, if they have any.
                villager.setVillagerXp(experience);

                // New offers for any villagers randomized based on their level, if they have any.
                addRandomTrades(villager, profession, level, villager.getRandom());
                villager.restock();
            }
        }
    }
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