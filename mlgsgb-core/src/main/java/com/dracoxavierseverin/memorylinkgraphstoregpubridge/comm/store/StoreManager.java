package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

import android.content.Context;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl.DiskGlobalStore;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl.DiskLocalStore;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl.MemoryGlobalStore;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl.MemoryLocalStore;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class StoreManager {

    private static final long DEFAULT_GLOBAL_TEMP = 24L * 1024L * 1024L;
    private static final long DEFAULT_LOCAL_TEMP = 8L * 1024L * 1024L;
    private static final long DEFAULT_GLOBAL_PERSIST = 128L * 1024L * 1024L;
    private static final long DEFAULT_LOCAL_PERSIST = 32L * 1024L * 1024L;

    private final Context appContext;

    private volatile Store globalTemp;
    private volatile Store globalPersist;

    private final ConcurrentHashMap<String, Store> localTemps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Store> localPersists = new ConcurrentHashMap<>();

    public StoreManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private File persistRoot() {
        File dir = new File(appContext.getFilesDir(), "mlgsgb");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public Store globalTemp() {
        if (globalTemp == null) {
            synchronized (this) {
                if (globalTemp == null) {
                    globalTemp = new MemoryGlobalStore(new StoreBuilder()
                            .scope(StoreScope.GLOBAL)
                            .lifetime(StoreLifetime.TEMP)
                            .id("global-temp")
                            .capacity(DEFAULT_GLOBAL_TEMP));
                }
            }
        }
        return globalTemp;
    }

    public Store globalPersist() {
        if (globalPersist == null) {
            synchronized (this) {
                if (globalPersist == null) {
                    globalPersist = new DiskGlobalStore(new StoreBuilder()
                            .scope(StoreScope.GLOBAL)
                            .lifetime(StoreLifetime.PERSIST)
                            .id("global-persist")
                            .capacity(DEFAULT_GLOBAL_PERSIST), persistRoot());
                }
            }
        }
        return globalPersist;
    }

    public Store localTemp(String id) {
        return localTemps.computeIfAbsent(id, k -> new MemoryLocalStore(new StoreBuilder()
                .scope(StoreScope.LOCAL)
                .lifetime(StoreLifetime.TEMP)
                .id("local-temp-" + k)
                .capacity(DEFAULT_LOCAL_TEMP)
                .allowAll()));
    }

    public Store localPersist(String id) {
        return localPersists.computeIfAbsent(id, k -> new DiskLocalStore(new StoreBuilder()
                .scope(StoreScope.LOCAL)
                .lifetime(StoreLifetime.PERSIST)
                .id("local-persist-" + k)
                .capacity(DEFAULT_LOCAL_PERSIST)
                .allowAll(), persistRoot()));
    }

    private volatile StoreMover mover;

    public StoreMover mover() {
        if (mover == null) {
            synchronized (this) {
                if (mover == null) mover = new StoreMover();
            }
        }
        return mover;
    }

    public void releaseAll() {
        if (globalTemp != null) { globalTemp.release(); globalTemp = null; }
        if (globalPersist != null) { globalPersist.release(); globalPersist = null; }
        for (Store s : localTemps.values()) s.release();
        for (Store s : localPersists.values()) s.release();
        localTemps.clear();
        localPersists.clear();
    }
}
