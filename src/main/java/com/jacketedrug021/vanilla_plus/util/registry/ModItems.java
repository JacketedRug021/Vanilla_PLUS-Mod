package com.jacketedrug021.vanilla_plus.util.registry;

import com.jacketedrug021.vanilla_plus.VanillaPlusMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VanillaPlusMod.MOD_ID);

    public static final RegistryObject<Item> NITRATE_POWDER = ITEMS.register("nitrate_powder",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ADV_BONE_MEAL = ITEMS.register("adv_bone_meal",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}