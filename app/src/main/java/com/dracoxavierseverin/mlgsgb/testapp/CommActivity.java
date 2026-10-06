package com.dracoxavierseverin.mlgsgb.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.MLGSGB;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.MessageBus;

public class CommActivity extends Activity {

    private TextView logView;
    private ScrollView scrollView;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("MLGSGB 通信演示");
        title.setTextSize(20);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Producer 每 500ms 向 Consumer 发送一条 ping");
        desc.setTextSize(14);
        desc.setPadding(0, 8, 0, 16);
        root.addView(desc);

        logView = new TextView(this);
        logView.setTextSize(14);

        scrollView = new ScrollView(this);
        scrollView.addView(logView);
        root.addView(scrollView);

        setContentView(root);

        startDemo();
    }

    private void startDemo() {
        MessageBus bus = MLGSGB.get().bus();
        bus.registerModule("producer");
        bus.registerModule("consumer");

        bus.subscribe("consumer", "ping", (from, topic, payload) -> {
            String line = "[" + topic + "] " + from + " → " + payload;
            uiHandler.post(() -> append(line));
        });

        Thread producerThread = new Thread(() -> {
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    count++;
                    bus.send("producer", "consumer", "ping", "第 " + count + " 条");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable t) {
                    uiHandler.post(() -> append("发送失败: " + t.getMessage()));
                    return;
                }
            }
        }, "producer-thread");
        producerThread.setDaemon(true);
        producerThread.start();

        append("=== 通信演示已启动 ===");
        append("producer → consumer (topic: ping)");
        append("");
    }

    private void append(String line) {
        CharSequence old = logView.getText();
        String next = (old == null || old.length() == 0) ? line : old + "\n" + line;
        logView.setText(next);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
