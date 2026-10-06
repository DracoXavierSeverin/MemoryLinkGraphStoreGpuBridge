package com.dracoxavierseverin.memorylinkgraphstoregpubridge;

import android.content.Context;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.MessageBus;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.impl.DefaultMessageBus;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreManager;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl.DefaultGpuBridge;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl.DefaultGraphStore;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl.DefaultMemoryLink;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.noise.NoiseBuilder;

public final class MLGSGB {
    private static volatile MLGSGB instance;

    private final MemoryLink memoryLink;
    private final GraphStore graphStore;
    private final GpuBridge gpuBridge;
    private final MessageBus messageBus;

    private volatile Context appContext;
    private volatile StoreManager storeManager;

    private MLGSGB() {
        this.memoryLink = new DefaultMemoryLink();
        this.graphStore = new DefaultGraphStore();
        this.gpuBridge = new DefaultGpuBridge();
        this.messageBus = new DefaultMessageBus();
    }

    public static MLGSGB get() {
        if (instance == null) {
            synchronized (MLGSGB.class) {
                if (instance == null) {
                    instance = new MLGSGB();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private Context requireContext() {
        Context ctx = appContext;
        if (ctx == null) {
            throw new IllegalStateException("MLGSGB not initialized. Call MLGSGB.get().init(context) first.");
        }
        return ctx;
    }

    public StoreManager store() {
        if (storeManager == null) {
            synchronized (this) {
                if (storeManager == null) {
                    storeManager = new StoreManager(requireContext());
                }
            }
        }
        return storeManager;
    }

    public MemoryLink memory() { return memoryLink; }
    public GraphStore graph() { return graphStore; }
    public GpuBridge gpu() { return gpuBridge; }
    public NoiseBuilder noise() { return new NoiseBuilder(); }
    public MessageBus bus() { return messageBus; }
}
