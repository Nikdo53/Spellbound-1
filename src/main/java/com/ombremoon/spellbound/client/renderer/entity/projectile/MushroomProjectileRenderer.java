package com.ombremoon.spellbound.client.renderer.entity.projectile;

import com.ombremoon.spellbound.client.model.entity.projectile.MushroomProjectileModel;
import com.ombremoon.spellbound.common.world.entity.projectile.MushroomProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MushroomProjectileRenderer extends GeoEntityRenderer<MushroomProjectile> {
    public MushroomProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MushroomProjectileModel());
    }
}
