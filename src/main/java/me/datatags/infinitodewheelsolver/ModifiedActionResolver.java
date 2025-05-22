package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.prineside.tdi2.ActionResolver;
import com.prineside.tdi2.utils.FileChooser;
import com.prineside.tdi2.utils.logging.PlatformLogger;
import com.prineside.tdi2.utils.logging.SystemOutPlatformLogger;

public class ModifiedActionResolver extends ActionResolver.ActionResolverAdapter {
    private final FileHandle handle = new Lwjgl3FileHandle("log.txt", Files.FileType.Local);
    private final PlatformLogger logger = new SystemOutPlatformLogger(true, true);

    @Override
    public boolean isAppModified() {
        return true;
    }

    @Override
    public String getAppModifiedInfo() {
        return "Wheel poking";
    }

    @Override
    public FileHandle getLogFile() {
        return handle;
    }

    @Override
    public PlatformLogger createPlatformLogger() {
        return logger;
    }

    @Override
    public ObjectMap<String, String> getDeviceInfo() {
        return new ObjectMap<>();
    }

    @Override
    public String getShortDeviceInfo() {
        return "";
    }

    @Override
    public String glGetStringi(int i, int i1) {
        return "";
    }

    @Override
    public FileChooser getFileChooser() {
        return null;
    }
}
