package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.Store;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreBuilder;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreLifetime;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreScope;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

public class DiskGlobalStore implements Store {

    private static final long DEFAULT_CAPACITY = 128L * 1024L * 1024L;

    private final String id;
    private final long capacityBytes;
    private final File baseDir;
    private final AtomicLong usedBytes = new AtomicLong(0);

    public DiskGlobalStore(StoreBuilder builder, File parentDir) {
        this.id = builder.getId() == null ? "global-persist" : builder.getId();
        long cap = builder.getCapacityBytes();
        this.capacityBytes = cap > 0 ? cap : DEFAULT_CAPACITY;
        this.baseDir = new File(parentDir, id);
        if (!baseDir.exists()) baseDir.mkdirs();
        scanUsage();
    }

    private void scanUsage() {
        long total = 0;
        File[] modules = baseDir.listFiles();
        if (modules != null) {
            for (File m : modules) {
                if (m.isDirectory()) {
                    File[] files = m.listFiles();
                    if (files != null) {
                        for (File f : files) total += f.length();
                    }
                }
            }
        }
        usedBytes.set(total);
    }

    private File fileFor(String moduleId, String tag) {
        String safeTag = URLEncoder.encode(tag);
        return new File(new File(baseDir, moduleId), safeTag + ".dat");
    }

    protected boolean checkPermission(String moduleId) { return true; }

    @Override
    public String id() { return id; }

    @Override
    public StoreScope scope() { return StoreScope.GLOBAL; }

    @Override
    public StoreLifetime lifetime() { return StoreLifetime.PERSIST; }

    @Override
    public long capacityBytes() { return capacityBytes; }

    @Override
    public long usedBytes() { return usedBytes.get(); }

    @Override
    public int size() {
        int total = 0;
        File[] modules = baseDir.listFiles();
        if (modules != null) {
            for (File m : modules) {
                if (m.isDirectory()) {
                    File[] files = m.listFiles();
                    if (files != null) total += files.length;
                }
            }
        }
        return total;
    }

    @Override
    public boolean put(String moduleId, String tag, Object content) {
        if (moduleId == null || tag == null || content == null) return false;
        if (!checkPermission(moduleId)) return false;

        File target = fileFor(moduleId, tag);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        long oldSize = target.exists() ? target.length() : 0;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(target))) {
            oos.writeObject(content);
        } catch (Throwable t) {
            return false;
        }

        long newSize = target.length();
        long projected = usedBytes.get() - oldSize + newSize;
        if (projected > capacityBytes) {
            if (oldSize == 0) target.delete();
            return false;
        }

        usedBytes.addAndGet(newSize - oldSize);
        return true;
    }

    @Override
    public Object get(String moduleId, String tag) {
        if (moduleId == null || tag == null) return null;
        if (!checkPermission(moduleId)) return null;
        File target = fileFor(moduleId, tag);
        if (!target.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(target))) {
            return ois.readObject();
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public boolean contains(String moduleId, String tag) {
        if (moduleId == null || tag == null) return false;
        if (!checkPermission(moduleId)) return false;
        return fileFor(moduleId, tag).exists();
    }

    @Override
    public boolean remove(String moduleId, String tag) {
        if (moduleId == null || tag == null) return false;
        if (!checkPermission(moduleId)) return false;
        File target = fileFor(moduleId, tag);
        if (!target.exists()) return false;

        long size = target.length();
        if (target.delete()) {
            usedBytes.addAndGet(-size);
            File parent = target.getParentFile();
            if (parent != null) {
                File[] siblings = parent.listFiles();
                if (siblings == null || siblings.length == 0) parent.delete();
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        deleteRecursive(baseDir);
        baseDir.mkdirs();
        usedBytes.set(0);
    }

    @Override
    public void release() {
        clear();
    }

    private static void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File c : children) deleteRecursive(c);
        }
        file.delete();
    }

    @Override
    public java.util.List<String> modules() {
        java.util.List<String> result = new java.util.ArrayList<>();
        File[] modules = baseDir.listFiles();
        if (modules != null) {
            for (File m : modules) {
                if (m.isDirectory()) result.add(m.getName());
            }
        }
        return result;
    }

    @Override
    public java.util.List<String> tags(String moduleId) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (!checkPermission(moduleId)) return result;
        File dir = new File(baseDir, moduleId);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                if (name.endsWith(".dat")) {
                    String raw = name.substring(0, name.length() - 4);
                    try {
                        result.add(URLDecoder.decode(raw, "UTF-8"));
                    } catch (Throwable ignored) {
                        result.add(raw);
                    }
                }
            }
        }
        return result;
    }
}
