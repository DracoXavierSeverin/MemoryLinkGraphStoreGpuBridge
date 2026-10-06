package com.dracoxavierseverin.mlgsgb.testapp;

import android.app.Application;
import android.util.Log;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.MLGSGB;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.Store;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuCallback;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuResult;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuScheduler;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu.GpuShaders;

public class TestApp extends Application {
    private static final String TAG = "MLGSGB-Test";

    @Override
    public void onCreate() {
        super.onCreate();
        MLGSGB lib = MLGSGB.get();
        lib.init(this);

        Store gTemp = lib.store().globalTemp();
        gTemp.put("moduleA", "user:profile", "Alice");
        gTemp.put("moduleA", "user:avatar", "avatar.png");
        gTemp.put("moduleA", "skip:me", "不搬");

        Store gPersist = lib.store().globalPersist();
        com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.MoveResult r =
                lib.store().mover().move(gTemp, "moduleA",
                        new String[]{"user:profile", "user:avatar"},
                        gPersist, "backup");

        Log.i(TAG, "moved = " + r.moved + ", failed = " + r.failed);
        Log.i(TAG, "persist[backup:user:profile] = " + gPersist.get("backup", "user:profile"));
        Log.i(TAG, "persist[backup:skip:me] = " + gPersist.get("backup", "skip:me"));

        final GpuScheduler sched = new GpuScheduler();
        sched.start();

        long tSubmit0 = System.currentTimeMillis();
        sched.submit(GpuShaders.perlin(), 512, 512,
                new String[]{"uFrequency", "uOctaves", "uSeed"},
                new float[]{0.03f, 4f, 42f},
                new GpuCallback() {
                    public void onComplete(GpuResult result) {
                        Log.i(TAG, "异步 GPU 完成: " + result.data.length
                                + " 像素, " + result.elapsedMs + " ms");
                    }
                    public void onError(Throwable t) {
                        Log.e(TAG, "异步 GPU 失败", t);
                    }
                });
        long tSubmit1 = System.currentTimeMillis();
        Log.i(TAG, "已提交，主线程立即返回, 提交耗时 = " + (tSubmit1 - tSubmit0) + " ms");

        Log.i(TAG, "=== MLGSGB 全部通道验证完成 ===");
    }
}
