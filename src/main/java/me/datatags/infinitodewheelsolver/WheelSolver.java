package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public abstract class WheelSolver {
    protected final SolverConfig config;
    protected final BoundedPriorityQueue<PackedResult> results;
    protected int totalResults = 0;

    public WheelSolver(SolverConfig config) {
        this.config = config;
        this.results = new BoundedPriorityQueue<>(config.getMaxResults());
    }

    public double calculateScore(PathResult pathResult) {
        double itemScore = 0;

        for (Map.Entry<Item, Integer> entry : pathResult.getRewards().entrySet()) {
            itemScore += config.getItemWeights().getOrDefault(entry.getKey(), 0d) * entry.getValue();
        }

        return itemScore - pathResult.getAcceleratorCost();
    }

    public boolean hasDesirableItems(WheelWrapper wheel) {
        for (LuckyWheelOverlay.WheelOption option : wheel.getWheelOptions()) {
            if (option.item != null && isDesirableItem(option.item.getItem())) {
                return true;
            }
        }
        return false;
    }

    public boolean isDesirableItem(Item item) {
        return config.getItemWeights().getOrDefault(item, 0.0) > 0;
    }

    public void run() {
        results.clear();
        totalResults = 0;

        solve();

        System.out.println("Finished exploring possibilities, kept " + results.size() + " results (" + totalResults + " possibilities checked)");

        showResults();
        File outFile = new File("results-" + Instant.now().getEpochSecond() + ".txt");
        saveResults(outFile);
        System.out.println("Done! The " + config.getMaxResults() + " top results have been written to " + outFile.getAbsolutePath());
    }

    public void showResults() {
        System.out.println("Top 25 results:");
        int i = 0;
        for (PackedResult item : results) {
            if (i++ >= results.size() || i >= 25) {
                break;
            }
            System.out.println(item.buildDescription());
        }
    }

    public void saveResults(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (PackedResult result : results) {
                writer.write(result.buildDescription() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void solve();
}
