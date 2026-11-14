package com.jp3spino.registry;

import com.jp3spino.Jp3SpinosaurusMod;
import com.jp3spino.entity.SpinosaurusEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Jp3SpinosaurusMod.MODID);

    public static final RegistryObject<EntityType<SpinosaurusEntity>> SPINOSAURUS = ENTITIES.register("spinosaurus",
            () -> EntityType.Builder.<SpinosaurusEntity>of(SpinosaurusEntity::new, MobCategory.MONSTER)
                    .sized(3.4F, 4.8F)
                    .clientTrackingRange(16)
                    .build("spinosaurus"));
}
