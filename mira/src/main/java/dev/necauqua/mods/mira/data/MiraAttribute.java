package dev.necauqua.mods.mira.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

public class MiraAttribute extends Attribute {
    public MiraAttribute(String pDescriptionId, double pDefaultValue) {
        super(pDescriptionId, pDefaultValue);
    }

    protected double computeValue(ModifiableAttributeInstance INSTANCE) {
        if (INSTANCE == null) return 1.0;
        return INSTANCE.getValue();
    }

    public double getValue(LivingEntity entity) {
        ModifiableAttributeInstance INSTANCE = entity.getAttribute(this);
        return computeValue(INSTANCE);
    }
}
