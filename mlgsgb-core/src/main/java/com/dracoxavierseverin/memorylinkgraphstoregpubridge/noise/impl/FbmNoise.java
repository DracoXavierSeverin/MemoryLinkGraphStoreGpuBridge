package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseGenerator;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseType;

public class FbmNoise implements NoiseGenerator {

    private long seed = 0L;
    private float frequency = 0.02f;
    private int octaves = 5;
    private int width = 256;
    private int height = 256;
    private float power = 1.0f;

    private static final float LACUNARITY = 2.0f;
    private static final float GAIN = 0.5f;

    public FbmNoise() {}

    public FbmNoise power(float power) {
        this.power = Math.max(0.1f, power);
        return this;
    }

    @Override
    public NoiseType type() { return NoiseType.FBM; }

    @Override
    public NoiseGenerator seed(long seed) { this.seed = seed; return this; }

    @Override
    public NoiseGenerator frequency(float frequency) { this.frequency = frequency; return this; }

    @Override
    public NoiseGenerator octaves(int octaves) { this.octaves = Math.max(1, octaves); return this; }

    @Override
    public NoiseGenerator size(int width, int height) { this.width = width; this.height = height; return this; }

    @Override
    public float[] generate() {
        float[] out = new float[width * height];
        float amp = 1f;
        float freq = frequency;
        float maxAmp = 0f;

        for (int o = 0; o < octaves; o++) {
            float[] layer = new PerlinNoise()
                    .seed(seed + o * 1013L)
                    .frequency(freq)
                    .octaves(1)
                    .size(width, height)
                    .generate();

            for (int i = 0; i < out.length; i++) {
                out[i] += layer[i] * amp;
            }

            maxAmp += amp;
            amp *= GAIN;
            freq *= LACUNARITY;
        }

        if (maxAmp > 0f) {
            for (int i = 0; i < out.length; i++) out[i] /= maxAmp;
        }

        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float v : out) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        float range = (max - min) == 0f ? 1f : (max - min);

        float invPower = 1.0f / power;
        for (int i = 0; i < out.length; i++) {
            float norm = (out[i] - min) / range;
            if (norm < 0f) norm = 0f;
            if (norm > 1f) norm = 1f;
            out[i] = (float) Math.pow(norm, invPower) * 2f - 1f;
        }

        return out;
    }
}
