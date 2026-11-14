package com.jp3spino.client;

import com.jp3spino.Jp3SpinosaurusMod;
import com.jp3spino.registry.ModEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientModEvents {

    /** Mod-bus events: renderer + model layer registration. */
    @Mod.EventBusSubscriber(modid = Jp3SpinosaurusMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.SPINOSAURUS.get(), SpinosaurusRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(SpinosaurusModel.LAYER, SpinosaurusModel::createBodyLayer);
        }
    }

    /** Forge-bus events: camera shake from footsteps and roars. */
    @Mod.EventBusSubscriber(modid = Jp3SpinosaurusMod.MODID, value = Dist.CLIENT)
    public static class ForgeBus {
        private static final double RANGE = 40.0D;

        @SubscribeEvent
        public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                return;
            }
            float shake = 0.0F;
            for (com.jp3spino.entity.SpinosaurusEntity e : mc.level.getEntitiesOfClass(
                    com.jp3spino.entity.SpinosaurusEntity.class, mc.player.getBoundingBox().inflate(RANGE))) {
                float falloff = (float) Math.max(0.0D, 1.0D - e.distanceTo(mc.player) / RANGE);
                float stomp = (float) Math.pow(Math.abs(Mth.cos(e.animPhase * 0.42F)), 14.0D)
                        * Mth.clamp(e.animAmount, 0.0F, 1.0F);
                float roar = e.roarTick > 0 ? 0.6F : 0.0F;
                shake = Math.max(shake, falloff * falloff * (stomp + roar));
            }
            if (shake > 0.001F) {
                float t = mc.player.tickCount + (float) event.getPartialTick();
                event.setPitch(event.getPitch() + Mth.sin(t * 1.9F) * shake * 1.2F);
                event.setRoll(event.getRoll() + Mth.cos(t * 2.3F) * shake);
            }
        }
    }
}
