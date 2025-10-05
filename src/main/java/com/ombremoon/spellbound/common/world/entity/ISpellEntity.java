package com.ombremoon.spellbound.common.world.entity;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.init.SBDamageTypes;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public interface ISpellEntity<T extends AbstractSpell> extends GeoEntity, FXEmitter {

    default Entity getEntity() {
        return (Entity) this;
    }

    EntityType<?> entityType();

    void setOwner(@NotNull Entity entity);

    @Nullable
    Entity getOwner();

    void setSpell(@NotNull AbstractSpell spell);

    T getSpell();

    boolean isInitialized();

    default DamageSource spellDamageSource(Level level) {
        return BoxUtil.damageSource(level, SBDamageTypes.SB_GENERIC, this.getEntity());
    }
}
