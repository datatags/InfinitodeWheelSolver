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
    public void solve() {
        WheelWrapper wheel = new WheelWrapper();
        doStep(wheel, false, new PathResult());
        doStep(wheel, true, new PathResult());
    }

    public void doStep(WheelWrapper wheel, boolean step, PathResult result) {
        ProgressState state = new ProgressState();
        ((ChangeTrackingPP_Inventory) ProgressPrefs.i().inventory).snapshot();

        doWheelAction(wheel, step, result);

        int ticketCount = ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN);
        boolean haveAccels = wheel.canBeRespun() && (Game.i.progressManager.getAccelerators() - config.getMinAccels()) >= wheel.getRespinAcceleratorCost();
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
            // No choices left, save results
            results.add(PackedResult.fromPathResult(result, this));
            if (++totalResults % 100000 == 0) {
                System.out.println("Explored " + totalResults + " paths so far...");
            }
        }

        ((ChangeTrackingPP_Inventory) ProgressPrefs.i().inventory).rollback();
        state.apply();
        wheel.rebuild();
    }
}
