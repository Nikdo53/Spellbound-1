package com.ombremoon.spellbound.common.world.entity.projectile;

import com.ombremoon.spellbound.common.world.entity.living.wildmushroom.GiantMushroom;
import com.ombremoon.spellbound.common.world.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.init.SBEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tslat.smartbrainlib.util.RandomUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MushroomProjectile extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean primaryProjectile;

    public MushroomProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public MushroomProjectile(Level level, GiantMushroom thrower) {
        this(SBEntities.MUSHROOM_PROJECTILE.get(), level);
        this.setOwner(thrower);
        this.setPos(
                thrower.getX() - (double)(thrower.getBbWidth() + 1.0F) * 0.5 * (double) Mth.sin(thrower.yBodyRot * (float) (Math.PI / 180.0)),
                thrower.getEyeY() - 0.1F,
                thrower.getZ() + (double)(thrower.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(thrower.yBodyRot * (float) (Math.PI / 180.0))
        );
    }

    protected double getDefaultGravity() {
        return 0.12;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitresult))
            this.hitTargetOrDeflectSelf(hitresult);
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        if (this.isInWaterOrBubble()) {
            this.discard();
        } else {
            this.setDeltaMovement(vec3.scale(0.99F));
            this.applyGravity();
            this.setPos(d0, d1, d2);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Level level = this.level();
        if (!level.isClientSide) {
            Entity entity = result.getEntity();
            if (!(entity instanceof MushroomProjectile || entity instanceof WildMushroom)) {
                if (this.getOwner() instanceof GiantMushroom mushroom) {
                    DamageSource damagesource = mushroom.spellDamageSource(level);
                    if (entity instanceof LivingEntity livingEntity && mushroom.hurtTarget(livingEntity, damagesource, 8.0F * mushroom.getPhase())) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, mushroom.getPhase()));
                    }
                }

                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Level level = this.level();
        if (!level.isClientSide) {
            Entity entity = this.getOwner();
            if (entity instanceof GiantMushroom mushroom && result.getDirection() == Direction.UP) {
                if (mushroom.getPhase() >= 2 && this.isPrimaryProjectile()) {
                    int numMushrooms = RandomUtil.randomNumberBetween(4, 6);
                    float x = (float) Math.toDegrees(-2 * Mth.PI / 3);
                    for (int i = 0; i < numMushrooms; i++) {
                        MushroomProjectile mushroomProjectile = new MushroomProjectile(level, mushroom);
                        mushroomProjectile.setPos(result.getLocation());
                        float y = (float) Math.toDegrees(i * Mth.TWO_PI / numMushrooms);
                        float f = -Mth.sin(y * 0.017453292F) * Mth.cos(x * 0.017453292F);
                        float f1 = -Mth.sin(x * 0.017453292F);
                        float f2 = Mth.cos(y * 0.017453292F) * Mth.cos(x * 0.017453292F);
                        mushroomProjectile.shoot(f, f1, f2, (float) RandomUtil.randomValueBetween(0.5, 0.75), 1.0F);
                        level.addFreshEntity(mushroomProjectile);
                    }
                } else {
                    WildMushroom wildMushroom = new WildMushroom(level, mushroom);
                    wildMushroom.setPos(result.getLocation());
                    level.addFreshEntity(wildMushroom);
                }
            }

            this.discard();
        }
    }

    public boolean isPrimaryProjectile() {
        return this.primaryProjectile;
    }

    public void setPrimaryProjectile(boolean primaryProjectile) {
        this.primaryProjectile = primaryProjectile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
