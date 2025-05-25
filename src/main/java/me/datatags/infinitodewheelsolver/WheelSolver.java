package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;

public interface WheelSolver {
    double calculateScore(PathResult pathResult);
    boolean isDesirableItem(Item item);
    void run();
}
