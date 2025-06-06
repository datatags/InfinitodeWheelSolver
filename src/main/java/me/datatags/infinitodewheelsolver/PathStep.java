package me.datatags.infinitodewheelsolver;

import com.prineside.tdi2.ItemStack;

public class PathStep {
    private final boolean action;
    private final int accelCost;
    private final int ticketCost;
    private final ItemStack reward;

    public PathStep(boolean action, int accelCost, int ticketCost, ItemStack reward) {
        this.action = action;
        this.accelCost = accelCost;
        this.ticketCost = ticketCost;
        this.reward = reward;
    }

    public boolean getAction() {
        return action;
    }

    public char getActionChar() {
        return action ? 'N' : 'R';
    }

    public int getAccelCost() {
        return accelCost;
    }

    public int getTicketCost() {
        return ticketCost;
    }

    public ItemStack getReward() {
        return reward;
    }
}
