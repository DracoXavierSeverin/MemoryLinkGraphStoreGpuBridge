package com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.GpuBridge;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGpuBridge implements GpuBridge {
    private boolean initialized = false;
    private final ConcurrentHashMap<String, Object> buffers = new ConcurrentHashMap<>();

    @Override
    public void initGpuContext() {
        initialized = true;
    }

    @Override
    public void uploadBuffer(String bufferId, Object data, int size) {
        if (!initialized || bufferId == null) return;
        buffers.put(bufferId, data);
    }

    @Override
    public void dispatchCompute(String kernelId, int workGroups) {
        if (!initialized) return;
        // 预留：后续接入 OpenGL ES / Vulkan 计算管线
    }

    @Override
    public void releaseGpuContext() {
        buffers.clear();
        initialized = false;
    }
}
