package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Config;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

import static me.datatags.infinitodewheelsolver.ReflectionUtils.setFieldValue;

public class RecursiveWheelSolver extends WheelSolver {
    public RecursiveWheelSolver(SolverConfig config) {
        super(config);

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
    public void solve() {
        WheelWrapper wheel = new WheelWrapper();
        doStep(wheel, false, new PathResult());
        System.out.println("Halfway done! (approximately)");
        doStep(wheel, true, new PathResult());
    }

    public void doStep(WheelWrapper wheel, boolean step, PathResult result) {
        ProgressState state = new ProgressState();
        ((ChangeTrackingPP_Inventory) ProgressPrefs.i().inventory).snapshot();

        doWheelAction(wheel, step, result);

        int ticketCount = ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN) - config.getMinTickets();
        int accelCount = Game.i.progressManager.getAccelerators() - config.getMinAccels();
        int ticketCost = wheel.getRespinTicketCost();
        int accelCost = wheel.getRespinAcceleratorCost();
        if (ticketCost == 0) ticketCost = Integer.MAX_VALUE;
        if (accelCost == 0) accelCost = Integer.MAX_VALUE;
        boolean canRespin;
        if (config.isRespinUsingTickets()) {
            canRespin = ticketCount >= ticketCost;
        } else {
            canRespin = accelCount >= accelCost;
        }

        // Don't bother respinning on the last wheel if it doesn't have anything good
        boolean doRespin = canRespin && (hasDesirableItems(wheel) || ticketCount > ticketCost);
        boolean doNewWheel = ticketCount > 0;

        if (doRespin) {
            doStep(wheel, false, result.copy());
        }

        if (doNewWheel) {
            doStep(wheel, true, result.copy());
        }

        if (!doRespin && !doNewWheel) { // leaf node
            // No choices left, trim and save results
            result.trim();
            if (!result.getPathSteps().isEmpty()) {
                results.add(PackedResult.fromPathResult(result, this));
            }
            if (++totalResults % 100000 == 0) {
                System.out.println("Explored " + totalResults + " paths so far...");
            }
        }

        ((ChangeTrackingPP_Inventory) ProgressPrefs.i().inventory).rollback();
        state.apply();
        wheel.rebuild();
    }
}
