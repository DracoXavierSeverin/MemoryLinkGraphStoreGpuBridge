package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.MessageBus;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.MessageListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultMessageBus implements MessageBus {

    private final Set<String> modules = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Subscription>> subscriptions =
            new ConcurrentHashMap<>();

    private static class Subscription {
        final String moduleId;
        final MessageListener listener;

        Subscription(String moduleId, MessageListener listener) {
            this.moduleId = moduleId;
            this.listener = listener;
        }
    }

    @Override
    public void registerModule(String moduleId) {
        if (moduleId == null) return;
        modules.add(moduleId);
    }

    @Override
    public void unregisterModule(String moduleId) {
        if (moduleId == null) return;
        modules.remove(moduleId);
        for (CopyOnWriteArrayList<Subscription> list : subscriptions.values()) {
            list.removeIf(s -> s.moduleId.equals(moduleId));
        }
    }

    @Override
    public boolean isRegistered(String moduleId) {
        return moduleId != null && modules.contains(moduleId);
    }

    @Override
    public void subscribe(String moduleId, String topic, MessageListener listener) {
        if (moduleId == null || topic == null || listener == null) return;
        CopyOnWriteArrayList<Subscription> list =
                subscriptions.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
        list.add(new Subscription(moduleId, listener));
    }

    @Override
    public void unsubscribe(String moduleId, String topic) {
        if (moduleId == null || topic == null) return;
        CopyOnWriteArrayList<Subscription> list = subscriptions.get(topic);
        if (list == null) return;
        list.removeIf(s -> s.moduleId.equals(moduleId));
        if (list.isEmpty()) subscriptions.remove(topic);
    }

    @Override
    public void send(String from, String to, String topic, Object payload) {
        if (to == null || topic == null) return;
        CopyOnWriteArrayList<Subscription> list = subscriptions.get(topic);
        if (list == null) return;

        for (Subscription s : list) {
            if (s.moduleId.equals(to)) {
                try {
                    s.listener.onMessage(from, topic, payload);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public void notify(String from, String topic, Object payload) {
        if (topic == null) return;
        CopyOnWriteArrayList<Subscription> list = subscriptions.get(topic);
        if (list == null) return;

        for (Subscription s : list) {
            try {
                s.listener.onMessage(from, topic, payload);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void release() {
        subscriptions.clear();
        modules.clear();
    }
}
