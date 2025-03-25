package dev.necauqua.mods.mira.data;

import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class InfluencedAttribute extends MiraAttribute {
    List<BiFunction<Double, LivingEntity, Double>> evaluations = new ArrayList<>();

    public InfluencedAttribute(String pDescriptionId, double pDefaultValue) {
        super(pDescriptionId, pDefaultValue);
    }

    public InfluencedAttribute addEvaluation(BiFunction<Double, LivingEntity, Double> evaluation) {
        this.evaluations.add(evaluation);
        return this;
    }

    public double getValue(LivingEntity entity) {
        double v = super.getValue(entity);
        for (BiFunction<Double, LivingEntity, Double> evaluation : evaluations) {
            v = evaluation.apply(v, entity);
        }
        return v;
    }
}
