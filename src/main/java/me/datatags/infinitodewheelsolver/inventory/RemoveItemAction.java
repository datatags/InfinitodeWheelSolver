package me.datatags.infinitodewheelsolver.inventory;

import com.prineside.tdi2.ItemStack;
import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

public class RemoveItemAction implements ItemAction {
    private final ItemStack stack;
    private int index;
    public RemoveItemAction(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void perform(ChangeTrackingPP_Inventory inventory) {
        index = inventory.getAllItems().indexOf(stack, true);
        inventory.getAllItems().removeIndex(index);
        inventory.removeItemFromIndex(stack);
    }

    @Override
    public void revert(ChangeTrackingPP_Inventory inventory) {
        inventory.getAllItems().insert(index, stack);
        inventory.addItemToIndex(stack);
    }
}
