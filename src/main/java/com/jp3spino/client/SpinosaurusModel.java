package com.jp3spino.client;

import com.jp3spino.Jp3SpinosaurusMod;
import com.jp3spino.entity.SpinosaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Jurassic Park III Spinosaurus: tall dorsal sail, long narrow croc-like skull, heavy digitigrade hind legs,
 * short clawed forearms and a long tail. Model space: front is -Z, ground is y = 24.
 * UV offsets must stay identical to tools/make_texture.py (256x256 sheet).
 */
public class SpinosaurusModel extends EntityModel<SpinosaurusEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(Jp3SpinosaurusMod.MODID, "spinosaurus"), "main");

    private static final float DEG = (float) Math.PI / 180.0F;

    // resting leg pose (digitigrade): thigh forward, shin back, foot flat
    private static final float THIGH_REST = -0.45F;
    private static final float SHIN_REST = 1.0F;
    private static final float FOOT_ABS = -0.05F;

    private static final float[] SAIL_H = {10F, 16F, 22F, 26F, 22F, 14F};
    private static final float[] SAIL_Z = {-15F, -9F, -3F, 3F, 9F, 15F};

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart legR;
    private final ModelPart shinR;
    private final ModelPart footR;
    private final ModelPart legL;
    private final ModelPart shinL;
    private final ModelPart footL;
    private final ModelPart armR;
    private final ModelPart foreR;
    private final ModelPart armL;
    private final ModelPart foreL;
    private final ModelPart[] sail = new ModelPart[SAIL_H.length];

    public SpinosaurusModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail1 = body.getChild("tail1");
        this.tail2 = tail1.getChild("tail2");
        this.tail3 = tail2.getChild("tail3");
        this.armR = body.getChild("arm_right");
        this.foreR = armR.getChild("forearm");
        this.armL = body.getChild("arm_left");
        this.foreL = armL.getChild("forearm");
        this.legR = root.getChild("leg_right");
        this.shinR = legR.getChild("shin");
        this.footR = shinR.getChild("foot");
        this.legL = root.getChild("leg_left");
        this.shinL = legL.getChild("shin");
        this.footL = shinL.getChild("foot");
        ModelPart sailGroup = body.getChild("sail");
        for (int i = 0; i < sail.length; i++) {
            sail[i] = sailGroup.getChild("s" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // torso (pivot at the middle of the body, ~46 units above ground)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-9F, -11F, -20F, 18F, 22F, 40F),
                PartPose.offsetAndRotation(0F, -22F, 2F, 0.04F, 0F, 0F));

        // dorsal sail made of 6 stepped slabs
        PartDefinition sailGroup = body.addOrReplaceChild("sail", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < SAIL_H.length; i++) {
            sailGroup.addOrReplaceChild("s" + i,
                    CubeListBuilder.create().texOffs(190, 0).addBox(-1F, -SAIL_H[i], -3F, 2F, SAIL_H[i], 6F),
                    PartPose.offset(0F, -10.5F, SAIL_Z[i]));
        }

        // neck + head + jaw
        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 132).addBox(-5F, -5.5F, -14F, 10F, 11F, 16F),
                PartPose.offsetAndRotation(0F, -6F, -16F, -0.35F, 0F, 0F));
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(52, 132).addBox(-4.5F, -4.5F, -10F, 9F, 9F, 12F)     // skull
                        .texOffs(96, 132).addBox(-3F, -3.5F, -30F, 6F, 5F, 20F)       // long snout
                        .texOffs(150, 132).addBox(-4F, -4F, -35F, 8F, 7F, 5F)         // rosette snout tip
                        .texOffs(178, 132).addBox(-1F, -8F, -9F, 2F, 4F, 10F)         // crest
                        .texOffs(70, 162).addBox(-3.5F, 1.4F, -29F, 7F, 2F, 19F),     // upper teeth
                PartPose.offsetAndRotation(0F, -0.5F, -13F, 0.38F, 0F, 0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(0, 162).addBox(-3F, 0F, -28F, 6F, 3F, 28F)           // lower jaw
                        .texOffs(124, 162).addBox(-3.5F, -2F, -27F, 7F, 2F, 22F),     // lower teeth
                PartPose.offset(0F, 2.2F, -2F));

        // tail (three segments)
        PartDefinition tail1 = body.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(116, 0).addBox(-6F, -7F, 0F, 12F, 14F, 24F),
                PartPose.offsetAndRotation(0F, -1F, 20F, 0.06F, 0F, 0F));
        PartDefinition tail2 = tail1.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(116, 40).addBox(-4.5F, -5F, 0F, 9F, 10F, 26F),
                PartPose.offset(0F, 0.5F, 23F));
        tail2.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(116, 78).addBox(-3F, -3.5F, 0F, 6F, 7F, 28F),
                PartPose.offset(0F, 0F, 25F));

        // short forearms with three claws each
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition arm = body.addOrReplaceChild(side < 0 ? "arm_right" : "arm_left",
                    CubeListBuilder.create().texOffs(0, 196).addBox(-2.5F, -1F, -2.5F, 5F, 12F, 5F),
                    PartPose.offsetAndRotation(side * 10F, 4F, -18F, -0.5F, 0F, 0F));
            arm.addOrReplaceChild("forearm",
                    CubeListBuilder.create()
                            .texOffs(22, 196).addBox(-2F, 0F, -2F, 4F, 11F, 4F)
                            .texOffs(40, 196).addBox(-2.5F, 8F, -2.5F, 5F, 3F, 5F)
                            .texOffs(62, 196)
                            .addBox(-2.5F, 11F, -0.5F, 1F, 5F, 1F)
                            .addBox(-0.5F, 11F, -0.5F, 1F, 5F, 1F)
                            .addBox(1.5F, 11F, -0.5F, 1F, 5F, 1F),
                    PartPose.offsetAndRotation(0F, 11F, 0F, -1.1F, 0F, 0F));
        }

        // hind legs: thigh -> shin -> foot (digitigrade)
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition leg = root.addOrReplaceChild(side < 0 ? "leg_right" : "leg_left",
                    CubeListBuilder.create().texOffs(0, 64).addBox(-5F, -3F, -7F, 10F, 22F, 14F),
                    PartPose.offsetAndRotation(side * 11F, -19.5F, 6F, THIGH_REST, 0F, 0F));
            PartDefinition shin = leg.addOrReplaceChild("shin",
                    CubeListBuilder.create().texOffs(48, 64).addBox(-3.5F, -1F, -3.5F, 7F, 26F, 7F),
                    PartPose.offsetAndRotation(0F, 19F, 1F, SHIN_REST, 0F, 0F));
            shin.addOrReplaceChild("foot",
                    CubeListBuilder.create()
                            .texOffs(0, 102).addBox(-5F, 0F, -12F, 10F, 5F, 14F)
                            .texOffs(0, 124).addBox(-5F, 1F, -15F, 10F, 3F, 3F),
                    PartPose.offsetAndRotation(0F, 25F, 0F, FOOT_ABS - THIGH_REST - SHIN_REST, 0F, 0F));
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(SpinosaurusEntity e, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float partial = ageInTicks - e.tickCount;
        e.animPhase = limbSwing;
        e.animAmount = limbSwingAmount;
        float amt = Math.min(limbSwingAmount, 1.0F);

        // ---- bite envelope: jaw snaps open, lunges, snaps shut
        float bite = 0.0F;
        if (e.biteTick > 0) {
            float p = Mth.clamp(1.0F - (e.biteTick - partial) / SpinosaurusEntity.BITE_TICKS, 0.0F, 1.0F);
            bite = p < 0.4F ? p / 0.4F : (1.0F - p) / 0.6F;
            bite = bite * bite * (3.0F - 2.0F * bite);
        }

        // ---- roar envelope: rear back, jaw wide, tremble
        float roar = 0.0F;
        if (e.roarTick > 0) {
            float p = Mth.clamp(1.0F - (e.roarTick - partial) / SpinosaurusEntity.ROAR_TICKS, 0.0F, 1.0F);
            roar = Mth.clamp(Math.min(p / 0.18F, (1.0F - p) / 0.18F), 0.0F, 1.0F);
        }
        float tremble = Mth.sin(ageInTicks * 3.1F) * roar;

        // ---- walk cycle
        float c = limbSwing * 0.42F;
        float swingR = Mth.cos(c) * 0.85F * amt;
        float swingL = Mth.cos(c + Mth.PI) * 0.85F * amt;
        setLeg(legR, shinR, footR, swingR);
        setLeg(legL, shinL, footL, swingL);

        // ---- body
        float step = Mth.cos(c * 2.0F);
        body.y = -22.0F + step * 0.9F * amt;
        body.z = 2.0F - bite * 4.0F;
        body.xRot = 0.04F + bite * 0.07F - roar * 0.16F + Mth.sin(c * 2.0F) * 0.015F * amt;
        body.yRot = Mth.cos(c) * 0.05F * amt;
        body.zRot = Mth.cos(c) * 0.03F * amt + tremble * 0.015F;

        // ---- neck / head / jaw
        float aggressive = e.isAggressive() ? 1.0F : 0.0F;
        neck.xRot = -0.35F + Mth.sin(ageInTicks * 0.06F) * 0.015F - Mth.sin(c * 2.0F) * 0.03F * amt
                + bite * 0.25F - roar * 0.5F + aggressive * 0.08F;
        neck.yRot = -Mth.cos(c) * 0.06F * amt;
        head.xRot = 0.38F + headPitch * DEG * 0.5F + bite * 0.35F - roar * 0.55F;
        head.yRot = Mth.clamp(netHeadYaw, -50.0F, 50.0F) * DEG * 0.8F;
        head.zRot = tremble * 0.02F;
        jaw.xRot = 0.03F + Mth.sin(ageInTicks * 0.09F) * 0.02F + aggressive * 0.12F
                + bite * 0.9F + roar * (0.75F + Mth.sin(ageInTicks * 2.4F) * 0.06F);

        // ---- tail whip / sway
        float sway = Mth.sin(ageInTicks * 0.08F) * 0.04F;
        tail1.yRot = sway + Mth.cos(c) * 0.12F * amt;
        tail2.yRot = sway * 1.3F + Mth.cos(c - 0.8F) * 0.16F * amt;
        tail3.yRot = sway * 1.6F + Mth.cos(c - 1.6F) * 0.20F * amt;
        tail1.xRot = 0.06F + roar * 0.2F - bite * 0.05F + Mth.sin(c * 2.0F) * 0.02F * amt;
        tail2.xRot = -0.05F + roar * 0.1F;
        tail3.xRot = -0.08F + roar * 0.1F;

        // ---- arms
        float armSwing = Mth.cos(c) * 0.18F * amt;
        float idleArm = Mth.sin(ageInTicks * 0.07F) * 0.04F;
        armR.xRot = -0.5F + armSwing + idleArm - bite * 0.7F - roar * 0.5F;
        armL.xRot = -0.5F - armSwing - idleArm - bite * 0.7F - roar * 0.5F;
        foreR.xRot = -1.1F - bite * 0.3F - roar * 0.4F;
        foreL.xRot = -1.1F - bite * 0.3F - roar * 0.4F;
        armR.zRot = 0.35F * roar;
        armL.zRot = -0.35F * roar;

        // ---- sail ripples when roaring
        for (int i = 0; i < sail.length; i++) {
            sail[i].zRot = Mth.sin(ageInTicks * 1.3F + i) * 0.06F * roar;
        }
    }

    private static void setLeg(ModelPart leg, ModelPart shin, ModelPart foot, float swing) {
        leg.xRot = THIGH_REST + swing;
        float lift = Math.max(0.0F, -swing);
        shin.xRot = SHIN_REST + lift * 1.0F - Math.max(0.0F, swing) * 0.25F;
        // keep the foot planted flat; toes tip down a little while the leg is lifted
        foot.xRot = FOOT_ABS - leg.xRot - shin.xRot + lift * 0.35F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
