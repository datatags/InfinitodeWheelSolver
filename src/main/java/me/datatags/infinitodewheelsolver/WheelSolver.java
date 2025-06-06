package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.ItemStack;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public abstract class WheelSolver {
    protected final SolverConfig config;
    protected final BoundedPriorityQueue<PackedResult> results;
    protected int totalResults = 0;

    public WheelSolver(SolverConfig config) {
        this.config = config;
        this.results = new BoundedPriorityQueue<>(config.getMaxResults());
    }

    public double calculateScore(PathResult pathResult) {
        double accelWeight = config.getItemWeights().getOrDefault(Item.D.ACCELERATOR, 0.0);
        double ticketWeight = config.getItemWeights().getOrDefault(Item.D.LUCKY_SHOT_TOKEN, 0.0);
        double itemScore = 0;
        for (PathStep step : pathResult.getPathSteps()) {
            ItemStack stack = step.getReward();
            itemScore += config.getItemWeights().getOrDefault(stack.getItem(), 0.0) * stack.getCount();
            itemScore -= step.getAccelCost() * accelWeight + step.getTicketCost() * ticketWeight;
        }

        return itemScore;
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

    protected void doWheelAction(WheelWrapper wheel, boolean action, PathResult result) {
        int accelCost = 0;
        int ticketCost = 0;
        if (action) {
            wheel.buyNew();
            ticketCost = 1;
        } else {
            if (config.isRespinUsingTickets()) {
                ticketCost = wheel.buyRespin(true);
            } else {
                accelCost = wheel.buyRespin(false);
            }
        }
        ItemStack stack = wheel.spin().item;
        result.addStep(new PathStep(action, accelCost, ticketCost, stack), isDesirableItem(stack.getItem()));
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
