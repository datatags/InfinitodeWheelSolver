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

    public PathResult copy() {
        PathResult o = new PathResult(new ArrayList<>(path));
        o.acceleratorCost = acceleratorCost;
        o.rewards.putAll(rewards);
        return o;
    }
}
