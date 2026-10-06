package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseGenerator;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseType;

public class WaveNoise implements NoiseGenerator {

    private long seed = 0L;
    private float frequency = 0.05f;
    private int octaves = 3;
    private int width = 256;
    private int height = 256;

    public WaveNoise() {}

    private float singleWave(float x, float y, int layer) {
        double angle = (seed * 31L + layer * 137L) % 360L;
        double rad = Math.toRadians(angle);
        float dirX = (float) Math.cos(rad);
        float dirY = (float) Math.sin(rad);
        float phase = (float) (((seed + layer * 53L) % 1000L) / 1000.0 * Math.PI * 2);
        float proj = x * dirX + y * dirY;
        return (float) Math.sin(proj * frequency * (1 << layer) + phase);
    }

    @Override
    public NoiseType type() { return NoiseType.WAVE; }

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
        float maxAmp = 0f;
        for (int layer = 0; layer < octaves; layer++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    out[y * width + x] += singleWave(x, y, layer) * amp;
                }
            }
            maxAmp += amp;
            amp *= 0.5f;
        }
        if (maxAmp > 0f) {
            for (int i = 0; i < out.length; i++) out[i] /= maxAmp;
        }
        return out;
    }
}
