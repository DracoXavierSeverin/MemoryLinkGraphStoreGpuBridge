package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

public class GpuResult {

    public final float[] data;
    public final int width;
    public final int height;
    public final long elapsedMs;

    public GpuResult(float[] data, int width, int height, long elapsedMs) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.elapsedMs = elapsedMs;
    }
}
