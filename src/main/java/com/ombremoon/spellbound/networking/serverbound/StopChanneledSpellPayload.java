package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StopChanneledSpellPayload(SpellType<?> spellType) implements CustomPacketPayload {
    public static final Type<StopChanneledSpellPayload> TYPE = new Type<>(CommonClass.customLocation("stop_channeled_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopChanneledSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), StopChanneledSpellPayload::spellType,
            StopChanneledSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
