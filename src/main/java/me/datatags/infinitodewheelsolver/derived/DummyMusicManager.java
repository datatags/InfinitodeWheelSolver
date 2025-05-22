package me.datatags.infinitodewheelsolver.derived;

import com.prineside.tdi2.ibxm.Module;
import com.prineside.tdi2.managers.MusicManager;

public class DummyMusicManager extends MusicManager {
    @Override
    public void stopMusic() {
    }

    @Override
    protected void setBackendVolume(float v) {
    }

    @Override
    public void playMusic(Module module) {
    }

    @Override
    public Module getPlayingMusic() {
        return null;
    }
}
