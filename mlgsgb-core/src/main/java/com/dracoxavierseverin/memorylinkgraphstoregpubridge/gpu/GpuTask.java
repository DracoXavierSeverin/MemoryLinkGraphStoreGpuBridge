package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

class GpuTask {

    final String shaderSrc;
    final int width;
    final int height;
    final String[] uniformNames;
    final float[] uniformValues;
    final GpuCallback callback;

    GpuTask(String shaderSrc, int width, int height,
            String[] uniformNames, float[] uniformValues,
            GpuCallback callback) {
        this.shaderSrc = shaderSrc;
        this.width = width;
        this.height = height;
        this.uniformNames = uniformNames;
        this.uniformValues = uniformValues;
        this.callback = callback;
    }
}
