package com.ombremoon.spellbound.client.gui.guide.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.client.gui.guide.elements.extras.ElementPosition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GuideSpellBorderElement(ResourceLocation spell, int colour, ElementPosition position, ResourceLocation path) implements IPageElement {
    public static final MapCodec<GuideSpellBorderElement> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(GuideSpellBorderElement::spell),
            Codec.INT.fieldOf("colour").forGetter(GuideSpellBorderElement::colour),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideSpellBorderElement::position),
            ResourceLocation.CODEC.fieldOf("pathTexture").forGetter(GuideSpellBorderElement::path)
    ).apply(inst, GuideSpellBorderElement::new));

    @Override
    public @NotNull MapCodec<? extends IPageElement> codec() {
        return CODEC;
    }
}
