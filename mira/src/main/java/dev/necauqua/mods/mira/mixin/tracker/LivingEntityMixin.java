package dev.necauqua.mods.mira.mixin.tracker;

import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.data.MiraAttributes;
import dev.necauqua.mods.mira.extras.IChangeTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract AttributeModifierManager getAttributes();

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(EntityType p_i48577_1_, World p_i48577_2_, CallbackInfo ci) {
        for (RegistryObject<Attribute> attributeRegistryObject : MiraAttributes.ALL) {
            ModifiableAttributeInstance INSTANCE = getAttributes().getInstance(attributeRegistryObject.get());

            ((IChangeTracker) INSTANCE).setEntity((LivingEntity) (Object) this);
            ((IChangeTracker) INSTANCE).setTracker((entity, inst) -> {
                ((IRenderSized) entity).updateSize();
            });
        }
    }
}
