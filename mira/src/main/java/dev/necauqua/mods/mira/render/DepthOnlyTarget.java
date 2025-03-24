package dev.necauqua.mods.mira.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import org.lwjgl.opengl.GL30;

public class DepthOnlyTarget {
    int tex = -1;
    int id = -1;
    protected int width, height;

    public DepthOnlyTarget(int width, int height) {
        this.width = width;
        this.height = height;
        resize();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        resize();
    }

    protected void resize() {
        if (id != -1) {
            GL30.glDeleteFramebuffers(id);
        }
        if (tex == -1) {
//            GL30.glDeleteTextures(tex);
            tex = TextureUtil.generateTextureId();
        }

        id = GL30.glGenFramebuffers();

        GlStateManager._bindTexture(this.tex);
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 10496);
        GlStateManager._texParameter(3553, 10243, 10496);
        GlStateManager._texParameter(3553, 34892, 0);
        GlStateManager._glBindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, this.id);
        GlStateManager._glFramebufferTexture2D(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_DEPTH_ATTACHMENT, 3553, this.tex, 0);
        GlStateManager._glBindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, 0);
        GlStateManager._texImage2D(3553, 0, org.lwjgl.opengl.GL30.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL, org.lwjgl.opengl.GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer creation failed");
        } else {
            System.out.println("Framebuffer with only depth attachment created successfully");
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTex() {
        return tex;
    }

    public int getId() {
        return id;
    }
}
