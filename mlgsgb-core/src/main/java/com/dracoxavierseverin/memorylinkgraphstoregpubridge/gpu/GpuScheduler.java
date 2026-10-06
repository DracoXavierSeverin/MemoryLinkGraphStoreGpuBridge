package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GpuScheduler {

    private static final String TAG = "MLGSGB-GpuScheduler";
    private static final long POLL_TIMEOUT_MS = 100L;

    private final GpuContext context;
    private final GpuCompute compute;
    private final LinkedBlockingQueue<GpuTask> queue = new LinkedBlockingQueue<>();

    private volatile boolean running = false;
    private Thread worker;

    public GpuScheduler() {
        this.context = new GpuContext();
        this.compute = new GpuCompute(context);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        worker = new Thread(this::workerLoop, "mlgsgb-gpu-scheduler");
        worker.start();
        Log.i(TAG, "scheduler started");
    }

    private void workerLoop() {
        try {
            context.init();
        } catch (Throwable t) {
            Log.e(TAG, "EGL init failed in worker", t);
            running = false;
            return;
        }

        while (running) {
            GpuTask task = null;
            try {
                task = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (task == null) continue;
            runTask(task);
        }

        try {
            compute.release();
            context.release();
        } catch (Throwable t) {
            Log.e(TAG, "release failed", t);
        }
        Log.i(TAG, "scheduler stopped");
    }

    private void runTask(GpuTask task) {
        long t0 = System.currentTimeMillis();
        try {
            float[] out = compute.executeWithUniforms(
                    task.shaderSrc, task.width, task.height,
                    task.uniformNames, task.uniformValues);
            long t1 = System.currentTimeMillis();
            GpuResult r = new GpuResult(out, task.width, task.height, t1 - t0);
            if (task.callback != null) task.callback.onComplete(r);
        } catch (Throwable t) {
            Log.e(TAG, "task failed", t);
            if (task.callback != null) task.callback.onError(t);
        }
    }

    public void submit(String shaderSrc, int width, int height,
                       String[] uniformNames, float[] uniformValues,
                       GpuCallback callback) {
        if (!running) {
            if (callback != null) {
                callback.onError(new IllegalStateException("GpuScheduler not started"));
            }
            return;
        }
        queue.offer(new GpuTask(shaderSrc, width, height, uniformNames, uniformValues, callback));
    }

    public int pendingTasks() {
        return queue.size();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(2000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            worker = null;
        }
    }
}
