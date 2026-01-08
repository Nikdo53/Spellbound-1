package com.ombremoon.spellbound.client.gui.guide.elements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.client.gui.guide.elements.extras.ElementPosition;
import com.ombremoon.spellbound.client.gui.guide.elements.extras.EquipmentExtras;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record GuideEquipmentElement(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack offHand, ItemStack mainHand, ElementPosition position, EquipmentExtras extras) implements IPageElement {
    public static final MapCodec<GuideEquipmentElement> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("helmet", ItemStack.EMPTY).forGetter(GuideEquipmentElement::helmet),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("chestplate", ItemStack.EMPTY).forGetter(GuideEquipmentElement::chestplate),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("leggings", ItemStack.EMPTY).forGetter(GuideEquipmentElement::leggings),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("boots", ItemStack.EMPTY).forGetter(GuideEquipmentElement::boots),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("off_hand", ItemStack.EMPTY).forGetter(GuideEquipmentElement::offHand),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("main_hand", ItemStack.EMPTY).forGetter(GuideEquipmentElement::mainHand),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideEquipmentElement::position),
            EquipmentExtras.CODEC.optionalFieldOf("extras", EquipmentExtras.getDefault()).forGetter(GuideEquipmentElement::extras)
    ).apply(inst, GuideEquipmentElement::new));

    @Override
    public @NotNull MapCodec<? extends IPageElement> codec() {
        return CODEC;
    }
}
