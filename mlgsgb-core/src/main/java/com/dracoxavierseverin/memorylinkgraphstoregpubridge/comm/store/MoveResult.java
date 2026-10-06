package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {

    public final int moved;
    public final int failed;
    public final List<String> failedTags;

    public MoveResult(int moved, int failed, List<String> failedTags) {
        this.moved = moved;
        this.failed = failed;
        this.failedTags = failedTags == null ? new ArrayList<>() : failedTags;
    }

    public boolean isAllSuccess() {
        return failed == 0;
    }

    @Override
    public String toString() {
        return "MoveResult{moved=" + moved + ", failed=" + failed
                + ", failedTags=" + failedTags + "}";
    }
}
