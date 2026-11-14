package com.jp3spino.registry;

import com.jp3spino.Jp3SpinosaurusMod;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Jp3SpinosaurusMod.MODID);

    public static final RegistryObject<Item> SPINOSAURUS_SPAWN_EGG = ITEMS.register("spinosaurus_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SPINOSAURUS, 0xA8512E, 0x8B8F96, new Item.Properties()));
}
