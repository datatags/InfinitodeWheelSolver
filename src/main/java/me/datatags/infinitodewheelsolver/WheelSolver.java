package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.Gdx;
import com.prineside.tdi2.Config;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.PreferencesManager;
import com.prineside.tdi2.managers.preferences.RegularPrefMap;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;
import com.prineside.tdi2.utils.logging.LogLevel;
import me.datatags.infinitodewheelsolver.exceptions.NotEnoughResourcesException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class WheelSolver {
    private final SolverConfig config;
    private final List<SimpleResult> results = new ArrayList<>();
    private final Stack<List<Boolean>> choices = new Stack<>();
    private final RegularPrefMap progressData;
    private final InventoryData inventoryData;
    public WheelSolver(SolverConfig config) {
        this.config = config;
        progressData = new RegularPrefMap((byte)1);
        byte[] data = Gdx.files.local(PreferencesManager.getProgressPrefsFilePath()).readBytes();
        progressData.fromBytes(data, 0, data.length);

        // Make sure progress data is all restored from save before we snapshot it
        Config.IS_HEADLESS = false;
        ProgressPrefs.i().progress.load(progressData);
        ProgressPrefs.i().inventory.load(progressData);
        Config.IS_HEADLESS = true;
        this.inventoryData = new InventoryData();
    }

    public double calculateScore(PathResult pathResult) {
        double itemScore = 0;

        for (Map.Entry<Item, Integer> entry : pathResult.getRewards().entrySet()) {
            itemScore += config.getItemWeights().getOrDefault(entry.getKey(), 0d) * entry.getValue();
        }

        return itemScore - pathResult.getAcceleratorCost();
    }

    /**
     * Reload inventory
     */
    public void resettiSpaghetti() {
        ProgressPrefs.i().progress.load(progressData);
        inventoryData.restore();
    }

    protected void doWheelAction(WheelWrapper wheel, boolean action, PathResult result) {
        if (action) {
            wheel.buyNew();
        } else {
            result.addAcceleratorCost(wheel.buyRespin());
        }
        result.addReward(wheel.spin().item);
    }

    public boolean isDesirableItem(Item item) {
        return config.getItemWeights().getOrDefault(item, 0.0) > 0;
    }

    public boolean hasDesirableItems(WheelWrapper wheel) {
        for (LuckyWheelOverlay.WheelOption option : wheel.getWheelOptions()) {
            if (option.item != null && isDesirableItem(option.item.getItem())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDesirableItems(PathResult result) {
        return result.getRewards().keySet().stream().anyMatch(this::isDesirableItem);
    }

    public void run() {
        // info is too noisy
        LogLevel.setCurrent(LogLevel.WARNING);
        results.clear();
        choices.clear();
        choices.push(new ArrayList<>());
        int totalResults = 0;
        while (!choices.empty()) {
            if (totalResults % 1000 == 0) {
                System.out.println("Explored " + totalResults + " paths so far...");
            }
            List<Boolean> currentChoices = choices.pop();
            resettiSpaghetti();
            PathResult result = new PathResult(currentChoices);
            WheelWrapper wheel = new WheelWrapper();
            if (wheel.isSpinAvailable()) {
                result.addReward(wheel.spin().item);
            }
            try {
                for (boolean choice : currentChoices) {
                    doWheelAction(wheel, choice, result);
                }
            } catch (NotEnoughResourcesException | IllegalStateException exception) {
                // This isn't a valid path, ignore it and try the next one.
                continue;
            }
            // We've reached the end of our planned choices, now see if we can build the tree.
            while (true) {
                int ticketCount = ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN);
                boolean haveAccels = wheel.canBeRespun() && Game.i.progressManager.getAccelerators() >= wheel.getRespinAcceleratorCost();
                boolean haveTickets = ticketCount > config.getMinTickets();
                // Don't bother respinning on the last wheel if it doesn't have anything good
                boolean doRespin = haveAccels && (haveTickets || hasDesirableItems(wheel));
                boolean newChoice;
                // If we have two choices, follow "false" but add "true" as a future possibility
                if (doRespin && haveTickets) {
                    List<Boolean> alt = new ArrayList<>(currentChoices);
                    alt.add(true);
                    choices.push(alt);
                    newChoice = false;
                } else if (haveTickets) {
                    // If we just have one choice, do that one.
                    newChoice = true;
                } else if (doRespin) {
                    newChoice = false;
                } else {
                    totalResults++;
                    // No choices left, check whether we got any of the things we wanted.
                    if (hasDesirableItems(result)) {
                        results.add(result.toSimple(this));
                    }
                    break;
                }
                // result already has a reference to this currentChoices so we don't need to update it manually
                currentChoices.add(newChoice);
                doWheelAction(wheel, newChoice, result);
            }
        }
        System.out.println("Finished exploring possibilities, got " + results.size() + " useful results (" + totalResults + " total)");
        Collections.sort(results);
        System.out.println("These are the top 25:");
        for (int i = 0; i < 25; i++) {
            System.out.println(results.get(i));
        }
        File outFile = new File("results-" + Instant.now().getEpochSecond() + ".txt");
        System.out.println("Please wait while other paths are written to " + outFile.getAbsolutePath() + ", or press Ctrl-C now if you don't care");
        try (FileWriter writer = new FileWriter(outFile)) {
            for (SimpleResult result : results) {
                writer.write(result.toString() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Done!");
    }
}
