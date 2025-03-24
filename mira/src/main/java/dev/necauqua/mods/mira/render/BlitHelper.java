package dev.necauqua.mods.mira.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.necauqua.mods.mira.size.MixinHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class BlitHelper {
    private static GlShader DEPTH_BLIT = ShaderHelper.load(
            "assets/mira/shaders/blit.vsh",
            "assets/mira/shaders/depth_blit.fsh",
            "depth_blit"
    );

    public static void _quickBlit(Framebuffer fb, int w, int h, boolean flag) {
//        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fb.getColorTextureId());
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fb.frameBufferId);
        GL30.glBlitFramebuffer(
                0, 0, w, h,
                0, 0, w, h,
                GL30.GL_COLOR_BUFFER_BIT,
                GL30.GL_NEAREST
        );
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
    }

    public static void _blitToScreen(
            Framebuffer fb, Framebuffer tgt, int w, int h, boolean flag,
            FloatBuffer projNear, FloatBuffer projFar
    ) {
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._matrixMode(5889);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
        GlStateManager._matrixMode(5888);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._translatef(0.0F, 0.0F, -2000.0F);
        GlStateManager._viewport(0, 0, w, h);
        GlStateManager._enableTexture();
        GlStateManager._disableLighting();
        GlStateManager._enableAlphaTest();
        if (flag) {
            GlStateManager._disableBlend();
            GlStateManager._enableColorMaterial();
        } else {
            GlStateManager._enableBlend();
            GlStateManager._disableColorMaterial();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.defaultAlphaFunc();
        }

        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);

        fb.bindRead();

        float f = (float) w;
        float f1 = (float) h;
        float f2 = (float) fb.viewWidth / (float) fb.width;
        float f3 = (float) fb.viewHeight / (float) fb.height;
        Tessellator tessellator = RenderSystem.renderThreadTesselator();

        DEPTH_BLIT.bind();

        int id = MixinHelpers.copyDepth(tgt);
        tgt.bindWrite(true);

        // set texture ids
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        int bound0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, fb.getColorTextureId());
        DEPTH_BLIT.uniformInt("texture", 0);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        int bound1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, fb.getDepthTextureId());
        DEPTH_BLIT.uniformInt("sampNear", 1);
        GL30.glActiveTexture(GL30.GL_TEXTURE2);
        int bound2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
        DEPTH_BLIT.uniformInt("sampFar", 2);

        // set matrices
        DEPTH_BLIT.uniformMat4("projNear", projNear);
        DEPTH_BLIT.uniformMat4("projFar", projFar);

        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.vertex(0.0D, f1, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(f, f1, 0.0D).uv(f2, 0.0F).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(f, 0.0D, 0.0D).uv(f2, f3).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, f3).color(255, 255, 255, 255).endVertex();
        tessellator.end();
        DEPTH_BLIT.unbind();

        fb.unbindRead();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._matrixMode(5889);
        GlStateManager._popMatrix();
        GlStateManager._matrixMode(5888);
        GlStateManager._popMatrix();

        GL20.glActiveTexture(GL20.GL_TEXTURE0);
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, bound0);
        GL20.glActiveTexture(GL20.GL_TEXTURE1);
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, bound1);
        GL20.glActiveTexture(GL20.GL_TEXTURE2);
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, bound2);
        GL20.glActiveTexture(GL20.GL_TEXTURE0);
    }
}
