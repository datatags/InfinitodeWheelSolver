package me.datatags.infinitodewheelsolver;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.prineside.tdi2.ItemStack;
import com.prineside.tdi2.enums.ItemCategoryType;
import com.prineside.tdi2.enums.ItemSubcategoryType;
import com.prineside.tdi2.enums.ItemType;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;
import com.prineside.tdi2.managers.preferences.categories.progress.PP_Inventory;

import static me.datatags.infinitodewheelsolver.ReflectionUtils.getFieldValue;
import static me.datatags.infinitodewheelsolver.ReflectionUtils.setFieldValue;

/**
 * A class to assist with saving and loading game inventory data.
 */
public class InventoryData {
    private DelayedRemovalArray<ItemStack> items;
    private ObjectMap<ItemType, DelayedRemovalArray<ItemStack>> d;
    private ObjectMap<ItemCategoryType, DelayedRemovalArray<ItemStack>> e;
    private ObjectMap<ItemSubcategoryType, DelayedRemovalArray<ItemStack>> f;
    public InventoryData() {
        save();
    }

    /**
     * Save the current inventory data. This is called automatically on construction, so you only need to call it again
     * if you want to account for changes to the inventory.
     */
    public void save() {
        PP_Inventory inv = ProgressPrefs.i().inventory;
        items = copyArray(inv.getAllItems());
        d = copyMap(getFieldValue(inv, "d"));
        e = copyMap(getFieldValue(inv, "e"));
        f = copyMap(getFieldValue(inv, "f"));
    }

    public void restore() {
        PP_Inventory inv = ProgressPrefs.i().inventory;
        setFieldValue(inv, "b", copyArray(items));
        setFieldValue(inv, "d", copyMap(d));
        setFieldValue(inv, "e", copyMap(e));
        setFieldValue(inv, "f", copyMap(f));
    }

    private DelayedRemovalArray<ItemStack> copyArray(Array<ItemStack> array) {
        DelayedRemovalArray<ItemStack> newArray = new DelayedRemovalArray<>(array.ordered, array.size, ItemStack.class);
        array.forEach(i -> newArray.add(i.cpy()));
        return newArray;
    }

    private <T> ObjectMap<T,DelayedRemovalArray<ItemStack>> copyMap(ObjectMap<T,DelayedRemovalArray<ItemStack>> map) {
        ObjectMap<T,DelayedRemovalArray<ItemStack>> newMap = new ObjectMap<>(map.size);
        map.forEach(e -> newMap.put(e.key, copyArray(e.value)));
        return newMap;
    }
}
