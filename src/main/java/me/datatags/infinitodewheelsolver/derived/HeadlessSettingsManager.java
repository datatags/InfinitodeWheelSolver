package me.datatags.infinitodewheelsolver.derived;

import com.prineside.tdi2.Config;
import com.prineside.tdi2.managers.SettingsManager;

public class HeadlessSettingsManager extends SettingsManager {
    @Override
    public void setup() {
        // Nothing useful happens
    }

    @Override
    public boolean getBoolCustomValue(CustomValueType type) {
        boolean result;
        try {
            Config.IS_HEADLESS = false;
            result = super.getBoolCustomValue(type);
        } finally {
            Config.IS_HEADLESS = true;
        }
        return result;
    }

    @Override
    public double getCustomValue(CustomValueType type) {
        double result;
        try {
            Config.IS_HEADLESS = false;
            result = super.getCustomValue(type);
        } finally {
            Config.IS_HEADLESS = true;
        }
        return result;
    }

    @Override
    public boolean isBugReportsEnabled() {
        return false;
    }
}
