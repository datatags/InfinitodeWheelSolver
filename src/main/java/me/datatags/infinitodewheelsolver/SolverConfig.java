package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;

import java.util.Map;

public class SolverConfig {
    private final Map<Item, Double> itemWeights;
    private final int minTickets;
    public SolverConfig(Map<Item, Double> itemWeights, int minTickets) {
        this.itemWeights = itemWeights;
        this.minTickets = minTickets;
    }

    public Map<Item, Double> getItemWeights() {
        return itemWeights;
    }

    public int getMinTickets() {
        return minTickets;
    }
}
