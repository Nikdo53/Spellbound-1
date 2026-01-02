package com.ombremoon.spellbound.client.gui.guide;

import com.ombremoon.spellbound.client.gui.guide.elements.GuideSpellBorderElement;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class GuideSpellBorderRenderer implements IPageElementRenderer<GuideSpellBorderElement> {
    @Override
    public void render(GuideSpellBorderElement element, GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick, int tickCount) {
        SpellType<?> spell = SBSpells.REGISTRY.get(element.spell());
        SpellPath subPath = spell.getSubPath();
        SpellMastery mastery = spell.createSpellWithData(Minecraft.getInstance().player).getSpellMastery();
        Font font = Minecraft.getInstance().font;
        topPos += 15;

        graphics.blit(
                element.path(),
                leftPos - 7 + element.position().xOffset(),
                topPos + element.position().yOffset(),
                0, 0,
                16, 16, 16, 16
        );

        graphics.hLine(leftPos + 8, leftPos + 150, topPos + element.position().yOffset() + 8, element.colour());

        topPos -= 5;

        String masteryText = I18n.get("guide.element.spell_border.mastery", mastery.toString());
        graphics.drawString(Minecraft.getInstance().font,
                masteryText,
                ((153-font.width(masteryText))/2) + leftPos + element.position().xOffset(),
                topPos + 168 + element.position().yOffset(), element.colour(), false);

        graphics.blit(
                element.path(),
                leftPos + 135 + element.position().xOffset(),
                topPos + 170,
                0, 0,
                16, 16, 16, 16
        );

        graphics.hLine(leftPos + 8, leftPos + 135, topPos+178, element.colour());

        if (subPath == null) return;
        String elementText = I18n.get("guide.element.spell_border.element");

        int textStart = (153-font.width(elementText + subPath.name()))/2;
        graphics.drawString(font,
                elementText,
                textStart + leftPos + element.position().xOffset(),
                topPos + 182 + element.position().yOffset(), element.colour(), false);
        graphics.drawString(font,
                subPath.name(),
                textStart + font.width(elementText) + leftPos + element.position().xOffset(),
                topPos + 182 + element.position().yOffset(), subPath.getColor(), false);
    }
}
