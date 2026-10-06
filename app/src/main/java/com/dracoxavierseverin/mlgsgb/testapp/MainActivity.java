package com.dracoxavierseverin.mlgsgb.testapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.MLGSGB;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuCompute;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuContext;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuShaders;

public class MainActivity extends Activity {
    private static final String TAG = "MLGSGB-Test";
    private static final int W = 512;
    private static final int H = 512;

    private GpuContext gpuCtx;
    private GpuCompute compute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        gpuCtx = new GpuContext();
        gpuCtx.init();
        compute = new GpuCompute(gpuCtx);
        android.widget.Button commBtn = new android.widget.Button(this);
        commBtn.setText("打开通信演示");
        commBtn.setOnClickListener(v -> startActivity(new android.content.Intent(this, CommActivity.class)));
        root.addView(commBtn);

        addPair(root, "Perlin", GpuShaders.perlin(),
                new String[]{"uFrequency", "uOctaves", "uSeed"},
                new float[]{0.03f, 4f, 42f},
                0.03f, 4, 42L);

        addPair(root, "Worley", GpuShaders.worley(),
                new String[]{"uFrequency", "uOctaves", "uSeed"},
                new float[]{0.08f, 3f, 42f},
                0.08f, 3, 42L);

        addPair(root, "Wave", GpuShaders.wave(),
                new String[]{"uFrequency", "uOctaves", "uSeed"},
                new float[]{0.05f, 5f, 42f},
                0.05f, 5, 42L);

        addPair(root, "FBM", GpuShaders.fbm(),
                new String[]{"uFrequency", "uOctaves", "uSeed", "uPower"},
                new float[]{0.008f, 6f, 42f, 2.5f},
                0.008f, 6, 42L);

        compute.release();
        gpuCtx.release();

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    private void addPair(LinearLayout root, String name, String shader,
                         String[] uniformNames, float[] uniformValues,
                         float freq, int octaves, long seed) {

        long tCpu0 = System.currentTimeMillis();
        float[] cpuData;
        switch (name) {
            case "Perlin": cpuData = MLGSGB.get().noise().perlin().seed(seed).frequency(freq).octaves(octaves).size(W, H).generate(); break;
            case "Worley": cpuData = MLGSGB.get().noise().worley().seed(seed).frequency(freq).octaves(octaves).size(W, H).generate(); break;
            case "Wave":   cpuData = MLGSGB.get().noise().wave().seed(seed).frequency(freq).octaves(octaves).size(W, H).generate(); break;
            case "FBM":    cpuData = MLGSGB.get().noise().fbm().seed(seed).frequency(freq).octaves(octaves).power(2.5f).size(W, H).generate(); break;
            default:       cpuData = MLGSGB.get().noise().perlin().size(W, H).generate(); break;
        }
        long tCpu1 = System.currentTimeMillis();

        long tGpu0 = System.currentTimeMillis();
        float[] gpuData = compute.executeWithUniforms(shader, W, H, uniformNames, uniformValues);
        long tGpu1 = System.currentTimeMillis();

        root.addView(makeLabel(name + " CPU  (" + (tCpu1 - tCpu0) + " ms)"));
        root.addView(makeImage(cpuData));

        root.addView(makeLabel(name + " GPU  (" + (tGpu1 - tGpu0) + " ms)"));
        root.addView(makeImage(gpuData));

        Log.i(TAG, name + " CPU=" + (tCpu1 - tCpu0) + "ms GPU=" + (tGpu1 - tGpu0) + "ms");
    }

    private TextView makeLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setPadding(0, 24, 0, 8);
        return tv;
    }

    private ImageView makeImage(float[] data) {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float v : data) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        float range = (max - min) == 0f ? 1f : (max - min);

        int[] pixels = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            int g = (int) ((data[i] - min) / range * 255f);
            if (g < 0) g = 0;
            if (g > 255) g = 255;
            pixels[i] = Color.argb(255, g, g, g);
        }

        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, W, 0, 0, W, H);

        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bmp);
        return iv;
    }
}
