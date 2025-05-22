package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Config;

public class Launcher {
    public static void main(String[] args) {
        try {
            Class.forName("com.prineside.tdi2.Game");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to hook Infinitode, please check that `infinitode-2.jar` is in the current folder.");
            System.exit(1);
        }

        // Infinitode rule 0: don't mess with the servers!
        // This isn't a foolproof way of avoiding external communication, other URLs exist in the code,
        // but this avoids the dangerous ones as far as I know.
        Config.SITE_URL = "";
        Config.AVATAR_WEB_TEXTURES_URL = "";

        if (args.length == 0) {
            InfinitodeWheelSolver.main(args);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("original")) {
            OriginalGameLauncher.main(args);
        } else {
            System.err.println("Unknown argument(s). Usage: InfinitodeWheelSolver.jar [original]");
            System.exit(1);
        }
    }
}
