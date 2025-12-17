package com.ombremoon.spellbound.client.gui.guide_renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideStaticItem;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class GuideStaticItemRenderer implements IPageElementRenderer<GuideStaticItem> {
    private RandomSource rand;

    public GuideStaticItemRenderer() {
        this.rand = Minecraft.getInstance().level.getRandom();
        rand.setSeed(Math.floorDiv(Minecraft.getInstance().player.tickCount, 10));
    }

    @Override
    public void render(GuideStaticItem element, GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick, int tickCount) {
        Registry<Item> registry = Minecraft.getInstance().level.registryAccess().registry(Registries.ITEM).get();

        ItemStack item = isVisible(element.extras().pageScrap()) ? new GuideRecipeRenderer.SBGhostItem(
                GuideUtil.buildIngredient(element.item()), 0, 0).getItem(tickCount)
                : registry.getRandom(rand).get().value().getDefaultInstance();

        graphics.blit(CommonClass.customLocation("textures/gui/books/crafting_grids/medium/" + element.tileName() + ".png"),
                leftPos + element.position().xOffset(),
                topPos + element.position().yOffset(),
                0,
                0,
                (int) (48 * element.extras().scale()),
                (int) (46 * element.extras().scale()),
                (int) (48 * element.extras().scale()),
                (int) (46 * element.extras().scale()));

        GuideUtil.renderItem(graphics, item, leftPos + element.position().xOffset(), topPos + element.position().yOffset(), 1.3f * element.extras().scale());
    }



}
