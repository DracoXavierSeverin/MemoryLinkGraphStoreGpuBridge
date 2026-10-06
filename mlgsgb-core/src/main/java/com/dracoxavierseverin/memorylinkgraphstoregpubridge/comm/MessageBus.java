package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm;

public interface MessageBus {

    void registerModule(String moduleId);

    void unregisterModule(String moduleId);

    boolean isRegistered(String moduleId);

    void send(String from, String to, String topic, Object payload);

    void notify(String from, String topic, Object payload);

    void subscribe(String moduleId, String topic, MessageListener listener);

    void unsubscribe(String moduleId, String topic);

    void release();
}
