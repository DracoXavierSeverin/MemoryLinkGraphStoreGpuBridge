package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

import java.util.ArrayList;
import java.util.List;

public class StoreMover {

    public MoveResult move(Store source, String sourceModule,
                           String[] tags,
                           Store target, String packName) {
        return move(source, sourceModule, tags, target, packName, false);
    }

    public MoveResult move(Store source, String sourceModule,
                           String[] tags,
                           Store target, String packName,
                           boolean deleteSource) {

        if (source == null || target == null
                || sourceModule == null || packName == null
                || tags == null || tags.length == 0) {
            return new MoveResult(0, tags == null ? 0 : tags.length, new ArrayList<>());
        }

        int moved = 0;
        int failed = 0;
        List<String> failedTags = new ArrayList<>();

        for (String tag : tags) {
            if (tag == null) {
                failed++;
                failedTags.add(null);
                continue;
            }

            Object content = source.get(sourceModule, tag);
            if (content == null && !source.contains(sourceModule, tag)) {
                failed++;
                failedTags.add(tag);
                continue;
            }

            boolean ok = target.put(packName, tag, content);
            if (ok) {
                moved++;
                if (deleteSource) {
                    source.remove(sourceModule, tag);
                }
            } else {
                failed++;
                failedTags.add(tag);
            }
        }

        return new MoveResult(moved, failed, failedTags);
    }
}
