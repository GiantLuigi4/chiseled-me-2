package dev.necauqua.mods.mira.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import org.lwjgl.opengl.GL30;

public class DepthOnlyTarget {
    int tex;
    int id = -1;
    protected int width, height;

    public DepthOnlyTarget(int width, int height) {
        tex = TextureUtil.generateTextureId();
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
