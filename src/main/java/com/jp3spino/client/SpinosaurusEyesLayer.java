package com.jp3spino.client;

import com.jp3spino.Jp3SpinosaurusMod;
import com.jp3spino.entity.SpinosaurusEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Glowing green JP3 eyes (the slit pupil is black, so it stays dark in the additive glow pass). */
public class SpinosaurusEyesLayer extends EyesLayer<SpinosaurusEntity, SpinosaurusModel> {
    private static final RenderType GLOW = RenderType.eyes(
            new ResourceLocation(Jp3SpinosaurusMod.MODID, "textures/entity/spinosaurus_glow.png"));

    public SpinosaurusEyesLayer(RenderLayerParent<SpinosaurusEntity, SpinosaurusModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
