package com.ombremoon.spellbound.client.renderer.entity.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.spellbound.client.renderer.types.GenericLivingEntityRenderer;
import com.ombremoon.spellbound.common.world.entity.living.wildmushroom.GiantMushroom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class GiantMushroomRenderer extends GenericLivingEntityRenderer<GiantMushroom> {
    public GiantMushroomRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, GiantMushroom animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        poseStack.scale(3.6F, 3.6F, 3.6F);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
