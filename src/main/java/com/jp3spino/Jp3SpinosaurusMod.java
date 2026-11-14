package com.jp3spino;

import com.jp3spino.registry.ModEntities;
import com.jp3spino.registry.ModItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Jp3SpinosaurusMod.MODID)
public class Jp3SpinosaurusMod {
    public static final String MODID = "jp3_spinosaurus";

    public Jp3SpinosaurusMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
    }
}
