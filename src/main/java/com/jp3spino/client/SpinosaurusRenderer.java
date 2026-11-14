package com.jp3spino.client;

import com.jp3spino.Jp3SpinosaurusMod;
import com.jp3spino.entity.SpinosaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpinosaurusRenderer extends MobRenderer<SpinosaurusEntity, SpinosaurusModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Jp3SpinosaurusMod.MODID, "textures/entity/spinosaurus.png");

    /** Model is drawn slightly oversized so the beast feels huge next to the player. */
    private static final float SCALE = 1.15F;

    public SpinosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new SpinosaurusModel(context.bakeLayer(SpinosaurusModel.LAYER)), 2.4F);
        this.addLayer(new SpinosaurusEyesLayer(this));
    }

    @Override
    protected void scale(SpinosaurusEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(SpinosaurusEntity entity) {
        return TEXTURE;
    }
}
