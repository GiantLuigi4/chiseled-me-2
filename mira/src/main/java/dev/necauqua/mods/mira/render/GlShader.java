package dev.necauqua.mods.mira.render;

import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class GlShader {
    private final int id;

    public GlShader(int id) {
        this.id = id;
    }

    public void bind() {
        GL30.glUseProgram(id);
    }

    public void unbind() {
        GL30.glUseProgram(0);
    }

    HashMap<String, Integer> uniformIds = new HashMap<>();

    public int getUniformId(String name) {
        Integer iv = uniformIds.get(name);
        if (iv == null) {
            iv = GL30.glGetUniformLocation(id, name);
            uniformIds.put(name, iv);
        }
        return iv;
    }

    public void uniformInt(String name, int value) {
        GL30.glUniform1i(getUniformId(name), value);
    }

    public void uniformMat4(String name, FloatBuffer value) {
        GL30.glUniformMatrix4fv(getUniformId(name), false, value);
    }
}
