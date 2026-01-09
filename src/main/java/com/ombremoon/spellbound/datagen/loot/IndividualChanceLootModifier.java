package com.ombremoon.spellbound.datagen.loot;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;
import java.util.function.Predicate;

public abstract class IndividualChanceLootModifier implements IGlobalLootModifier {
    protected final LootItemCondition[] conditions;
    protected final LootItemRandomChanceCondition chanceCon;
    private final Predicate<LootContext> combinedConditions;

    protected static <T extends IndividualChanceLootModifier> Products.P2<RecordCodecBuilder.Mu<T>, LootItemCondition[], LootItemRandomChanceCondition> codecStart(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter((lm) -> lm.conditions),
                LootItemRandomChanceCondition.CODEC.fieldOf("chance").forGetter(lm -> lm.chanceCon));
    }

    protected IndividualChanceLootModifier(LootItemCondition[] conditionsIn, LootItemRandomChanceCondition chanceCondition) {
        this.conditions = conditionsIn;
        this.chanceCon = chanceCondition;
        this.combinedConditions = AllOfCondition.allOf(List.of(conditionsIn));
    }

    public final ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return this.combinedConditions.test(context) ? this.doApply(generatedLoot, chanceCon, context) : generatedLoot;
    }

    protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> var1, LootItemRandomChanceCondition chanceCon, LootContext var2);
}
