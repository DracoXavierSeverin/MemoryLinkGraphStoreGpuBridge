package com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl.FbmNoise;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl.PerlinNoise;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl.WaveNoise;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.impl.WorleyNoise;

public class NoiseBuilder {

    private NoiseType type = NoiseType.PERLIN;
    private long seed = 0L;
    private float frequency = 0.05f;
    private int octaves = 1;
    private int width = 256;
    private int height = 256;
    private float power = 1.0f;

    public NoiseBuilder perlin() { this.type = NoiseType.PERLIN; return this; }
    public NoiseBuilder worley() { this.type = NoiseType.WORLEY; return this; }
    public NoiseBuilder wave()   { this.type = NoiseType.WAVE;   return this; }
    public NoiseBuilder fbm()    { this.type = NoiseType.FBM;    return this; }

    public NoiseBuilder seed(long seed)    { this.seed = seed; return this; }
    public NoiseBuilder frequency(float f) { this.frequency = f; return this; }
    public NoiseBuilder octaves(int o)     { this.octaves = o; return this; }
    public NoiseBuilder size(int w, int h) { this.width = w; this.height = h; return this; }
    public NoiseBuilder power(float p)     { this.power = p; return this; }

    public NoiseGenerator build() { return create(); }

    public float[] generate() { return create().generate(); }

    private NoiseGenerator create() {
        switch (type) {
            case PERLIN:
                return new PerlinNoise().seed(seed).frequency(frequency).octaves(octaves).size(width, height);
            case WORLEY:
                return new WorleyNoise().seed(seed).frequency(frequency).octaves(octaves).size(width, height);
            case WAVE:
                return new WaveNoise().seed(seed).frequency(frequency).octaves(octaves).size(width, height);
            case FBM: {
                FbmNoise fbm = new FbmNoise();
                fbm.seed(seed);
                fbm.frequency(frequency);
                fbm.octaves(octaves);
                fbm.size(width, height);
                fbm.power(power);
                return fbm;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented: " + type);
        }
    }
}
