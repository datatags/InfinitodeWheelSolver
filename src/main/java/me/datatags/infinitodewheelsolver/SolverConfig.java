package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;

import java.util.Map;

public class SolverConfig {
    private final Map<Item, Double> itemWeights;
    private final int ticketsToUse;
    private final int accelsToUse;
    private final int maxResults;
    private final int minTickets;
    private final int minAccels;

    public SolverConfig(Map<Item, Double> itemWeights, int ticketsToUse, int accelsToUse, int maxResults) {
        this.itemWeights = itemWeights;
        this.ticketsToUse = ticketsToUse;
        this.accelsToUse = accelsToUse;
        this.maxResults = maxResults;
        // Can't be calculated on the fly because the ticket count changes as we go
        this.minTickets = ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN) - ticketsToUse;
        this.minAccels = ProgressPrefs.i().inventory.getItemsCount(Item.D.ACCELERATOR) - accelsToUse;
    }

    public Map<Item, Double> getItemWeights() {
        return itemWeights;
    }

    public int getTicketsToUse() {
        return ticketsToUse;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public int getMinTickets() {
        return minTickets;
    }

    public int getMinAccels() {
        return minAccels;
    }
}
