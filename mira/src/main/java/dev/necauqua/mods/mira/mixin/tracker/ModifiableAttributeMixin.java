package dev.necauqua.mods.mira.mixin.tracker;

import dev.necauqua.mods.mira.extras.IChangeTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ModifiableAttributeInstance.class)
public class ModifiableAttributeMixin implements IChangeTracker {
    LivingEntity entity;
    BiConsumer<LivingEntity, ModifiableAttributeInstance> tracker;

    @Override
    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setTracker(BiConsumer<LivingEntity, ModifiableAttributeInstance> tracker) {
        this.tracker = tracker;
    }

    @Inject(at = @At("TAIL"), method = "setDirty")
    public void postSetDirty(CallbackInfo ci) {
        if (entity != null && tracker != null)
            tracker.accept(entity, (ModifiableAttributeInstance) (Object) this);
    }
}
