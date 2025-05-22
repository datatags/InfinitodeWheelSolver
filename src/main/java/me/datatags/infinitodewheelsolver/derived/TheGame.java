package me.datatags.infinitodewheelsolver.derived;

import com.prineside.tdi2.Config;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.managers.AchievementManager;
import com.prineside.tdi2.managers.AnalyticsManager;
import com.prineside.tdi2.managers.AuthManager;
import com.prineside.tdi2.managers.LocaleManager;
import com.prineside.tdi2.managers.PreferencesManager;
import com.prineside.tdi2.managers.ProgressManager;
import com.prineside.tdi2.managers.PurchaseManager;
import com.prineside.tdi2.managers.ScreenManager;
import com.prineside.tdi2.managers.StatisticsManager;
import com.prineside.tdi2.utils.GameSyncLoader;
import me.datatags.infinitodewheelsolver.ModifiedActionResolver;

public class TheGame extends Game {
    private final Runnable onReady;
    public TheGame(Runnable onReady) {
        super(new ModifiedActionResolver(), null);
        this.onReady = onReady;
    }

    @Override
    public void create() {
        super.create();
        gameSyncLoader.addTask(new GameSyncLoader.Task("extras", this::loadExtras));
        if (onReady != null) {
            onReady.run();
        }
    }

    public void loadExtras() {
        // These don't normally exist in headless mode, but are required for certain things we need to do.
        preferencesManager = new PreferencesManager();
        screenManager = new ScreenManager();
        statisticsManager = new StatisticsManager();
        progressManager = new ProgressManager();
        purchaseManager = new PurchaseManager();
        authManager = new AuthManager();
        settingsManager = new HeadlessSettingsManager();
        analyticsManager = new AnalyticsManager();
        localeManager = new LocaleManager();
        musicManager = new DummyMusicManager();
        achievementManager = new AchievementManager();

        screenManager.setup();
        statisticsManager.setup();

        // Inventory won't fully load in headless mode
        Config.IS_HEADLESS = false;
        preferencesManager.setup();
        Config.IS_HEADLESS = true;

        progressManager.setup();
        purchaseManager.setup();
        authManager.setup();
        // Don't setup settingsManager, it tries to make HTTP requests
        // Don't setup analyticsManager, it's unnecessary
        localeManager.setup();
        achievementManager.setup();
    }
}
