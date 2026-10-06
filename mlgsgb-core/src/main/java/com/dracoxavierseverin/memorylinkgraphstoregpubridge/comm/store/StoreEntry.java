package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

public class StoreEntry {

    public final String moduleId;
    public final String tag;
    public final Object content;
    public final long createdAt;

    public StoreEntry(String moduleId, String tag, Object content) {
        this.moduleId = moduleId;
        this.tag = tag;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
    }
}
