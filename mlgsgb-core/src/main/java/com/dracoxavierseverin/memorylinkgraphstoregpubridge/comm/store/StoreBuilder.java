package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

import java.util.HashSet;
import java.util.Set;

public class StoreBuilder {

    private StoreScope scope = StoreScope.GLOBAL;
    private StoreLifetime lifetime = StoreLifetime.TEMP;
    private long capacityBytes = -1L;
    private String id = "default";
    private final Set<String> allowedModules = new HashSet<>();

    public StoreBuilder scope(StoreScope scope) {
        this.scope = scope;
        return this;
    }

    public StoreBuilder lifetime(StoreLifetime lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public StoreBuilder capacity(long bytes) {
        this.capacityBytes = bytes;
        return this;
    }

    public StoreBuilder id(String id) {
        this.id = id;
        return this;
    }

    public StoreBuilder allow(String moduleId) {
        this.allowedModules.add(moduleId);
        return this;
    }

    public StoreBuilder allowAll() {
        this.allowedModules.clear();
        this.allowedModules.add("*");
        return this;
    }

    public StoreScope getScope() { return scope; }
    public StoreLifetime getLifetime() { return lifetime; }
    public long getCapacityBytes() { return capacityBytes; }
    public String getId() { return id; }
    public Set<String> getAllowedModules() { return allowedModules; }
}
