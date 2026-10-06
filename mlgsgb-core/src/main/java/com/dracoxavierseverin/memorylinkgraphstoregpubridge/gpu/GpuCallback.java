package com.dracoxavierseverin.memorylinkgraphstoregpubridge.gpu;

public interface GpuCallback {
    void onComplete(GpuResult result);
    void onError(Throwable error);
}
