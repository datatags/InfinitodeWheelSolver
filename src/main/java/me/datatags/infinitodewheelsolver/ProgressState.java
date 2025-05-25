package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.utils.Array;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import com.prineside.tdi2.managers.preferences.categories.progress.PP_Progress;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;

public class ProgressState {
    private final long spinRandom0;
    private final long spinRandom1;
    private final long wheelRandom0;
    private final long wheelRandom1;
    private final float weaponAngle;
    private final float wheelRotation;
    private final int wheelMultiplier;
    private final boolean isWheelSpinning;
    private final boolean isWheelSpinAvailable;
    private final Array<LuckyWheelOverlay.WheelOption> wheelOptions;
    private final float lootBoostTime;

    public ProgressState() {
        PP_Progress p = ProgressPrefs.i().progress;
        spinRandom0 = p.getLuckyWheelSpinRandom().getState(0);
        spinRandom1 = p.getLuckyWheelSpinRandom().getState(1);
        wheelRandom0 = p.getLuckyWheelWheelRandom().getState(0);
        wheelRandom1 = p.getLuckyWheelWheelRandom().getState(1);
        weaponAngle = p.getLuckyWheelLastWeaponAngle();
        wheelRotation = p.getLuckyWheelLastRotation();
        wheelMultiplier = p.getLuckyWheelCurrentMultiplier();
        isWheelSpinning = p.isLuckyWheelSpinInProgress();
        isWheelSpinAvailable = p.isLuckyWheelSpinAvailable();
        wheelOptions = copy(p.getLuckyWheelOptions());
        // If we collect any loot tickets from the wheel, they are auto-used which we have to account for.
        // Loot tickets influence the core tile RNG
        lootBoostTime = p.getLootBoostTimeLeft();
    }

    public void apply() {
        PP_Progress p = ProgressPrefs.i().progress;
        p.getLuckyWheelSpinRandom().setState(spinRandom0, spinRandom1);
        p.getLuckyWheelWheelRandom().setState(wheelRandom0, wheelRandom1);
        p.setLuckyWheelLastWeaponAngle(weaponAngle);
        p.setLuckyWheelLastRotation(wheelRotation);
        p.setLuckyWheelCurrentMultiplier(wheelMultiplier);
        p.setLuckyWheelSpinInProgress(isWheelSpinning);
        p.setLuckyWheelSpinAvailable(isWheelSpinAvailable);
        p.setLuckyWheelOptions(copy(wheelOptions));
        p.setLootBoostTimeLeft(lootBoostTime);
    }

    public static Array<LuckyWheelOverlay.WheelOption> copy(Array<LuckyWheelOverlay.WheelOption> array) {
        Array<LuckyWheelOverlay.WheelOption> options = new Array<>(array.ordered, array.size, LuckyWheelOverlay.WheelOption.class);
        array.forEach(o -> options.add(copy(o)));
        return options;
    }

    public static LuckyWheelOverlay.WheelOption copy(LuckyWheelOverlay.WheelOption option) {
        return new LuckyWheelOverlay.WheelOption(option.item, option.chance, option.wheelMultiplier);
    }
}
