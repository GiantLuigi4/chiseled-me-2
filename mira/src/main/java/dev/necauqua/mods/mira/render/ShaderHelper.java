package dev.necauqua.mods.mira.render;

import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;

public class ShaderHelper {
    public static String readResource(String dir) throws IOException {
        InputStream is = GlShader.class.getClassLoader().getResourceAsStream(dir);
        int available = is.available();
        byte[] data = new byte[available];
        is.read(data);
        String str = new String(data);
        try {
            is.close();
        } catch (Throwable ignored) {
        }

        return str;
    }

    protected static int loadCode(int type, String data, String pth) {
        int id = GL30.glCreateShader(type);

        GL30.glShaderSource(id, data);
        GL30.glCompileShader(id);

        int len = GL30.glGetShaderi(id, GL30.GL_INFO_LOG_LENGTH);

        if (len != 0) {
            System.err.println(GL30.glGetShaderInfoLog(id));
            throw new RuntimeException("Failed to compile shader: " + pth);
        }

        return id;
    }

    public static int createProgram(String ident, int... shaders) {
        int id = GL30.glCreateProgram();

        for (int shader : shaders) {
            GL30.glAttachShader(id, shader);
        }

        GL30.glValidateProgram(id);
        GL30.glLinkProgram(id);

        int len = GL30.glGetProgrami(id, GL30.GL_INFO_LOG_LENGTH);
        if (len != 0) {
            System.err.println(GL30.glGetProgramInfoLog(id));
            throw new RuntimeException("Failed to compile shader: " + ident);
        }

        return id;
    }

    public static GlShader load(String vsh, String fsh, String ident) {
        try {

            String vertex = readResource(vsh);
            String fragment = readResource(fsh);

            int vert = loadCode(GL30.GL_VERTEX_SHADER, vertex, vsh);
            int frag = loadCode(GL30.GL_FRAGMENT_SHADER, fragment, fsh);

            int prog = createProgram(ident, vert, frag);

            GL30.glDeleteShader(vert);
            GL30.glDeleteShader(frag);

            return new GlShader(prog);

        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }
}
