package com.jacketedrug021.vanilla_plus.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(Villager.class)
public class VillagerEntityMixin {
    @Shadow @Final private static Logger LOGGER;

    @Shadow private int villagerXp;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo info) {
        Villager villager = (Villager) (Object) this;

        // Ensure this runs on the server side only
        if (!villager.level().isClientSide && villager.level() instanceof ServerLevel) {
            long gameTime = villager.level().getGameTime();

            // Reset trades every 200 ticks (10 seconds)
            if (gameTime % 200L == 0) {
                // Save the current experience and profession
                int experience = villager.getVillagerXp();
                VillagerProfession profession = villager.getVillagerData().getProfession();
                int level = villager.getVillagerData().getLevel();

                // Clear the current offers
                villager.setOffers(new MerchantOffers());

                // Restore the experience
                villager.setVillagerXp(experience);

                // Randomize new offers based on the villager's profession and level
                addRandomTrades(villager, profession, level, villager.getRandom());

                // Restock to refresh the trades
                villager.restock();
            }
        }
    }

    private void addRandomTrades(Villager villager, VillagerProfession profession, int level, RandomSource randomSource) {
        Random random = new Random(randomSource.nextLong());
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