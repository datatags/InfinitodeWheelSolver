package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.Item;
import com.prineside.tdi2.managers.preferences.categories.ProgressPrefs;

import java.util.HashMap;
import java.util.Map;

public class SolverConfig {
    private final Map<Item, Double> itemWeights = new HashMap<>();
    private int ticketsToUse;
    private int accelsToUse;
    private int maxResults = 1000;
    private int minTickets;
    private int minAccels;
    private boolean respinUsingTickets = false;

    private static int getRealTicketCount() {
        return ProgressPrefs.i().inventory.getItemsCount(Item.D.LUCKY_SHOT_TOKEN);
    }

    private static int getRealAccelCount() {
        return ProgressPrefs.i().inventory.getItemsCount(Item.D.ACCELERATOR);
    }

    public SolverConfig() {
        ticketsToUse = getRealTicketCount();
        accelsToUse = getRealAccelCount();
        updateCalculated();
    }

    private void updateCalculated() {
        // Can't be calculated on the fly because the ticket count changes as we go
        minTickets = getRealTicketCount() - ticketsToUse;
        minAccels = getRealAccelCount() - accelsToUse;
    }

    /**
     * Scores are assigned to results as the quantity of each item multiplied by its weight, minus the weights of spent accelerators and lucky tickets.
     * Make sure to assign the weights of accelerators and lucky tickets!
     *
     * @param item   The item to set the weight of.
     * @param weight The weight to set.
     * @return this
     */
    public SolverConfig withItemWeight(Item item, double weight) {
        itemWeights.put(item, weight);
        return this;
    }

    /**
     * Set the number of tickets to use when exploring paths. The processing time required increases very quickly as this increases. 10 is a good number to start with.
     *
     * @param ticketsToUse The number of tickets to use. Pass {@link Integer#MAX_VALUE} to use all tickets.
     * @return this
     */
    public SolverConfig withTickets(int ticketsToUse) {
        this.ticketsToUse = Integer.min(ticketsToUse, getRealTicketCount());
        updateCalculated();
        return this;
    }

    /**
     * Set the maximum number of accelerators to use when exploring paths.
     * The processing time required increases as this increases, but the exact relation is complex.
     * This has no effect if {@link #withTicketsToRespin()} is enabled.
     *
     * @param accelsToUse The number of accelerators to use. Pass {@link Integer#MAX_VALUE} to use all accelerators.
     * @return this
     */
    public SolverConfig withAccels(int accelsToUse) {
        this.accelsToUse = Integer.min(accelsToUse, getRealAccelCount());
        updateCalculated();
        return this;
    }

    /**
     * Set the number of top results to keep. Each result is about 100 bytes of memory required, so 10,000,000 would require around 1GB of memory.
     *
     * @param maxResults The number of top results to keep.
     * @return this
     */
    public SolverConfig withMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Set to purchase respins using tickets instead of accelerators.
     *
     * @return this
     */
    public SolverConfig withTicketsToRespin() {
        return withTicketsToRespin(true);
    }

    /**
     * Set whether to purchase respins using tickets instead of accelerators.
     *
     * @return this
     */
    public SolverConfig withTicketsToRespin(boolean ticketsToRespin) {
        this.respinUsingTickets = true;
        return this;
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

    public boolean isRespinUsingTickets() {
        return respinUsingTickets;
    }
}
