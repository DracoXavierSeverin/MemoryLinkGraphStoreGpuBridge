package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

public final class GpuShaders {

    private GpuShaders() {}

    private static final String HEADER =
            "#version 300 es\n" +
            "precision highp float;\n" +
            "in vec2 vUV;\n" +
            "uniform vec2 uResolution;\n" +
            "out vec4 fragColor;\n";

    private static final String PERLIN_FN =
            "vec2 hash2(vec2 p, float s) {\n" +
            "    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));\n" +
            "    return -1.0 + 2.0 * fract(sin(p + s) * 43758.5453123);\n" +
            "}\n" +
            "float perlin(vec2 p, float s) {\n" +
            "    vec2 i = floor(p);\n" +
            "    vec2 f = fract(p);\n" +
            "    vec2 u = f * f * (3.0 - 2.0 * f);\n" +
            "    float a = dot(hash2(i + vec2(0.0, 0.0), s), f - vec2(0.0, 0.0));\n" +
            "    float b = dot(hash2(i + vec2(1.0, 0.0), s), f - vec2(1.0, 0.0));\n" +
            "    float c = dot(hash2(i + vec2(0.0, 1.0), s), f - vec2(0.0, 1.0));\n" +
            "    float d = dot(hash2(i + vec2(1.0, 1.0), s), f - vec2(1.0, 1.0));\n" +
            "    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);\n" +
            "}\n";

    public static String perlin() {
        return HEADER +
                "uniform float uFrequency;\n" +
                "uniform float uOctaves;\n" +
                "uniform float uSeed;\n" +
                PERLIN_FN +
                "void main() {\n" +
                "    vec2 p = vUV * uResolution * uFrequency;\n" +
                "    float amp = 1.0;\n" +
                "    float freq = 1.0;\n" +
                "    float sum = 0.0;\n" +
                "    float maxAmp = 0.0;\n" +
                "    for (int o = 0; o < 8; o++) {\n" +
                "        if (float(o) >= uOctaves) break;\n" +
                "        sum += perlin(p * freq, uSeed + float(o) * 13.0) * amp;\n" +
                "        maxAmp += amp;\n" +
                "        amp *= 0.5;\n" +
                "        freq *= 2.0;\n" +
                "    }\n" +
                "    float v = sum / maxAmp;\n" +
                "    v = clamp(v * 0.5 + 0.5, 0.0, 1.0);\n" +
                "    fragColor = vec4(v, v, v, 1.0);\n" +
                "}\n";
    }

    private static final String WORLEY_FN =
            "vec2 hashW(vec2 p, float s) {\n" +
            "    vec3 p3 = fract(vec3(p.xyx) * 0.1031);\n" +
            "    p3 += dot(p3, p3.yzx + 33.33 + s);\n" +
            "    return fract((p3.xx + p3.yz) * p3.zy);\n" +
            "}\n" +
            "float worley(vec2 p, float s) {\n" +
            "    vec2 i = floor(p);\n" +
            "    vec2 f = fract(p);\n" +
            "    float minDist = 8.0;\n" +
            "    for (int oy = -1; oy <= 1; oy++) {\n" +
            "        for (int ox = -1; ox <= 1; ox++) {\n" +
            "            vec2 g = vec2(float(ox), float(oy));\n" +
            "            vec2 h = hashW(i + g, s);\n" +
            "            vec2 diff = g + h - f;\n" +
            "            minDist = min(minDist, dot(diff, diff));\n" +
            "        }\n" +
            "    }\n" +
            "    return sqrt(minDist);\n" +
            "}\n";

    public static String worley() {
        return HEADER +
                "uniform float uFrequency;\n" +
                "uniform float uOctaves;\n" +
                "uniform float uSeed;\n" +
                WORLEY_FN +
                "void main() {\n" +
                "    vec2 p = vUV * uResolution * uFrequency;\n" +
                "    float amp = 1.0;\n" +
                "    float freq = 1.0;\n" +
                "    float sum = 0.0;\n" +
                "    float maxAmp = 0.0;\n" +
                "    for (int o = 0; o < 8; o++) {\n" +
                "        if (float(o) >= uOctaves) break;\n" +
                "        sum += worley(p * freq, uSeed + float(o) * 13.0) * amp;\n" +
                "        maxAmp += amp;\n" +
                "        amp *= 0.5;\n" +
                "        freq *= 2.0;\n" +
                "    }\n" +
                "    float v = sum / maxAmp;\n" +
                "    v = clamp(v, 0.0, 1.0);\n" +
                "    fragColor = vec4(v, v, v, 1.0);\n" +
                "}\n";
    }

    public static String wave() {
        return HEADER +
                "uniform float uFrequency;\n" +
                "uniform float uOctaves;\n" +
                "uniform float uSeed;\n" +
                "void main() {\n" +
                "    vec2 p = vUV * uResolution;\n" +
                "    float amp = 1.0;\n" +
                "    float sum = 0.0;\n" +
                "    float maxAmp = 0.0;\n" +
                "    float freq = uFrequency;\n" +
                "    for (int layer = 0; layer < 6; layer++) {\n" +
                "        if (float(layer) >= uOctaves) break;\n" +
                "        float angle = mod(uSeed * 31.0 + float(layer) * 137.0, 360.0);\n" +
                "        float rad = radians(angle);\n" +
                "        vec2 dir = vec2(cos(rad), sin(rad));\n" +
                "        float phase = mod(uSeed + float(layer) * 53.0, 1000.0) / 1000.0 * 6.2831853;\n" +
                "        float proj = dot(p, dir);\n" +
                "        float arg = proj * freq + phase;\n" +
                "        float folded = fract(arg / 6.2831853) * 6.2831853;\n" +
                "        float w = sin(folded);\n" +
                "        sum += w * amp;\n" +
                "        maxAmp += amp;\n" +
                "        amp *= 0.5;\n" +
                "        freq *= 2.0;\n" +
                "    }\n" +
                "    float v = sum / maxAmp;\n" +
                "    v = clamp(v * 0.5 + 0.5, 0.0, 1.0);\n" +
                "    fragColor = vec4(v, v, v, 1.0);\n" +
                "}\n";
    }

    public static String fbm() {
        return HEADER +
                "uniform float uFrequency;\n" +
                "uniform float uOctaves;\n" +
                "uniform float uSeed;\n" +
                "uniform float uPower;\n" +
                PERLIN_FN +
                "void main() {\n" +
                "    vec2 p = vUV * uResolution * uFrequency;\n" +
                "    float amp = 1.0;\n" +
                "    float freq = 1.0;\n" +
                "    float sum = 0.0;\n" +
                "    float maxAmp = 0.0;\n" +
                "    for (int o = 0; o < 8; o++) {\n" +
                "        if (float(o) >= uOctaves) break;\n" +
                "        sum += perlin(p * freq, uSeed + float(o) * 13.0) * amp;\n" +
                "        maxAmp += amp;\n" +
                "        amp *= 0.5;\n" +
                "        freq *= 2.0;\n" +
                "    }\n" +
                "    float v = sum / maxAmp;\n" +
                "    v = v * 0.5 + 0.5;\n" +
                "    v = clamp(v, 0.0, 1.0);\n" +
                "    v = pow(v, 1.0 / uPower);\n" +
                "    fragColor = vec4(v, v, v, 1.0);\n" +
                "}\n";
    }
}
