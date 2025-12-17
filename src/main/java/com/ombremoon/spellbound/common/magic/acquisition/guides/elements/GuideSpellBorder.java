package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GuideSpellBorder(ResourceLocation spell, int topGap, ElementPosition position, ResourceLocation path) implements IPageElement {
    public static final MapCodec<GuideSpellBorder> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(GuideSpellBorder::spell),
            Codec.INT.fieldOf("topGap").forGetter(GuideSpellBorder::topGap),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideSpellBorder::position),
            ResourceLocation.CODEC.fieldOf("pathTexture").forGetter(GuideSpellBorder::path)
    ).apply(inst, GuideSpellBorder::new));

    @Override
    public @NotNull MapCodec<? extends IPageElement> codec() {
        return CODEC;
    }
}
