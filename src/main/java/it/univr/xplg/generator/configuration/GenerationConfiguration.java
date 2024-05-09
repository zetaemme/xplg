package it.univr.xplg.generator.configuration;

public class GenerationConfiguration extends plg.generator.process.RandomizationConfiguration {
    private final int nestedXORBranches;
    private final int independentXORBranches;
    private final int maxFanOut;
    private final int maxDepth;

    public GenerationConfiguration(int ANDBranches, int nestedXORBranches, int independentXORBranches, double loopWeight, double singleActivityWeight, double skipWeight, double sequenceWeight, double ANDWeight, double XORWeight, int maxDepth, double dataObjectProbability, int maxFanOut) {
        super(ANDBranches, nestedXORBranches + independentXORBranches, loopWeight, singleActivityWeight, skipWeight, sequenceWeight, ANDWeight, XORWeight, maxDepth, dataObjectProbability);
        this.nestedXORBranches = nestedXORBranches;
        this.independentXORBranches = independentXORBranches;
        this.maxFanOut = maxFanOut;
        this.maxDepth = maxDepth;
    }

    public int getNestedXORBranches() {
        return nestedXORBranches;
    }

    public int getIndependentXORBranches() {
        return independentXORBranches;
    }

    public int getMaxFanOut() {
        return maxFanOut;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
