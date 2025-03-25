/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.data.MiraAttributes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRendererManager.class)
public abstract class EntityRendererManagerMixin {

    private static double $cm$sizeStatic = 1.0;
    private double $cm$size$width = 1.0;
    private double $cm$size$height = 1.0;

    // shadow radius
    @ModifyVariable(method = "renderShadow", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static float renderShadowScaleRadius(float radius, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                                                 Entity entity, float strength, float partialTicks) {
        $cm$sizeStatic = ((IRenderSized) entity).getSizeCM(partialTicks);
        return (float) (radius * Math.min($cm$sizeStatic, Config.maxShadowSize.get()));
    }

    // shadow strength
    @ModifyConstant(method = "renderBlockShadow", constant = @Constant(doubleValue = 2.0))
    private static double renderShadowScaleRadius(double constant) {
        return constant * $cm$sizeStatic;
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    Vector3d renderOffset(Vector3d offset, Entity entity, double x, double y, double z, float rotationYaw,
                          float partialTicks, MatrixStack matrixStack) {
        $cm$size$width = ((IRenderSized) entity).getSizeCM(
                MiraAttributes.WIDTH.getSecond().get(),
                partialTicks
        );
        $cm$size$height = ((IRenderSized) entity).getSizeCM(
                MiraAttributes.HEIGHT.getSecond().get(),
                partialTicks
        );
        return offset.multiply($cm$size$width, $cm$size$height, $cm$size$width);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V", ordinal = 0, shift = Shift.AFTER))
    void renderScale(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks,
                     MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci) {
        matrixStack.pushPose();
        matrixStack.scale((float) $cm$size$width, (float) $cm$size$height, (float) $cm$size$width);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V", ordinal = 1, shift = Shift.BEFORE))
    void renderDeScale(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks,
                       MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci) {
//        float invSize = (float) (1.0 / $cm$size);
//        matrixStack.scale(invSize, invSize, invSize);
        matrixStack.popPose();
    }

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE"), ordinal = 0)
    float renderFlame(float f) {
        // TODO: split xyz
        return (float) (f / $cm$size$height);
    }

    @ModifyConstant(method = "renderHitbox", constant = @Constant(floatValue = 0.01f))
    float renderHitboxEyeBoxFix(float constant) {
        // TODO: choose a better scale type..?
        return (float) (constant * $cm$size$width);
    }

    @ModifyConstant(method = "renderHitbox", constant = @Constant(doubleValue = 2.0))
    double renderHitboxEyeVecFix(double constant) {
        // TODO: choose a better scale type..?
        return constant * $cm$size$height;
    }

    // unscale distance from eyes to feet used in shadow strength calculation
    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 6)
    double renderShadowDistance(double dist) {
        // TODO: currently assumption based
        return dist / $cm$size$height / $cm$size$height;
    }
}
