package com.ombremoon.spellbound.client.gui.guide_renderers;

import com.lowdragmc.lowdraglib2.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib2.client.renderer.IRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.lwjgl.opengl.WGLARBRenderTexture;
import software.bernie.geckolib.animatable.GeoEntity;

import javax.swing.*;

public class GuideEntityRendererRenderer implements IPageElementRenderer<GuideEntityRenderer> {

    @Override
    public void render(GuideEntityRenderer element, GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick, int tickCount) {
        Registry<EntityType<?>> registry = Minecraft.getInstance().level.registryAccess().registry(Registries.ENTITY_TYPE).get();
        EntityType<?> entityType = registry.get(element.entityLoc().get(Mth.floor(tickCount / 30.0F) % element.entityLoc().size()));

        if (entityType == null) {
            LOGGER.warn("Entity could not be found {}", element.entityLoc());
            return;
        }

        Entity entity = entityType.create(Minecraft.getInstance().level);

        if (entity instanceof GeoEntity){
            entity.tickCount = tickCount;
            entity.tick();
        }

        GuideUtil.renderEntityInInventory(graphics,
                leftPos + element.position().xOffset(),
                topPos + element.position().yOffset(),
                element.extras().scale(),
                entity, isVisible(element.extras().pageScrap()),
                new Quaternionf()
                        .rotateX((float) Math.toRadians(element.extras().xRot()))
                        .rotateY((float) Math.toRadians(element.extras().yRot()))
                        .rotateZ((float) Math.toRadians(element.extras().zRot())),
                element.extras().followMouse(), mouseX, mouseY);
    }


}
