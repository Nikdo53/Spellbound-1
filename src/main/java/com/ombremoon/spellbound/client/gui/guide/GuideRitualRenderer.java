package com.ombremoon.spellbound.client.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.gui.guide.elements.special.GuideGhostItem;
import com.ombremoon.spellbound.client.gui.guide.elements.special.TransfigurationRitualElement;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualHelper;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import com.ombremoon.spellbound.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuideRitualRenderer implements IPageElementRenderer<TransfigurationRitualElement> {

    @Override
    public void render(TransfigurationRitualElement element, GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick, int tickCount) {
        ItemStack item = SBBlocks.TRANSFIGURATION_DISPLAY.get().asItem().getDefaultInstance();
        RenderUtil.renderItem(graphics, item, leftPos - 25, topPos + 20, 5.3F);

        TransfigurationRitual ritual = RitualHelper.getRitualFor(Minecraft.getInstance().level, element.ritual());
        ItemStack stack = new GuideGhostItem(buildIngredientFromValues(ritual.materials()), 30, 30).getItem(tickCount, 50.0F);
        RenderUtil.renderItem(graphics, stack, leftPos + 31, topPos + 22, 2.3F, 175);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(2.3F, 2.3F, 2.3F);
        poseStack.translate(0.0F, 0.0F, 176);
        graphics.drawString(Minecraft.getInstance().font,
                Component.literal(String.valueOf(stack.getCount())).withStyle(ChatFormatting.WHITE),
                leftPos + 8,
                topPos + 23,
                0,
                true);
        poseStack.popPose();
    }
}
