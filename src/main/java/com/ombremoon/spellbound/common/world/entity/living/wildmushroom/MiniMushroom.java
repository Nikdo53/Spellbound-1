package com.ombremoon.spellbound.common.world.entity.living.wildmushroom;

import com.mojang.datafixers.util.Pair;
import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.client.particle.EffectBuilder;
import com.ombremoon.spellbound.common.world.entity.SBLivingEntity;
import com.ombremoon.spellbound.common.world.entity.behavior.attack.MushroomExplosion;
import com.ombremoon.spellbound.common.world.entity.behavior.move.FollowSummoner;
import com.ombremoon.spellbound.common.world.entity.behavior.sensor.HurtOwnerSensor;
import com.ombremoon.spellbound.common.world.entity.behavior.sensor.OwnerAttackSenor;
import com.ombremoon.spellbound.common.world.entity.behavior.target.ExtendedTargetOrRetaliate;
import com.ombremoon.spellbound.common.world.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.common.init.SBDamageTypes;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.WalkOrRunToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.List;

public class MiniMushroom extends LivingMushroom {
    private static final EntityDataAccessor<Boolean> ENLARGED = SynchedEntityData.defineId(MiniMushroom.class, EntityDataSerializers.BOOLEAN);
    private boolean isDazed;

    public MiniMushroom(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMiniMushroomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.JUMP_STRENGTH, 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ENLARGED, false);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else {
            if (player != this.getOwner()) {
                if (!this.isDazed) {
                    return InteractionResult.PASS;
                }

                this.enlarge();
                this.setOwner(player);
                itemStack.consume(1, player);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                var skills = SpellUtil.getSkills(player);
                if (!skills.hasSkill(SBSkills.PROLIFERATION)) {
                    return InteractionResult.PASS;
                } else {
                    BlockPos blockPos = this.blockPosition();
                    if (!this.level().isClientSide) {
                        if (RandomUtil.percentChance(0.15F)) {
                            WildMushroomSpell spell = this.getSpell();
                            GiantMushroom giantMushroom = spell.summonEntity(spell.getContext(), SBEntities.GIANT_MUSHROOM.get(), this.position());
                            spell.setGiantMushroom(giantMushroom.getId());
                            this.discard();
                        }
                    }

                    ParticleUtils.spawnParticles(this.level(), blockPos, 15, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
                    itemStack.consume(1, player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }
        }
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level().isClientSide) {
            Vec3 position = this.position();
            this.addFX(
                    EffectBuilder.Block.of(CommonClass.customLocation("mushroom_explosion"), BlockPos.containing(position.x, position.y, position.z))
                            .setOffset(0, 0.08, 0)
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.isExploding()) {
            if (this.explosionTimer-- == 1) {
                Vec3 position = this.position();
                this.addFX(
                        EffectBuilder.Block.of(CommonClass.customLocation("mushroom_explosion"), BlockPos.containing(position.x, position.y, position.z))
                                .setOffset(0, 0.08, 0)
                );
            }
        }
    }

    @Override
    public List<? extends ExtendedSensor<? extends SBLivingEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<MiniMushroom>()
                        .setRadius(10)
                        .setPredicate((player, mushroom) -> !mushroom.isAlliedTo(player)),
                new HurtOwnerSensor<MiniMushroom>()
                        .setPredicate((source, miniMushroom) -> !(source.getDirectEntity() != null && miniMushroom.isAlliedTo(source.getDirectEntity()))),
                new OwnerAttackSenor<>(),
                new HurtBySensor<MiniMushroom>()
                        .setPredicate((source, miniMushroom) -> !(source.getDirectEntity() != null && miniMushroom.isAlliedTo(source.getDirectEntity())))
        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new FollowSummoner<>(),
                new WalkOrRunToWalkTarget<MiniMushroom>()
                        .stopIf(MiniMushroom::isExploding)
        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new ExtendedTargetOrRetaliate<>()
                                .noTargetSwapping()
                                .useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER),
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<MiniMushroom>(),
                        new Idle<MiniMushroom>()
                                .runFor(mushroom -> mushroom.getRandom().nextInt(30, 60))
                )
        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<MiniMushroom>(),
                new SetWalkTargetToAttackTarget<>()
                        .speedMod((miniMushroom, livingEntity) -> 1.5F),
                new MushroomExplosion<MiniMushroom>(30)
                        .explosionRadius(miniMushroom -> {
                            if (miniMushroom.getOwner() instanceof LivingEntity livingEntity) {
                                var skills = SpellUtil.getSkills(livingEntity);
                                if (skills.hasSkill(SBSkills.VILE_INFLUENCE)) {
                                    return 5.0F;
                                }
                            }

                            return 3.0F;
                        })
                        .shouldDiscard()
        );
    }

    public boolean isEnlarged() {
        return this.entityData.get(ENLARGED);
    }

    public void enlarge() {
        this.entityData.set(ENLARGED, true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 7, this::miniMushroomController));
        controllers.add(new AnimationController<>(this, EXPLOSION, 0, state -> PlayState.STOP)
                .triggerableAnim("explode", RawAnimation.begin().thenPlay("explode")));
    }

    protected <T extends GeoAnimatable> PlayState miniMushroomController(AnimationState<T> data) {
        if (data.isMoving()) {
            if (this.getSharedFlag(3)) {
                data.setAnimation(RawAnimation.begin().thenPlay("run"));
            } else {
                data.setAnimation(RawAnimation.begin().thenPlay("walk"));
            }
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }

    public static class Explode extends DelayedBehaviour<MiniMushroom> {
        private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.ATTACK_TARGET);

        @Nullable
        protected LivingEntity target = null;

        public Explode() {
            super(30);
        }

        @Override
        protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
            return MEMORY_REQUIREMENTS;
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, MiniMushroom entity) {
            this.target = BrainUtils.getTargetOfEntity(entity);

            return entity.getSensing().hasLineOfSight(this.target) && entity.distanceToSqr(this.target) <= 4;
        }

        @Override
        protected void start(MiniMushroom entity) {
            entity.triggerAnim("explode", "explode");
            entity.setExploding(true);
        }

        @Override
        protected void stop(MiniMushroom entity) {
            entity.setExploding(false);
        }

        @Override
        protected void doDelayedAction(MiniMushroom entity) {
            boolean flag = entity.spell != null;
            int radius = 3;
            if (entity.getOwner() instanceof LivingEntity livingEntity) {
                var skills = SpellUtil.getSkills(livingEntity);
                radius = skills.hasSkill(SBSkills.VILE_INFLUENCE) ? 5 : 3;
            }

            for (LivingEntity target : EntityRetrievalUtil.getEntities(entity, radius, radius, radius, LivingEntity.class, livingEntity -> !entity.isAlliedTo(livingEntity))) {
                if (flag) {
                    entity.spell.hurt(target);
                } else {
                    target.hurt(BoxUtil.damageSource(entity.level(), SBDamageTypes.SB_GENERIC, entity), 4.0F);
                }

                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60));
            }

            entity.discard();
            if (entity.spell != null)
                entity.spell.endSpell();
        }
    }
}
