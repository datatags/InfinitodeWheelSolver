package me.datatags.infinitodewheelsolver.inventory;

import me.datatags.infinitodewheelsolver.derived.ChangeTrackingPP_Inventory;

public interface ItemAction {
    void perform(ChangeTrackingPP_Inventory inventory);
    void revert(ChangeTrackingPP_Inventory inventory);
}
