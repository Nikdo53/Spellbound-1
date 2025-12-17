package com.ombremoon.spellbound.client.gui.guide_renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GuideUtil {

    public static Ingredient buildIngredient(List<Ingredient> ingredients) {
        List<ItemLike> list = new ArrayList<>();
        for (Ingredient ing : ingredients) {
            for (ItemStack stack : ing.getItems()) {
                list.add(stack.getItem());
            }
        }

        return Ingredient.of(list.toArray(new ItemLike[]{}));
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        if (!stack.isEmpty()) {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((x + (19*scale)), (y + (17*scale)), (float)(150));

            try {
                float size = 16.0F * 1.2F * scale;
                pose.scale(size, -size, size);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
                graphics.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            pose.popPose();
        }
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, float x, float y, float scale, Entity entity, boolean isVisible) {
        renderEntityInInventory(guiGraphics, x, y, scale, entity, isVisible, new Quaternionf(), false, 0, 0);
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, float x, float y, float scale, Entity entity, boolean isVisible, Quaternionf mul,  boolean followsMouse, int mouseX, int mouseY) {
        renderEntityInInventory(guiGraphics,x, y, scale, entity, isVisible, mul, followsMouse, mouseX, mouseY, false);
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, float x, float y, float scale, Entity entity, boolean isVisible, Quaternionf mul, boolean followsMouse, int mouseX, int mouseY, boolean rotates) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(x, y, 50);
        poseStack.mulPose(mul);

        if (followsMouse) {
            float f = (float)(x + x + entity.getBbWidth()) / 2.0F;
            float f1 = (float)(y +  y + entity.getBbHeight()) / 2.0F;
            float f2 = (float)Math.atan((double)((f - mouseX) / 40.0F));
            float f3 = (float)Math.atan((double)((f1 - mouseY) / 40.0F));

            Quaternionf quaternionf = new Quaternionf()
                    .rotateZ((float)Math.PI)         //flip
                    .rotateX(-f3 * 20.0F * ((float)Math.PI / 180F))  //vertical
                    .rotateY(f2);                //horizontal

            poseStack.mulPose(quaternionf);
        } else if (rotates) {
            poseStack.mulPose(new Quaternionf()
                    .rotateY((float) (Math.toRadians(entity.tickCount) % 360))
                    .rotateZ((float) Math.PI));
        } else {
            poseStack.mulPose(new Quaternionf()
                    .rotateZ((float) Math.PI));
        }

        poseStack.translate(-240, -130, 50);
        poseStack.scale(scale, scale, scale);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        if (!isVisible) {
            RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        }

        if (entity instanceof ItemEntity itemEntity) {
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.renderStatic(itemEntity.getItem(),
                    ItemDisplayContext.GUI,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffers,
                    itemEntity.level(),
                    0);
        } else {
            dispatcher.setRenderShadow(false);
            dispatcher.render(entity,
                    0,
                    0,
                    0,
                    0,
                    0,
                    poseStack,
                    buffers,
                    isVisible ? 15728880 : 0);
            buffers.endBatch();
            dispatcher.setRenderShadow(true);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);


        poseStack.popPose();
    }
}
