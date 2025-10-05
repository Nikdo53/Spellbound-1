package com.ombremoon.spellbound.common.world.entity.behavior.move;

import com.ombremoon.spellbound.common.world.entity.SBLivingEntity;
import net.minecraft.world.entity.Entity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;

public class FollowSummoner<E extends SBLivingEntity> extends FollowEntity<E, Entity> {
    protected Entity owner = null;

    public FollowSummoner() {
        following(this::getOwner);
        teleportToTargetAfter(12);
    }

    protected Entity getOwner(E entity) {
        if (this.owner != null && (this.owner.isRemoved() || !this.owner.getUUID().equals(entity.getOwner().getUUID())))
            this.owner = null;

        if (this.owner == null)
            this.owner = entity.getOwner();

        return this.owner;
    }
}
