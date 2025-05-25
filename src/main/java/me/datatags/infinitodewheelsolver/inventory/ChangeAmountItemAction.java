package me.datatags.infinitodewheelsolver.inventory;

import com.prineside.tdi2.ItemStack;
import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

public class ChangeAmountItemAction implements ItemAction {
    private final ItemStack stack;
    private final int oldAmount;
    private final int newAmount;
    public ChangeAmountItemAction(ItemStack stack, int newAmount) {
        this.stack = stack;
        this.oldAmount = stack.getCount();
        this.newAmount = newAmount;
    }

    @Override
    public void perform(ChangeTrackingPP_Inventory inventory) {
        stack.setCount(newAmount);
    }

    @Override
    public void revert(ChangeTrackingPP_Inventory inventory) {
        stack.setCount(oldAmount);
    }
}
