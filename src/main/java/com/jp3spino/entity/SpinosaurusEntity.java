package com.jp3spino.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

/**
 * The Jurassic Park III Spinosaurus.
 * Fast in water, brutal on land, roars when it spots prey and goes berserk below 50% health.
 */
public class SpinosaurusEntity extends Monster {

    /** Animation lengths in ticks (shared with the client model). */
    public static final int BITE_TICKS = 14;
    public static final int ROAR_TICKS = 50;

    private static final UUID ENRAGE_SPEED_ID = UUID.fromString("5c1f2a0e-3d4b-4e6a-9b7c-1a2b3c4d5e6f");
    private static final UUID ENRAGE_DAMAGE_ID = UUID.fromString("7d2e3b1f-4a5c-4f7b-8c9d-2b3c4d5e6f70");

    /** Animation counters (counted down on both client and server; client is driven by entity events). */
    public int biteTick;
    public int roarTick;

    /** Written by the client model each frame so the camera-shake code can read the leg phase. */
    public float animPhase;
    public float animAmount;

    private int roarCooldown;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.jp3_spinosaurus.spinosaurus"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    public SpinosaurusEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 240.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(ForgeMod.SWIM_SPEED.get(), 2.0D);
    }

    public static boolean checkSpawnRules(EntityType<SpinosaurusEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    // ------------------------------------------------------------------ AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, 6, true, false, e -> true));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        boolean newTarget = target != null && this.getTarget() == null;
        super.setTarget(target);
        if (newTarget && !this.level().isClientSide && this.roarCooldown <= 0) {
            this.startRoar();
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.roarTick > 0;
    }

    // ------------------------------------------------------------------ combat

    @Override
    public boolean doHurtTarget(Entity target) {
        this.biteTick = BITE_TICKS;
        this.level().broadcastEntityEvent(this, (byte) 4);
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.45D, 0.0D));
            target.hurtMarked = true;
            this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.5F, 0.6F);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.isAlive()
                && !this.isEnraged() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.enrage();
        }
        return result;
    }

    private boolean isEnraged() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(ENRAGE_SPEED_ID) != null;
    }

    private void enrage() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(ENRAGE_SPEED_ID) == null) {
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_SPEED_ID, "Enraged speed", 0.25D,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && damage.getModifier(ENRAGE_DAMAGE_ID) == null) {
            damage.addPermanentModifier(new AttributeModifier(ENRAGE_DAMAGE_ID, "Enraged damage", 0.35D,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
        this.roarCooldown = 0;
        this.startRoar();
    }

    /** Starts the roar animation, sound and (mid-roar) shockwave. Server only. */
    public void startRoar() {
        if (this.level().isClientSide || this.roarTick > 0) {
            return;
        }
        this.roarTick = ROAR_TICKS;
        this.roarCooldown = 400;
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, (byte) 5);
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 5.0F, 0.55F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 5.0F, 0.4F);
    }

    private void roarBlast() {
        AABB area = this.getBoundingBox().inflate(16.0D);
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, area,
                x -> x != this && x.isAlive() && !(x instanceof SpinosaurusEntity))) {
            Vec3 away = e.position().subtract(this.position());
            Vec3 flat = new Vec3(away.x, 0.0D, away.z);
            if (flat.lengthSqr() < 1.0E-4D) {
                continue;
            }
            flat = flat.normalize();
            double dist = Math.sqrt(away.x * away.x + away.z * away.z);
            double power = Math.max(0.3D, 1.4D * (1.0D - dist / 18.0D));
            e.push(flat.x * power, 0.25D * power + 0.1D, flat.z * power);
            e.hurtMarked = true;
            if (e instanceof Player) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
            }
        }
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 36; i++) {
                double a = i * (Math.PI * 2.0D / 36.0D);
                double dx = Math.cos(a);
                double dz = Math.sin(a);
                server.sendParticles(ParticleTypes.CLOUD, this.getX() + dx * 2.5D, this.getY() + 0.3D,
                        this.getZ() + dz * 2.5D, 0, dx, 0.05D, dz, 0.9D);
            }
        }
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.biteTick > 0) {
            this.biteTick--;
        }
        if (this.roarTick > 0) {
            this.roarTick--;
            if (!this.level().isClientSide && this.roarTick == ROAR_TICKS - 18) {
                this.roarBlast();
            }
        }

        if (!this.level().isClientSide) {
            if (this.roarCooldown > 0) {
                this.roarCooldown--;
            }
            // plow through jungle canopy
            if (this.horizontalCollision && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                AABB box = this.getBoundingBox().inflate(0.4D);
                for (BlockPos p : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                        Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
                    BlockState state = this.level().getBlockState(p);
                    if (state.is(BlockTags.LEAVES)) {
                        this.level().destroyBlock(p, true, this);
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.biteTick = BITE_TICKS;
        } else if (id == 5) {
            this.roarTick = ROAR_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ------------------------------------------------------------------ boss bar

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(this.getTarget() != null);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    // ------------------------------------------------------------------ misc

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    // ------------------------------------------------------------------ sounds (vanilla sounds pitched way down)

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_STEP, 3.0F, 0.55F);
    }

    @Override
    public float getVoicePitch() {
        return 0.42F + this.random.nextFloat() * 0.1F;
    }

    @Override
    public float getSoundVolume() {
        return 3.0F;
    }
}
