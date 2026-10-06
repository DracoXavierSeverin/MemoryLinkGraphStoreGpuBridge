package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GpuCompute {

    private static final String TAG = "MLGSGB-GpuCompute";

    private static final String VERTEX_SHADER =
            "#version 300 es\n" +
            "in vec2 aPos;\n" +
            "out vec2 vUV;\n" +
            "void main() {\n" +
            "    vUV = aPos * 0.5 + 0.5;\n" +
            "    gl_Position = vec4(aPos, 0.0, 1.0);\n" +
            "}\n";

    private final GpuContext context;

    private int program = 0;
    private String programSource = null;
    private int fbo = 0;
    private int outTexture = 0;
    private int vbo = 0;
    private int vao = 0;

    private int texWidth = 0;
    private int texHeight = 0;

    public GpuCompute(GpuContext context) {
        this.context = context;
    }

    private static int compileShader(int type, String src) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, src);
        GLES30.glCompileShader(shader);
        int[] status = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed: " + log);
        }
        return shader;
    }

    private void ensureProgram(String fragmentSrc) {
        if (program != 0) {
            if (fragmentSrc.equals(programSource)) return;
            GLES30.glDeleteProgram(program);
            program = 0;
        }

        int vs = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSrc);

        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vs);
        GLES30.glAttachShader(program, fs);
        GLES30.glLinkProgram(program);

        int[] status = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES30.glGetProgramInfoLog(program);
            GLES30.glDeleteProgram(program);
            program = 0;
            throw new RuntimeException("Program link failed: " + log);
        }

        GLES30.glDeleteShader(vs);
        GLES30.glDeleteShader(fs);

        float[] quad = {
                -1f, -1f,
                 1f, -1f,
                -1f,  1f,
                 1f,  1f
        };
        FloatBuffer buf = ByteBuffer.allocateDirect(quad.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buf.put(quad).position(0);

        int[] vaoArr = new int[1];
        GLES30.glGenVertexArrays(1, vaoArr, 0);
        vao = vaoArr[0];
        GLES30.glBindVertexArray(vao);

        int[] vboArr = new int[1];
        GLES30.glGenBuffers(1, vboArr, 0);
        vbo = vboArr[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, quad.length * 4, buf, GLES30.GL_STATIC_DRAW);

        int posLoc = GLES30.glGetAttribLocation(program, "aPos");
        GLES30.glEnableVertexAttribArray(posLoc);
        GLES30.glVertexAttribPointer(posLoc, 2, GLES30.GL_FLOAT, false, 0, 0);

        Log.i(TAG, "program ready");
        programSource = fragmentSrc;
    }

    private void prepareTarget(int width, int height) {
        if (texWidth == width && texHeight == height && fbo != 0) return;

        releaseTarget();

        int[] texArr = new int[1];
        GLES30.glGenTextures(1, texArr, 0);
        outTexture = texArr[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, outTexture);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F,
                width, height, 0, GLES30.GL_RGBA, GLES30.GL_FLOAT, null);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        int[] fboArr = new int[1];
        GLES30.glGenFramebuffers(1, fboArr, 0);
        fbo = fboArr[0];
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, outTexture, 0);

        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO incomplete: " + status);
        }

        texWidth = width;
        texHeight = height;
        Log.i(TAG, "target ready " + width + "x" + height);
    }

    private void releaseTarget() {
        if (fbo != 0) { GLES30.glDeleteFramebuffers(1, new int[]{fbo}, 0); fbo = 0; }
        if (outTexture != 0) { GLES30.glDeleteTextures(1, new int[]{outTexture}, 0); outTexture = 0; }
        texWidth = 0;
        texHeight = 0;
    }

    public float[] execute(String fragmentSrc, int width, int height) {
        context.makeCurrent();
        ensureProgram(fragmentSrc);
        prepareTarget(width, height);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glUseProgram(program);

        int resLoc = GLES30.glGetUniformLocation(program, "uResolution");
        if (resLoc >= 0) GLES30.glUniform2f(resLoc, width, height);

        GLES30.glBindVertexArray(vao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glFinish();

        FloatBuffer fb = ByteBuffer.allocateDirect(width * height * 4 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_FLOAT, fb);
        fb.position(0);

        float[] result = new float[width * height];
        for (int y = 0; y < height; y++) {
            int flippedY = height - 1 - y;
            for (int x = 0; x < width; x++) {
                result[y * width + x] = fb.get((flippedY * width + x) * 4);
            }
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        Log.i(TAG, "execute done " + width + "x" + height);
        return result;
    }

    public void release() {
        if (program != 0) { GLES30.glDeleteProgram(program); program = 0; }
        if (vbo != 0) { GLES30.glDeleteBuffers(1, new int[]{vbo}, 0); vbo = 0; }
        if (vao != 0) { GLES30.glDeleteVertexArrays(1, new int[]{vao}, 0); vao = 0; }
        releaseTarget();
    }

    public float[] executeWithUniforms(String fragmentSrc, int width, int height,
                                       String[] uniformNames, float[] uniformValues) {
        context.makeCurrent();
        ensureProgram(fragmentSrc);
        prepareTarget(width, height);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glUseProgram(program);

        int resLoc = GLES30.glGetUniformLocation(program, "uResolution");
        if (resLoc >= 0) GLES30.glUniform2f(resLoc, width, height);

        if (uniformNames != null && uniformValues != null) {
            for (int i = 0; i < uniformNames.length && i < uniformValues.length; i++) {
                int loc = GLES30.glGetUniformLocation(program, uniformNames[i]);
                if (loc >= 0) GLES30.glUniform1f(loc, uniformValues[i]);
            }
        }

        GLES30.glBindVertexArray(vao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glFinish();

        FloatBuffer fb = ByteBuffer.allocateDirect(width * height * 4 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_FLOAT, fb);
        fb.position(0);

        float[] result = new float[width * height];
        for (int y = 0; y < height; y++) {
            int flippedY = height - 1 - y;
            for (int x = 0; x < width; x++) {
                result[y * width + x] = fb.get((flippedY * width + x) * 4);
            }
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        return result;
    }
}
