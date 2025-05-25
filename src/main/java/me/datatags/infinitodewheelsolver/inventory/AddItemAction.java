package me.datatags.infinitodewheelsolver.inventory;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.ItemStack;
import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

public class AddItemAction implements ItemAction {
    private final ItemStack stack;
    private final boolean canPreempt;
    private int preemptsRemaining = 0;
    public AddItemAction(ItemStack stack) {
        this.stack = stack;
        // If it's an item that is automatically used, it will be automatically removed from the inventory immediately
        // after being added, so we keep track of that too.
        // The loot ticket is the only one we have to worry about here btw
        this.canPreempt = stack.getItem() instanceof Item.UsableItem
                && ((Item.UsableItem)stack.getItem()).autoUseWhenAdded();
        this.preemptsRemaining = canPreempt ? stack.getCount() : 0;
    }

    public boolean preempt(int count) {
        if (preemptsRemaining < count) {
            return false;
        }
        preemptsRemaining -= count;
        return true;
    }

    public int getPreemptsRemaining() {
        return preemptsRemaining;
    }

    @Override
    public void perform(ChangeTrackingPP_Inventory inventory) {
        if (!canPreempt) {
            inventory.getAllItems().add(stack);
            inventory.addItemToIndex(stack);
        }
    }

    @Override
    public void revert(ChangeTrackingPP_Inventory inventory) {
        if (canPreempt) {
            if (preemptsRemaining != 0) {
                throw new IllegalStateException("Item with auto-use was never preemptively removed from the inventory!");
            }
            return;
        }
        ItemStack removed = inventory.getAllItems().pop();
        if (removed != stack) {
            throw new IllegalStateException("Item remove error: expected " + stack.toString() + ", got " + removed.toString());
        }
        inventory.removeItemFromIndex(stack);
    }
}
