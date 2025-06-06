package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.ItemStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Flattened storage of a PathResult. Most notably, does not keep references to any game objects.
 */
public class PackedResult implements Comparable<PackedResult> {
    public final double value;
    public final BitSet path;
    public final List<SimpleItemStack> items;

    public PackedResult(double value, BitSet path, List<SimpleItemStack> items) {
        this.value = value;
        this.path = path;
        this.items = items;
    }

    public static PackedResult fromPathResult(PathResult result, WheelSolver solver) {
        double value = solver.calculateScore(result);
        List<PathStep> steps = result.getPathSteps();
        BitSet path = new BitSet(steps.size());
        Map<Item, SimpleItemStack> items = new HashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            path.set(i, steps.get(i).getAction());
            ItemStack stack = steps.get(i).getReward();
            items.computeIfAbsent(stack.getItem(),
                    item -> new SimpleItemStack(item.getTitle().toString(), 0, solver.isDesirableItem(item))).addAmount(stack.getCount());
        }
        List<SimpleItemStack> simple = new ArrayList<>(items.values());
        Collections.sort(simple);
        return new PackedResult(value, path, simple);
    }

    // If a.value < b.value, then a < b.
    @Override
    public int compareTo(PackedResult o) {
        return Double.compare(this.value, o.value);
    }

    public String buildDescription() {
        StringBuilder result = new StringBuilder();
        result.append(value).append(": [");
        for (int i = 0; i < path.length(); i++) { // size() is NOT the number of bits we've set, length() is
            result.append(path.get(i) ? 'N' : 'R');
        }
        result.append("] ");
        StringJoiner good = new StringJoiner(", ");
        StringJoiner bad = new StringJoiner(", ");
        for (SimpleItemStack item : items) {
            if (item.desired) {
                good.add(item.toString());
            } else {
                bad.add(item.toString());
            }
        }

        result.append(good).append(" (").append(bad).append(")");
        return result.toString();
    }
}
