package dev.necauqua.mods.mira.extras;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

import java.util.function.BiConsumer;

public interface IChangeTracker {
    void setEntity(LivingEntity entity);
    void setTracker(BiConsumer<LivingEntity, ModifiableAttributeInstance> tracker);
}
