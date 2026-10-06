package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreBuilder;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreLifetime;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreScope;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DiskLocalStore extends DiskGlobalStore {

    private final Set<String> allowedModules = ConcurrentHashMap.newKeySet();

    public DiskLocalStore(StoreBuilder builder, File parentDir) {
        super(builder, parentDir);
        this.allowedModules.addAll(builder.getAllowedModules());
        if (this.allowedModules.isEmpty()) {
            this.allowedModules.add("*");
        }
    }

    @Override
    public StoreScope scope() { return StoreScope.LOCAL; }

    @Override
    public StoreLifetime lifetime() { return StoreLifetime.PERSIST; }

    @Override
    protected boolean checkPermission(String moduleId) {
        if (moduleId == null) return false;
        return allowedModules.contains("*") || allowedModules.contains(moduleId);
    }
}
