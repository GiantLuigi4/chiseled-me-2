/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.size;

import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.render.DepthOnlyTarget;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class MixinHelpers {

    public static boolean renderingNearPatch = false;
    public static Framebuffer nearPatchRenderTarget = null;
//    public static DepthOnlyTarget depthCpy = null;
    public static Framebuffer depthCpy = null;

    public static float viewOffsetZLayeringScale = 0.99975586f;

    private MixinHelpers() {
    }

    public static Framebuffer getNearPatchRenderTarget(MainWindow w) {
        if (nearPatchRenderTarget == null) {
            nearPatchRenderTarget = new Framebuffer(w.getWidth(), w.getHeight(), true, Minecraft.ON_OSX);
            depthCpy = new Framebuffer(w.getWidth(), w.getHeight(), true, Minecraft.ON_OSX);
        } else if (nearPatchRenderTarget.width != w.getWidth() || nearPatchRenderTarget.height != w.getHeight()) {
            nearPatchRenderTarget.resize(w.getWidth(), w.getHeight(), Minecraft.ON_OSX);
            depthCpy.resize(w.getWidth(), w.getHeight(), Minecraft.ON_OSX);
        }
        return nearPatchRenderTarget;
    }

    // apparently you cannot reference PlayerEntity from Entity mixin idk
    public static void onSetSize(Entity entity, double prevSize, double size) {
        if (entity instanceof PlayerEntity) {
            if (((PlayerEntity) entity).isSleeping()) {
                ((PlayerEntity) entity).stopSleeping();
            }
            if (entity instanceof ServerPlayerEntity) {
                SizeTrigger.trigger((ServerPlayerEntity) entity, prevSize, size);
            }
        }
    }

    public static double getAverageSize(Object a, Object b) {
        double sa = ((ISized) a).getSizeCM();
        double sb = ((ISized) b).getSizeCM();
        return sa == sb ? sa : Math.sqrt(sa * sb);
    }

    public static double getAverageSize(double a, double b) {
        return a == b ? a : Math.sqrt(a * b);
    }

    @OnlyIn(Dist.CLIENT)
    public static double getViewerSize() {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer != null ? ((ISized) viewer).getSizeCM() : 1.0;
    }

    @OnlyIn(Dist.CLIENT)
    public static double getViewerSize(float partialTicks) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer != null ? ((IRenderSized) viewer).getSizeCM(partialTicks) : 1.0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setCameraHeight(Entity entity, float prevCameraHeight, float cameraHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != entity) {
            return;
        }
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        camera.eyeHeightOld = prevCameraHeight;
        camera.eyeHeight = cameraHeight;
    }

    public static int copyDepth(Framebuffer tgt) {
//        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, tgt.getColorTextureId());
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depthCpy.getId());
//        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, tgt.frameBufferId);
//        GL30.glBlitFramebuffer(
//                0, 0,
//                tgt.width, tgt.height,
//                0, 0,
//                tgt.width, tgt.height,
//                GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST
//        );
//        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//        return depthCpy.getId();

        depthCpy.copyDepthFrom(tgt);
        return depthCpy.getDepthTextureId();
    }
}
