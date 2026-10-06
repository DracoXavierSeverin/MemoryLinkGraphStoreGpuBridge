package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseGenerator;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseType;

import java.util.Random;

public class WorleyNoise implements NoiseGenerator {

    private long seed = 0L;
    private float frequency = 0.05f;
    private int octaves = 1;
    private int width = 256;
    private int height = 256;

    public WorleyNoise() {}

    private float hash(int x, int y, int k) {
        int n = x * 374761393 + y * 668265263 + k * 1274126177 + (int) seed;
        n = (n ^ (n >> 13)) * 1274126177;
        return ((n ^ (n >> 16)) & 0x7fffffff) / (float) 0x7fffffff;
    }

    private float singleWorley(float px, float py) {
        int cx = (int) Math.floor(px);
        int cy = (int) Math.floor(py);
        float minDist = Float.MAX_VALUE;
        for (int oy = -1; oy <= 1; oy++) {
            for (int ox = -1; ox <= 1; ox++) {
                int gx = cx + ox;
                int gy = cy + oy;
                float fx = hash(gx, gy, 0);
                float fy = hash(gx, gy, 1);
                float dx = (gx + fx) - px;
                float dy = (gy + fy) - py;
                float d = dx * dx + dy * dy;
                if (d < minDist) minDist = d;
            }
        }
        return (float) Math.sqrt(minDist);
    }

    @Override
    public NoiseType type() { return NoiseType.WORLEY; }

    @Override
    public NoiseGenerator seed(long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public NoiseGenerator frequency(float frequency) {
        this.frequency = frequency;
        return this;
    }

    @Override
    public NoiseGenerator octaves(int octaves) {
        this.octaves = Math.max(1, octaves);
        return this;
    }

    @Override
    public NoiseGenerator size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public float[] generate() {
        float[] out = new float[width * height];
        float amp = 1f;
        float freq = frequency;
        float maxAmp = 0f;
        for (int o = 0; o < octaves; o++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    out[y * width + x] += singleWorley(x * freq, y * freq) * amp;
                }
            }
            maxAmp += amp;
            amp *= 0.5f;
            freq *= 2f;
        }
        if (maxAmp > 0f) {
            for (int i = 0; i < out.length; i++) out[i] /= maxAmp;
        }
        return out;
    }
}
