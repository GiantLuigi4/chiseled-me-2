package dev.necauqua.mods.mira.size;

import dev.necauqua.mods.mira.data.MiraAttributes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;

public class MiraModHandler {
    public static void on(EntityAttributeModificationEvent e) {
        for (EntityType<? extends LivingEntity> type : e.getTypes()) {
            for (RegistryObject<Attribute> attributeRegistryObject : MiraAttributes.ALL) {
                e.add(type, attributeRegistryObject.get(), 1.0);
            }
        }
    }
}
