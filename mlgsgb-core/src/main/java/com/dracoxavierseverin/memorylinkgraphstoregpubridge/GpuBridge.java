package com.dracoxavierseverin.memorylinkgraphstoregpubridge;

public interface GpuBridge {
    void initGpuContext();
    void uploadBuffer(String bufferId, Object data, int size);
    void dispatchCompute(String kernelId, int workGroups);
    void releaseGpuContext();
}
