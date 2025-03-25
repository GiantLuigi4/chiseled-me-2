/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.data.MiraAttributes;
import dev.necauqua.mods.mira.render.BlitHelper;
import dev.necauqua.mods.mira.size.MixinHelpers;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

import static dev.necauqua.mods.mira.size.MixinHelpers.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    private float $cm$bobbingHack = 1.0f;

    @Shadow
    private boolean renderHand;
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyConstant(method = "getProjectionMatrix", constant = @Constant(floatValue = 0.05f))
    float getProjectionMatrixNearPlane(float nearPlane, ActiveRenderInfo ari, float partialTick) {
        if (!renderingNearPatch) {
            return nearPlane;
        }
        Entity view = minecraft.getCameraEntity();
        assert view != null;
        double size = ((IRenderSized) view).getSizeCM(MiraAttributes.NEAR_PLANE.get(), partialTick);
        return size < 1.0f ? nearPlane * (float) size : nearPlane;
    }

    @ModifyArg(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Matrix4f;perspective(DFFF)Lnet/minecraft/util/math/vector/Matrix4f;"), index = 3)
    float getProjectionMatrixFarPlane(float farPlane) {
//        return renderingNearPatch ? (float) (0.06f / MixinHelpers.getViewerSize()) : farPlane;

        // overcompensate to account for GPU precision
        return renderingNearPatch ? 0.0515f : farPlane;
    }

    @Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z", shift = Shift.BEFORE))
    void renderDoublePass(float partialTick, long lastTimeNanos, MatrixStack matrixStack, CallbackInfo ci) {
        if (renderingNearPatch) {
            return;
        }

        FloatBuffer projFar = MemoryUtil.memAllocFloat(4 * 4);
        FloatBuffer projNear = MemoryUtil.memAllocFloat(4 * 4);

        GL30.glGetFloatv(GL30.GL_PROJECTION_MATRIX, projFar);

        Framebuffer vTarget = Minecraft.getInstance().getMainRenderTarget();
        MainWindow w = minecraft.getWindow();

        renderingNearPatch = true;
        renderHand = false;
        minecraft.getProfiler().push("miraNearPass");

        float invSize = (float) (1.0 / MixinHelpers.getViewerSize(MiraAttributes.NEAR_PLANE.get(), partialTick));
        MatrixStack scaled = new MatrixStack();
//        scaled.scale(invSize, invSize, invSize);

        // no idea how tf this works but seems to work.. (aka dumbass heuristic)

        if (invSize > 64) {
            // a linear function with two points:
            //   f(64) = 0.99975586
            //   f(4096) = 0.9
            viewOffsetZLayeringScale = (float) (-2.4741036706349E-5 * invSize + 1.0013392863492);
        }

        // todo:
        //  So I set the far plane above to 0.06 instead of 0.05 so that the little overlap hides any possible holes
        //  between the main near clip and the near patch we're hacking in here
        //  But that does not work too well with transparency
        //  *
        //  The only solution I can think of right now is to render the near patch to a framebuffer,
        //  and then slap it over the main one with the blending configured accordingly

        // main render target here must be our near patch one
        minecraft.getMainRenderTarget().bindWrite(true);
        BlitHelper._quickBlit(vTarget, w.getWidth(), w.getHeight(), true);
        GlStateManager._clear(GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        renderLevel(partialTick, lastTimeNanos, scaled);
        GL30.glGetFloatv(GL30.GL_PROJECTION_MATRIX, projNear);

        viewOffsetZLayeringScale = 0.99975586f;

        minecraft.getProfiler().pop();
        renderHand = true;
        renderingNearPatch = false;

        minecraft.getMainRenderTarget().bindWrite(true);
//        getNearPatchRenderTarget(w).blitToScreen(w.getWidth(), w.getHeight(), false);
        BlitHelper._blitToScreen(
                getNearPatchRenderTarget(w), vTarget,
                w.getWidth(), w.getHeight(),
                false, projNear, projFar
        );

        MemoryUtil.memFree(projFar);
        MemoryUtil.memFree(projNear);
    }

    @Shadow
    public abstract void renderLevel(float p_228378_1_, long p_228378_2_, MatrixStack p_228378_4_);

    // region scale bobbing screen translation

    @Inject(method = "getRenderDistance", at = @At("RETURN"), cancellable = true)
    public void getRenderDistance(CallbackInfoReturnable<Float> cir) {
        if (renderingNearPatch) {
            cir.setReturnValue((float) (cir.getReturnValueF() / MixinHelpers.getViewerSize()));
        }
    }

    @ModifyConstant(method = "pick", constant = {
            @Constant(doubleValue = 1.0),
            @Constant(doubleValue = 3.0),
            @Constant(doubleValue = 6.0),
    })
    double pick(double constant) {
        return constant * MixinHelpers.getViewerSize();
    }

    @ModifyVariable(method = "bobView", at = @At(value = "STORE"), ordinal = 3)
    float bobViewScale(float f2) {
        return f2 * $cm$bobbingHack;
    }

    @ModifyVariable(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V"), ordinal = 3)
    float bobViewUnScale(float f2) {
        return f2 / $cm$bobbingHack;
    }

    // endregion

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.BEFORE))
    void renderLevelBeforeBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = (float) MixinHelpers.getViewerSize(MiraAttributes.VIEW_BOB.get(), partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.AFTER))
    void renderLevelAfterBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = 1.0f;
    }
}
