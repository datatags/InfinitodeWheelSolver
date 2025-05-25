package me.datatags.infinitodewheelsolver.derived;

import com.badlogic.gdx.utils.Array;
import com.prineside.tdi2.Item;
import com.prineside.tdi2.ItemStack;
import com.prineside.tdi2.managers.preferences.PrefMap;
import com.prineside.tdi2.managers.preferences.categories.progress.PP_Inventory;
import me.datatags.infinitodewheelsolver.inventory.AddItemAction;
import me.datatags.infinitodewheelsolver.inventory.ChangeAmountItemAction;
import me.datatags.infinitodewheelsolver.inventory.InventoryIncrement;
import me.datatags.infinitodewheelsolver.inventory.ItemAction;
import me.datatags.infinitodewheelsolver.inventory.RemoveItemAction;

import java.util.Stack;

import static me.datatags.infinitodewheelsolver.ReflectionUtils.createWithoutConstructor;

/**
 * Class to track changes to its items.
 * This implementation probably won't work in the general case, but it should be fine for this specific purpose where
 * there is no intersection between items that can be added and items that can be removed.
 */
public class ChangeTrackingPP_Inventory extends PP_Inventory {
    protected boolean initializing = false;
    protected Stack<InventoryIncrement> increments = new Stack<>();

    @Override
    public synchronized void load(PrefMap prefs) {
        initializing = true;
        super.load(prefs);
        initializing = false;
        increments.clear();
        snapshot();
    }

    public synchronized void addItemToIndex(ItemStack stack) {
        getItemsByType(stack.getItem().getType()).add(stack);
        getItemsByCategory(stack.getItem().getCategory()).add(stack);
        getItemsBySubcategory(stack.getItem().getSubcategory()).add(stack);
    }

    public synchronized void removeItemFromIndex(ItemStack stack) {
        getItemsByType(stack.getItem().getType()).removeValue(stack, true);
        getItemsByCategory(stack.getItem().getCategory()).removeValue(stack, true);
        getItemsBySubcategory(stack.getItem().getSubcategory()).removeValue(stack, true);
    }

    @Override
    public synchronized void addItems(Item item, int amount) {
        if (initializing) {
            super.addItems(item, amount);
            return;
        }
        Array<ItemStack> byType = getItemsByType(item.getType());
        ItemAction action = null;
        for (ItemStack stack : byType) {
            if (stack.getItem().sameAs(item)) {
                action = new ChangeAmountItemAction(stack, stack.getCount() + amount);
                break;
            }
        }
        if (action == null) {
            action = new AddItemAction(new ItemStack(item, amount));
        }
        action.perform(this);
        increments.peek().actions.push(action);
    }

    @Override
    public synchronized ItemRemoveResult removeItems(Item item, int amount) {
        if (initializing) {
            return super.removeItems(item, amount);
        }
        ItemRemoveResult result = createWithoutConstructor(ItemRemoveResult.class);
        result.removedRequiredAmount = false;
        Array<ItemStack> byType = getItemsByType(item.getType());
        if (item instanceof Item.UsableItem && ((Item.UsableItem)item).autoUseWhenAdded()) {
            for (ItemAction action : increments.peek().actions) {
                if (action instanceof AddItemAction && ((AddItemAction)action).preempt(amount)) {
                    result.removedRequiredAmount = true;
                    // This might be wrong but I'm pretty sure this isn't important
                    result.remainingCount = ((AddItemAction) action).getPreemptsRemaining();
                    return result;
                }
            }
            throw new IllegalStateException("Tried to remove an auto use item that we don't know about!");
        }
        for (ItemStack stack : byType) {
            if (stack.getItem().sameAs(item)) {
                ItemAction action;
                if (stack.getCount() > amount) {
                    action = new ChangeAmountItemAction(stack, stack.getCount() - amount);
                    result.removedRequiredAmount = true;
                    result.remainingCount = stack.getCount();
                } else if (stack.getCount() == amount) {
                    action = new RemoveItemAction(stack);
                    result.removedRequiredAmount = true;
                    result.remainingCount = 0;
                } else {
                    break;
                }
                action.perform(this);
                increments.peek().actions.push(action);
                break;
            }
        }
        return result;
    }

    public void snapshot() {
        increments.push(new InventoryIncrement());
    }

    public void rollback() {
        InventoryIncrement increment = increments.pop();
        while (!increment.actions.empty()) {
            increment.actions.pop().revert(this);
        }
    }
}
