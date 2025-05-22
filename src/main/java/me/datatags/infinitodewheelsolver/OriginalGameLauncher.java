package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.NormalGame;
import com.prineside.tdi2.events.global.GameLoad;
import me.datatags.infinitodewheelsolver.derived.NullPurchaseManager;

public class OriginalGameLauncher {
    public static void main(String[] args) {
        new Lwjgl3Application(new NormalGame(new ModifiedActionResolver(), () -> {
            Game.EVENTS.getListeners(GameLoad.class).add(e -> {
                NormalGame.i.purchaseManager.purchaseManager = new NullPurchaseManager();
            });
        }), new Lwjgl3ApplicationConfiguration());
    }
}
