package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Config;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;
import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.datatags.infinitodewheelsolver.ReflectionUtils.setFieldValue;

public class RecursiveWheelSolver implements WheelSolver {
    private final SolverConfig config;
    private final List<SimpleResult> results = new ArrayList<>();
    private int totalResults = 0;
    public RecursiveWheelSolver(SolverConfig config) {
        this.config = config;

        replaceInventoryManager();
        // Reload progress after replacing inventory manager
        Config.IS_HEADLESS = false;
        Game.i.preferencesManager.setup();
        Config.IS_HEADLESS = true;
    }

    private void replaceInventoryManager() {
        ProgressPrefs pp = ProgressPrefs.i();
        for (int i = 0; i < pp.all.length; i++) {
            if (pp.all[i] == pp.inventory) {
                setFieldValue(pp, "inventory", new ChangeTrackingPP_Inventory());
                pp.all[i] = pp.inventory;
                return;
            }
        }
        throw new IllegalStateException("Couldn't find inventory manager to replace");
    }

    @Override
    public double calculateScore(PathResult pathResult) {
        double itemScore = 0;

        for (Map.Entry<Item, Integer> entry : pathResult.getRewards().entrySet()) {
            itemScore += config.getItemWeights().getOrDefault(entry.getKey(), 0d) * entry.getValue();
        }

        return itemScore - pathResult.getAcceleratorCost();
    }

    protected void doWheelAction(WheelWrapper wheel, boolean action, PathResult result) {
        if (action) {
            wheel.buyNew();
        } else {
            result.addAcceleratorCost(wheel.buyRespin());
        }
        result.addStep(action);
        result.addReward(wheel.spin().item);
    }

    @Override
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

    @Override
    public void run() {
        results.clear();
        totalResults = 0;

        WheelWrapper wheel = new WheelWrapper();
        doStep(wheel, false, new PathResult());
        doStep(wheel, true, new PathResult());

        System.out.println("Finished exploring possibilities, got " + results.size() + " useful results (" + totalResults + " total)");
        Collections.sort(results);
        System.out.println("These are the top 25:");
        for (int i = 0; i < 25; i++) {
            if (i >= results.size()) {
                break;
            }
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

    public void doStep(WheelWrapper wheel, boolean step, PathResult result) {
        ProgressState state = new ProgressState();
        ((ChangeTrackingPP_Inventory)ProgressPrefs.i().inventory).snapshot();

        doWheelAction(wheel, step, result);

        int ticketCount = ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN);
        boolean haveAccels = wheel.canBeRespun() && Game.i.progressManager.getAccelerators() >= wheel.getRespinAcceleratorCost();
        boolean haveTickets = ticketCount > config.getMinTickets();
        // Don't bother respinning on the last wheel if it doesn't have anything good
        boolean doRespin = haveAccels && (haveTickets || hasDesirableItems(wheel));

        if (doRespin) {
            doStep(wheel, false, result.copy());
        }

        if (haveTickets) {
            doStep(wheel, true, result.copy());
        }

        if (!doRespin && !haveTickets) { // leaf node
            // No choices left, check whether we got any of the things we wanted.
            if (hasDesirableItems(result)) {
                results.add(result.toSimple(this));
            }
            if (++totalResults % 1000 == 0) {
                System.out.println("Explored " + totalResults + " paths so far...");
            }
        }

        ((ChangeTrackingPP_Inventory)ProgressPrefs.i().inventory).rollback();
        state.apply();
        wheel.rebuild();
    }
}
