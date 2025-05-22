package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathResult {
    private final List<Boolean> path;
    private int acceleratorCost = 0;
    private final Map<Item, Integer> rewards = new HashMap<>();

    public PathResult(List<Boolean> path) {
        this.path = path;
    }

    public PathResult() {
        this(new ArrayList<>());
    }

    public void addAcceleratorCost(int cost) {
        acceleratorCost += cost;
    }

    public int getAcceleratorCost() {
        return acceleratorCost;
    }

    public List<Boolean> getPath() {
        return path;
    }

    public void addStep(boolean step) {
        path.add(step);
    }

    public Map<Item, Integer> getRewards() {
        return rewards;
    }

    public void addReward(ItemStack reward) {
        rewards.merge(reward.getItem(), reward.getCount(), Integer::sum);
    }

    public void print(WheelSolver solver) {
        System.out.print(solver.calculateScore(this) + ": [");
        for (boolean step : path) {
            System.out.print(step ? 'N' : 'R');
        }
        System.out.print("] ");
        // Print the desirable items first, followed by the others
        boolean first = true;
        for (Map.Entry<Item, Integer> entry : rewards.entrySet()) {
            if (solver.isDesirableItem(entry.getKey())) {
                if (first) {
                    first = false;
                } else {
                    System.out.print(", ");
                }
                System.out.print(entry.getValue() + "x " + entry.getKey().getTitle());
            }
        }
        first = true;
        System.out.print(" (");
        for (Map.Entry<Item, Integer> entry : rewards.entrySet()) {
            if (!solver.isDesirableItem(entry.getKey())) {
                if (first) {
                    first = false;
                } else {
                    System.out.print(", ");
                }
                System.out.print(entry.getValue() + "x " + entry.getKey().getTitle());
            }
        }
        System.out.println(")");
    }
}
