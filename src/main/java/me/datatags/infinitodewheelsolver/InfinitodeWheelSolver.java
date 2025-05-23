package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.prineside.tdi2.Config;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;
import me.datatags.infinitodewheelsolver.derived.TheGame;

import java.util.HashMap;
import java.util.Map;

public class InfinitodeWheelSolver {
    private static Game game;
    public static void main(String[] args) {
        Config.IS_HEADLESS = true;
        Gdx.files = new HeadlessFiles();
        game = new TheGame(InfinitodeWheelSolver::run);
        Gdx.app = new HeadlessApplication(game);
    }

    public static void run() {
        // The first is required for our hackery, the second is a check to make sure our loadExtras function ran.
        while (game.itemManager == null || game.localeManager == null) {
            game.gameSyncLoader.iterate();
        }

        WheelWrapper wheel = new WheelWrapper();
        System.out.println("We're in! Dumping wheel:");
        wheel.dump();
        for (int i = 0; i < 6; i++) {
            System.out.println("Spin # " + (i + 1));
            if (i == 3 || !wheel.canBeRespun()) {
                System.out.println("Buying new wheel");
                wheel.buyNew();
                wheel.dump();
            } else {
                wheel.buyRespin();
            }
            LuckyWheelOverlay.WheelOption option = wheel.spin();
            System.out.println("Got " + option.item.getCount() + "x " + option.item.getItem().getTitle());
            wheel.dump();
        }

        for (int i = 0; i < 10; i++) {
            System.out.println();
        }
        System.out.println("Test finished, running solver");

        // Define what items you want and how much you want them.
        // Each accelerator spent is -1, so essentially you're assigning an accelerator cost to each item.
        Map<Item, Double> itemWeights = new HashMap<>();
        itemWeights.put(Item.D.RESEARCH_TOKEN, 100d);
        SolverConfig config = new SolverConfig(itemWeights, 6);

        WheelSolver solver = new WheelSolver(config);
        solver.run();
        System.out.println("Solver finished");

        Gdx.app.exit();
    }
}
