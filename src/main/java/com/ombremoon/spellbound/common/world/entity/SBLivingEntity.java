package com.ombremoon.spellbound.common.world.entity;

import com.ombremoon.spellbound.client.particle.EffectCache;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SBLivingEntity extends PathfinderMob implements SmartBrainOwner<SBLivingEntity>, GeoEntity, FXEmitter, Loggable {
    protected static final String CONTROLLER = "controller";
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SBLivingEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final EffectCache effectCache = new EffectCache();

    protected SBLivingEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Mob.createMobAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide)
            this.handleFXRemoval();
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return entity instanceof LivingEntity livingEntity && (this.isOwner(livingEntity) || SpellUtil.IS_ALLIED.test(this.getOwner(), livingEntity)) || super.isAlliedTo(entity);
    }

    @Nullable
    public Entity getOwner() {
        return this.level().getEntity(this.entityData.get(OWNER_ID));
    }

    public void setOwner(Entity entity) {
        this.setOwner(entity.getId());
    }

    public void setOwner(int id) {
        this.entityData.set(OWNER_ID, id);
    }

    protected boolean isOwner(LivingEntity entity) {
        Entity owner = this.getOwner();
        return owner != null && owner.is(entity);
    }

    protected boolean hidesBossPhases() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        Entity entity = this.getOwner();
        if (entity != null)
            compound.putInt("SpellboundOwner", this.getOwner().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setOwner(compound.getInt("SpellboundOwner"));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SmoothGroundNavigation(this, this.level());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public void onClientRemoval() {
        this.handleFXRemoval();
    }

    public EntityType<?> entityType() {
        return this.getType();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public EffectCache getFXCache() {
        return this.effectCache;
    }
}
