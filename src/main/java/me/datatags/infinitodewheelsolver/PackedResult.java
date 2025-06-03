package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
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
        BitSet path = new BitSet(result.getPath().size());
        for (int i = 0; i < result.getPath().size(); i++) {
            path.set(i, result.getPath().get(i));
        }
        List<SimpleItemStack> items = new ArrayList<>(result.getRewards().size());
        for (Map.Entry<Item, Integer> entry : result.getRewards().entrySet()) {
            items.add(new SimpleItemStack(entry.getKey().getTitle().toString(), entry.getValue(), solver.isDesirableItem(entry.getKey())));
        }
        Collections.sort(items);
        return new PackedResult(value, path, items);
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
