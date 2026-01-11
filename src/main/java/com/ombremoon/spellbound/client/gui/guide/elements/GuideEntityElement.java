package com.ombremoon.spellbound.client.gui.guide.elements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.client.gui.guide.elements.extras.ElementPosition;
import com.ombremoon.spellbound.client.gui.guide.elements.extras.EntityRendererExtras;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record GuideEntityElement(List<ResourceLocation> entityLoc, EntityRendererExtras extras, ElementPosition position) implements IPageElement {
    public static final MapCodec<GuideEntityElement> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("entities").forGetter(GuideEntityElement::entityLoc),
            EntityRendererExtras.CODEC.optionalFieldOf("extras", EntityRendererExtras.getDefault()).forGetter(GuideEntityElement::extras),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideEntityElement::position)
    ).apply(inst, GuideEntityElement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuideEntityElement> STREAM_CODEC = StreamCodec.of(
            (buf, element) -> {
                for (var entity : element.entityLoc()) {
                    ResourceLocation.STREAM_CODEC.encode(buf, entity);
                }

            },
            (buf) -> {
                return null;
            }
    );

    @Override
    public @NotNull MapCodec<? extends IPageElement> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, ? extends IPageElement> streamCodec() {
        return STREAM_CODEC;
    }
}
