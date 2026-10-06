package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseGenerator;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseType;

public class PerlinNoise implements NoiseGenerator {

    private long seed = 0L;
    private float frequency = 0.05f;
    private int octaves = 1;
    private int width = 256;
    private int height = 256;

    private int[] perm = new int[512];

    public PerlinNoise() {
        buildPermutation();
    }

    private void buildPermutation() {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        java.util.Random rnd = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    @Override
    public NoiseType type() { return NoiseType.PERLIN; }

    @Override
    public NoiseGenerator seed(long seed) {
        this.seed = seed;
        buildPermutation();
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

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 7;
        float u = (h < 4) ? x : y;
        float v = (h < 4) ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private float singlePerlin(float x, float y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        float u = fade(xf);
        float v = fade(yf);
        int aa = perm[perm[xi] + yi];
        int ab = perm[perm[xi] + yi + 1];
        int ba = perm[perm[xi + 1] + yi];
        int bb = perm[perm[xi + 1] + yi + 1];
        float x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
        float x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);
        return lerp(x1, x2, v);
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
                    out[y * width + x] += singlePerlin(x * freq, y * freq) * amp;
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
