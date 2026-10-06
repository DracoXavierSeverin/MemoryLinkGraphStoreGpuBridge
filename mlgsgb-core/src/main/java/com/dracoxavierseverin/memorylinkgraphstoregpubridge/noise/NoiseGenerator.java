package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise;

public interface NoiseGenerator {

    NoiseType type();

    NoiseGenerator seed(long seed);

    NoiseGenerator frequency(float frequency);

    NoiseGenerator octaves(int octaves);

    NoiseGenerator size(int width, int height);

    float[] generate();
}
