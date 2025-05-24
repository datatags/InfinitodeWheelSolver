package me.datatags.infinitodewheelsolver;

public class SimpleResult implements Comparable<SimpleResult> {
    public final double value;
    public final String description;
    public SimpleResult(double value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int compareTo(SimpleResult o) {
        return Double.compare(o.value, this.value);
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
