package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.utils.Array;
import com.prineside.tdi2.Game;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.ProgressManager;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import com.prineside.tdi2.managers.preferences.categories.SettingsPrefs;
import com.prineside.tdi2.scene2d.Group;
import com.prineside.tdi2.ui.shared.LuckyWheelOverlay;
import me.datatags.infinitodewheelsolver.exceptions.NotEnoughResourcesException;
import me.datatags.infinitodewheelsolver.exceptions.SpinAvailableException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static me.datatags.infinitodewheelsolver.ReflectionUtils.createWithoutConstructor;
import static me.datatags.infinitodewheelsolver.ReflectionUtils.getFieldValue;
import static me.datatags.infinitodewheelsolver.ReflectionUtils.invoke;
import static me.datatags.infinitodewheelsolver.ReflectionUtils.setFieldValue;

public class WheelWrapper {
    private static final Group group = new Group();
    private final LuckyWheelOverlay wheel;
    private final Array<LuckyWheelOverlay.WheelOptionConfig> options = new Array<>(LuckyWheelOverlay.WheelOptionConfig.class);

    public WheelWrapper() {
        wheel = createWithoutConstructor(LuckyWheelOverlay.class);
        // This is final and needs to be initialized, plus we get a reference to it this way
        setFieldValue(wheel, "z", options);
        setFieldValue(wheel, "q", group);
        // Ensure we can spin the wheel even if we recently loaded from cloud
        SettingsPrefs.i().auth.sessionData.lastLoadFromCloudTimestamp = 0;
    }

    /**
     * Spin the wheel. You must have a spin available ({@link WheelWrapper::isSpinAvailable()})
     * If the wheel lands on x2 or x3, it will be automatically respun.
     *
     * @return The item the wheel landed on.
     */
    public LuckyWheelOverlay.WheelOption spin() {
        return spin(true);
    }

    /**
     * Spin the wheel. You must have a spin available ({@link #isSpinAvailable()})
     *
     * @param respinOnMultiplier Whether the wheel should be automatically respun if the wheel lands on x2 or x3.
     * @return The option the wheel landed on.
     * @throws IllegalStateException if there isn't a spin available.
     */
    public LuckyWheelOverlay.WheelOption spin(boolean respinOnMultiplier) {
        if (!isSpinAvailable()) {
            // Restriction imposed by the game. Checking ahead of time lets us provide a helpful error message.
            throw new IllegalStateException("Spin not available, call buyRespin() or buyNew() first");
        }

        // All these fields and methods are obfuscated :)
        // Fields:
        // A: current wheel rotation
        // B: whether wheel is actively spinning
        // D: target wheel rotation
        // E: initial wheel rotation
        // F: target weapon rotation
        // G: initial weapon rotation
        // J: whether a respin of the wheel is being prepared
        // K: whether wheel is being prepared
        // M: current weapon rotation
        // Methods:
        // a(float): spin current wheel. parameter is how far the slider is pulled
        // a(float, float): calculate selected item index from weapon rotation and wheel rotation, respectively
        // a(WheelOptionConfig): finalize and give won items
        // d(): respin
        // e(): new wheel
        // f(): build respin UI. not called here (since we're headless) but handy for reference

        invoke(wheel, "a", "spin", 0f); // spin current wheel. speed doesn't matter

        rebuild();

        float wheelRot = getFieldValue(wheel, "D");
        float weaponRot = getFieldValue(wheel, "F");
        weaponRot = (weaponRot % 360 + 360) % 360;

        int targetIndex = invoke(wheel, "a", "get index", weaponRot, wheelRot); // get index of item we landed on
        Array<LuckyWheelOverlay.WheelOptionConfig> options = getFieldValue(wheel, "z");
        LuckyWheelOverlay.WheelOptionConfig option = options.get(targetIndex);
        option.wasHit = true;

        // Get the thing with the actual item info
        LuckyWheelOverlay.WheelOption item = getFieldValue(option, "a");

        try {
            // Do final spin processing, including issuing items
            Method finalProcessing = LuckyWheelOverlay.class.getDeclaredMethod("a", LuckyWheelOverlay.WheelOptionConfig.class);
            finalProcessing.setAccessible(true);
            finalProcessing.invoke(wheel, option);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // Items that can be auto-used throw an NPE due to the fact that they try to display a notification.
            // If we're trying to add such an item, ignore the exception, otherwise rethrow it for debugging.
            if (!(e.getCause() instanceof NullPointerException) || !(item.item.getItem() instanceof Item.UsableItem)
                    || !((Item.UsableItem) item.item.getItem()).autoUseWhenAdded()) {
                throw new RuntimeException(e);
            }
            // Otherwise, finish using the items like that method was supposed to do (it uses only one successfully)
            for (int i = 1; i < item.item.getCount(); i++) {
                ((Item.UsableItem) item.item.getItem()).useItem();
            }
        }

        if (item.wheelMultiplier != 0 && respinOnMultiplier) {
            // Hit an x2 or x3, respin automatically
            return spin();
        }
        return item;
    }

    /**
     * Get the options currently available on the wheel. Includes both items and multipliers.
     *
     * @return An Iterable of available options on the wheel.
     */
    public Iterable<LuckyWheelOverlay.WheelOption> getWheelOptions() {
        return Game.i.progressManager.getLuckyWheelOptions();
    }

    /**
     * Print current wheel options to the screen.
     */
    public void dump() {
        Array<LuckyWheelOverlay.WheelOption> options = Game.i.progressManager.getLuckyWheelOptions();
        for (LuckyWheelOverlay.WheelOption option : options) {
            System.out.println(option.toString());
        }
    }

    /**
     * Check whether the wheel can be spun immediately by calling {@link #spin()}.
     * If not, you can buy a respin using {@link #buyRespin(boolean)} or buy a whole new wheel with {@link #buyNew()}
     *
     * @return True if the wheel can be spun immediately.
     */
    public boolean isSpinAvailable() {
        return Game.i.progressManager.isLuckyWheelSpinAvailable();
    }

    /**
     * Check whether the wheel can be respun, not considering limited resources.
     *
     * @return True if the wheel can be respun, given infinite resources.
     */
    public boolean canBeRespun() {
        ProgressManager pm = Game.i.progressManager;
        int ticketCost = pm.getLuckyWheelRespinPriceTokens();
        int accelCost = pm.getLuckyWheelRespinPriceAccelerators();
        return ticketCost != 0 || accelCost != 0;
    }

    /**
     * Get the cost in accelerators to purchase a respin.
     * This returns 0 if the wheel cannot be respun, which may be due to the game deciding such, or may be because you
     * already have a spin available {@link #isSpinAvailable()}.
     *
     * @return The cost in accelerators, or 0 if a respin cannot be purchased at the moment.
     */
    public int getRespinAcceleratorCost() {
        return Game.i.progressManager.getLuckyWheelRespinPriceAccelerators();
    }

    /**
     * Get the cost in lucky tickets to purchase a respin.
     * This returns 0 if the wheel cannot be respun, which may be due to the game deciding such, or may be because you
     * already have a spin available {@link #isSpinAvailable()}.
     *
     * @return The cost in lucky tickets, or 0 if a respin cannot be purchased at the moment.
     */
    public int getRespinTicketCost() {
        return Game.i.progressManager.getLuckyWheelRespinPriceTokens();
    }

    /**
     * Purchase a respin of the existing wheel.
     * After successfully calling this method, {@link #isSpinAvailable()} will return true.
     *
     * @return The number of items spent to purchase the respin (either tickets or wheels).
     * @throws SpinAvailableException      if the wheel already has a spin available.
     * @throws IllegalStateException       if the wheel can no longer be respun.
     * @throws NotEnoughResourcesException if you don't have enough resources to purchase a respin.
     */
    public int buyRespin(boolean useTickets) {
        if (isSpinAvailable()) {
            throw new SpinAvailableException("A spin is available, you should spin first. (Logic error?)");
        }

        // Actually removing spent accelerators is important if you haven't maxed out the "bonus x2 chance"
        // research, because that research costs accelerators, and items needed to advance research are more likely
        // to appear on the wheel, meaning the wheel options selection can change depending on accelerator count.
        ProgressManager pm = Game.i.progressManager;
        int cost;
        if (useTickets) {
            cost = getRespinTicketCost();
            if (cost == 0) {
                throw new IllegalStateException("This wheel can't be respun using tickets anymore, use buyNew()");
            }
            if (pm.getItemsCount(Item.D.LUCKY_SHOT_TOKEN) < cost) {
                throw new NotEnoughResourcesException("You don't have enough tickets to buy a respin");
            }
            pm.removeItems(Item.D.LUCKY_SHOT_TOKEN, cost);
        } else {
            cost = getRespinAcceleratorCost();
            if (cost == 0) {
                throw new IllegalStateException("This wheel can't be respun using accelerators anymore, use buyNew()");
            }
            if (pm.getAccelerators() < cost) {
                throw new NotEnoughResourcesException("You don't have enough accelerators or tickets to buy a respin");
            }
            pm.removeAccelerators(cost);
        }

        // This is essentially the check that's used to decide whether we need to do the animation of removing a slice
        // from the wheel before respinning. If so, we let it do the math to update the wheel. Otherwise, we manually
        // set the wheel to be ready.
        boolean anyHit = false;
        for (LuckyWheelOverlay.WheelOptionConfig option : options) {
            if (option.wasHit) {
                anyHit = true;
                break;
            }
        }
        if (!anyHit) {
            rebuild();
            ProgressPrefs.i().progress.setLuckyWheelSpinAvailable(true);
        } else {
            invoke(wheel, "d", "respin"); // Game internal respin setup
        }
        return cost;
    }

    /**
     * Buy a new wheel. This costs one lucky ticket.
     * After successfully calling this method, {@link #isSpinAvailable()} will return true.
     *
     * @throws SpinAvailableException      if a spin is available on the current wheel.
     * @throws NotEnoughResourcesException if you don't have enough lucky tickets to purchase a new wheel.
     */
    public void buyNew() {
        // Theoretically e() does this but it also does a bunch of stuff we don't want.
        if (ProgressPrefs.i().progress.isLuckyWheelSpinAvailable()) {
            throw new SpinAvailableException("A spin is available, you should spin first. (Logic error?)");
        }
        ProgressManager pm = Game.i.progressManager;
        if (pm.getItemsCount(Item.D.LUCKY_SHOT_TOKEN) == 0) {
            throw new NotEnoughResourcesException("You don't have enough tickets to buy a new wheel.");
        }
        pm.removeItems(Item.D.LUCKY_SHOT_TOKEN, 1);
        Game.i.progressManager.generateNewLuckyWheel();
        ProgressPrefs.i().progress.setLuckyWheelSpinAvailable(true);
        rebuild();
    }

    /**
     * Rebuild the wheel options from whatever's currently in ProgressManager's wheel options.
     * You don't normally need to call this, it's called internally.
     */
    public void rebuild() {
        // Not all the fields are initialized so we get a NPE at some point in there. However, it doesn't matter
        // because by that point it's done everything we needed anyway.
        try {
            wheel.rebuild();
        } catch (NullPointerException ignored) {
        }

        // Avoids an NPE when we're finalizing the wheel result
        for (LuckyWheelOverlay.WheelOptionConfig option : options) {
            setFieldValue(option, "e", group);
        }
    }
}
