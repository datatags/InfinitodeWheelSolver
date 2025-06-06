package me.datatags.infinitodewheelsolver;

public class SimpleItemStack implements Comparable<SimpleItemStack> {
    public final String name;
    public int amount;
    public final boolean desired;

    public SimpleItemStack(String name, int amount, boolean desired) {
        this.name = name;
        this.amount = amount;
        this.desired = desired;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    @Override
    public String toString() {
        return amount + "x " + name;
    }

    @Override
    public int compareTo(SimpleItemStack o) {
        return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
    }
}
