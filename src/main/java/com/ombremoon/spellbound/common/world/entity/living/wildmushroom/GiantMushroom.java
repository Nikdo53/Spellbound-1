package com.ombremoon.spellbound.common.world.entity.living.wildmushroom;

import com.mojang.datafixers.util.Pair;
import com.ombremoon.spellbound.client.particle.EffectBuilder;
import com.ombremoon.spellbound.common.world.entity.SBLivingEntity;
import com.ombremoon.spellbound.common.world.entity.SmartSpellEntity;
import com.ombremoon.spellbound.common.world.entity.behavior.attack.MushroomExplosion;
import com.ombremoon.spellbound.common.world.entity.behavior.sensor.HurtOwnerSensor;
import com.ombremoon.spellbound.common.world.entity.behavior.sensor.OwnerAttackSenor;
import com.ombremoon.spellbound.common.world.entity.projectile.MushroomProjectile;
import com.ombremoon.spellbound.common.world.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.main.CommonClass;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GiantMushroom extends LivingMushroom implements RangedAttackMob {
    protected static final EntityDataAccessor<BlockPos> START_POS = SynchedEntityData.defineId(GiantMushroom.class, EntityDataSerializers.BLOCK_POS);
    private boolean bouncing;
    private boolean aboutToBounce;

    public GiantMushroom(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createGiantMushroomAttributes() {
        return SBLivingEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.JUMP_STRENGTH, 3.7)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(START_POS, BlockPos.ZERO);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public int getHeadRotSpeed() {
        return 3;
    }

    public void setStartPos(BlockPos startPos) {
        this.entityData.set(START_POS, startPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(START_POS);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (!level.isClientSide) {
            if (this.bouncing && this.onGround()) {
                List<LivingEntity> collidingEntities = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), livingEntity -> !livingEntity.is(this) && !this.isAlliedTo(livingEntity));
                List<LivingEntity> surroundingEntities = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3, 0, 3), livingEntity -> !livingEntity.is(this) && !this.isAlliedTo(livingEntity))
                        .stream()
                        .filter(livingEntity -> !collidingEntities.contains(livingEntity) && livingEntity.onGround())
                        .toList();
                collidingEntities.forEach(livingEntity -> this.hurtTarget(livingEntity, 6.0F));
                surroundingEntities.forEach(livingEntity -> this.hurtTarget(livingEntity, 4.0F));
                this.bouncing = false;
            }
        } else {
            if (this.isExploding()) {
                if (this.explosionTimer-- == 1) {
                    Vec3 position = this.position();
                    this.addFX(
                            EffectBuilder.Block.of(CommonClass.customLocation("mushroom_explosion"), BlockPos.containing(position.x, position.y, position.z))
                                    .setScale(3, 3, 3)
                    );
                }
            }
        }
        this.setYBodyRot(this.getYHeadRot());
    }

    public boolean isBouncing() {
        return this.bouncing || this.aboutToBounce;
    }

    @Override
    public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @org.jetbrains.annotations.Nullable SpawnGroupData spawnGroupData) {
        this.setStartPos(this.blockPosition());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("StartPos", NbtUtils.writeBlockPos(this.getStartPos()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        NbtUtils.readBlockPos(compound, "StartPos").ifPresent(this::setStartPos);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 7, this::giantMushroomController));
        controllers.add(new AnimationController<>(this, EXPLOSION, 0, state -> PlayState.STOP)
                .triggerableAnim("explode", RawAnimation.begin().thenPlay("explode")));
    }

    protected <T extends GeoAnimatable> PlayState giantMushroomController(AnimationState<T> data) {
        data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public List<? extends ExtendedSensor<? extends SBLivingEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<GiantMushroom>()
                        .setPredicate((player, giantMushroom) -> !giantMushroom.isAlliedTo(player)),
                new HurtOwnerSensor<GiantMushroom>()
                        .setPredicate((source, giantMushroom) -> !(source.getDirectEntity() != null && giantMushroom.isAlliedTo(source.getDirectEntity()))),
                new OwnerAttackSenor<GiantMushroom>(),
                new HurtBySensor<GiantMushroom>()
                        .setPredicate((source, giantMushroom) -> !(source.getDirectEntity() != null && giantMushroom.isAlliedTo(source.getDirectEntity())))
        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<GiantMushroom>(),
                new BounceToTarget()
                        .startCondition(giantMushroom -> !giantMushroom.isAttacking() && !giantMushroom.wasSummoned() && giantMushroom.getPhase() != 2)
        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new TargetOrRetaliate<>()
                        .useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)

        );
    }

    @Override
    public BrainActivityGroup<? extends SBLivingEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<GiantMushroom>() {
                    @Override
                    protected boolean isTiredOfPathing(GiantMushroom entity) {
                        return false;
                    }
                },
                new OneRandomBehaviour<>(
                        newMushroomAttackBehaviour(
                                new AnimatableRangedAttack<GiantMushroom>(20)
                                        .attackRadius(32),
                                20,
                                60),
                        newMushroomAttackBehaviour(
                                new MushroomExplosion<GiantMushroom>(70)
                                        .explosionRadius(livingMushroom -> 5.0F),
                               30,
                                100)
                )
        );
    }

    private  ExtendedBehaviour<GiantMushroom> newMushroomAttackBehaviour(ExtendedBehaviour<GiantMushroom> behaviour, int delayTicks, int cooldownTicks) {
        return behaviour
                .stopIf(GiantMushroom::isBouncing)
                .startCondition(giantMushroom -> !giantMushroom.isBouncing())
                .whenStarting(giantMushroom -> giantMushroom.startAttack(delayTicks))
                .cooldownFor(giantMushroom -> cooldownTicks);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        MushroomProjectile mushroom = new MushroomProjectile(this.level(), this);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333) - mushroom.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.20000000298023224;
        mushroom.shoot(d0, d1 + d3, d2, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }

        this.level().addFreshEntity(mushroom);
    }

    public static class BounceToTarget extends ExtendedBehaviour<GiantMushroom> {
        private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.ATTACK_TARGET);

        @Nullable
        protected LivingEntity target = null;
        @Nullable
        protected Vec3 chosenJump;
        protected long prepareJumpStart;
        protected Optional<Vec3> initialPosition;
        private Function<GiantMushroom, BlockPos> targetBlock = giantMushroom -> null;

        public BounceToTarget() {
            runFor(entity -> 200);
            cooldownFor(entity -> entity.random.nextIntBetweenInclusive(100, 200));
        }

        @Override
        protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
            return MEMORY_REQUIREMENTS;
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, GiantMushroom entity) {
            boolean flag = entity.onGround()
                    && !entity.isInWater()
                    && !entity.isInLava()
                    && !level.getBlockState(entity.blockPosition()).is(Blocks.HONEY_BLOCK);
            this.target = BrainUtils.getTargetOfEntity(entity);

            return flag && this.target != null && entity.getSensing().hasLineOfSight(this.target);
        }

        @Override
        protected boolean canStillUse(ServerLevel level, GiantMushroom entity, long gameTime) {
            return this.initialPosition.isPresent()
                    && this.initialPosition.get().equals(entity.position())
                    && !entity.isInWaterOrBubble()
                    && this.chosenJump != null;
        }

        @Override
        protected void start(ServerLevel level, GiantMushroom entity, long gameTime) {
            super.start(level, entity, gameTime);
            this.chosenJump = null;
            this.initialPosition = Optional.of(entity.position());
            this.pickCandidate(entity, gameTime);
            entity.aboutToBounce = true;
        }

        @Override
        protected void tick(ServerLevel level, GiantMushroom entity, long gameTime) {
            super.tick(level, entity, gameTime);
            if (this.chosenJump != null) {
                if (gameTime - this.prepareJumpStart >= 40L) {
                    entity.setDiscardFriction(true);
                    entity.setDeltaMovement(this.chosenJump);
                    entity.bouncing = true;
                    entity.aboutToBounce = false;
                } else {
                    this.pickCandidate(entity, gameTime);
                }
            }
        }

        @Override
        protected void stop(GiantMushroom entity) {
            super.stop(entity);
            entity.setDiscardFriction(false);
            BehaviorUtils.lookAtEntity(entity, this.target);
            entity.aboutToBounce = false;
        }

        protected void pickCandidate(GiantMushroom entity, long prepareJumpStart) {
            if (this.target != null) {
                if (this.chosenJump == null) {
                    BlockPos blockPos = this.target.blockPosition();
                    BlockPos blockPos1 = this.targetBlock.apply(entity);
                    if (blockPos1 != null)
                        blockPos = blockPos1;

                    if (this.isAcceptableLandingPosition(entity, blockPos)) {
                        Vec3 vec31 = this.calculateJumpVector(entity, blockPos, (float) entity.getAttributeValue(Attributes.JUMP_STRENGTH));
                        if (vec31 != null) {
                            BehaviorUtils.lookAtEntity(entity, this.target);
                            this.chosenJump = vec31;
                        }
                    }
                } else {
                    this.prepareJumpStart = prepareJumpStart;
                }
            }
        }

        private Vec3 calculateJumpVector(Entity entity, BlockPos target, float jumpPower) {
            double gravity = 0.28F;
            Vec3 startPos = entity.position();
            Vec3 endPos = new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            double dx = endPos.x - startPos.x;
            double dy = endPos.y - startPos.y;
            double dz = endPos.z - startPos.z;
            double velocityY = Math.sqrt(2 * gravity * jumpPower);
            double d0 = 0.5 * gravity;
            double time = (velocityY + Math.sqrt(-velocityY * -velocityY + 2 * d0 * dy)) / (2 * d0);
            if (time <= 0) {
                return Vec3.ZERO;
            }

            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            double horizontalVelocity = horizontalDistance / time;
            double velocityX = (dx / horizontalDistance) * horizontalVelocity;
            double velocityZ = (dz / horizontalDistance) * horizontalVelocity;
            return new Vec3(velocityX, velocityY, velocityZ);
        }

        private boolean isAcceptableLandingPosition(GiantMushroom entity, BlockPos pos) {
            BlockPos blockpos = entity.blockPosition();
            int i = blockpos.getX();
            int j = blockpos.getZ();
            return (i != pos.getX() || j != pos.getZ()) && this.defaultAcceptableLandingSpot(entity, pos);
        }

        private  boolean defaultAcceptableLandingSpot(GiantMushroom mob, BlockPos pos) {
            Level level = mob.level();
            BlockPos blockpos = pos.below();
            return level.getBlockState(blockpos).isSolidRender(level, blockpos) && mob.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(mob, pos)) == 0.0F;
        }
    }
}
