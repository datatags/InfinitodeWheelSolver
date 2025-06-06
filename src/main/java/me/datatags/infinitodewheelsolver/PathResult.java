package me.datatags.infinitodewheelsolver;

import java.util.ArrayList;
import java.util.List;

public class PathResult {
    private final List<PathStep> steps;
    private int lastDesiredItemIndex = -1;

    public PathResult(List<PathStep> steps) {
        this.steps = steps;
    }

    public PathResult() {
        this(new ArrayList<>());
    }

    public List<PathStep> getPathSteps() {
        return steps;
    }

    public void addStep(PathStep step, boolean isRewardDesired) {
        if (isRewardDesired) {
            lastDesiredItemIndex = steps.size();
        }
        steps.add(step);
    }

    public PathResult copy() {
        return new PathResult(new ArrayList<>(steps));
    }

    public PathStep peekLast() {
        return steps.get(steps.size() - 1);
    }

    public PathStep popLast() {
        return steps.remove(steps.size() - 1);
    }

    public void trim() {
        steps.subList(lastDesiredItemIndex + 1, steps.size()).clear();
    }
}
