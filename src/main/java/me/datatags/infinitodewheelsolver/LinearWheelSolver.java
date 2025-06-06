package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.Gdx;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.PreferencesManager;
import com.prineside.tdi2.managers.preferences.RegularPrefMap;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import me.datatags.infinitodewheelsolver.exceptions.NotEnoughResourcesException;
import me.datatags.infinitodewheelsolver.inventory.InventoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LinearWheelSolver extends WheelSolver {
    private final Stack<List<Boolean>> choices = new Stack<>();
    private final RegularPrefMap progressData;
    private final InventoryData inventoryData;

    public LinearWheelSolver(SolverConfig config) {
        super(config);
        progressData = new RegularPrefMap((byte) 1);
        byte[] data = Gdx.files.local(PreferencesManager.getProgressPrefsFilePath()).readBytes();
        progressData.fromBytes(data, 0, data.length);

        // Snapshot data
        this.inventoryData = new InventoryData();
    }

    /**
     * Reload inventory
     */
    public void resettiSpaghetti() {
        ProgressPrefs.i().progress.load(progressData);
        inventoryData.restore();
    }

    @Override
    public void solve() {
        choices.clear();
        choices.push(new ArrayList<>());
        while (!choices.empty()) {
            if (totalResults % 1000 == 0) {
                System.out.println("Explored " + totalResults + " paths so far...");
            }
            List<Boolean> currentChoices = choices.pop();
            resettiSpaghetti();
            PathResult result = new PathResult();
            WheelWrapper wheel = new WheelWrapper();
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
                    // Save results
                    results.add(PackedResult.fromPathResult(result, this));
                    break;
                }
                // result already has a reference to this currentChoices so we don't need to update it manually
                currentChoices.add(newChoice);
                doWheelAction(wheel, newChoice, result);
            }
        }
    }
}
