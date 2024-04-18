package it.univr.xplg.generator.process;

import it.univr.xplg.generator.configuration.GenerationConfiguration;
import it.univr.xplg.generator.utils.TreeGenerator;
import it.univr.xplg.model.ProcessWithImpacts;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import plg.model.Process;
import plg.utils.Logger;

import java.util.UUID;

public class RandomProcessGenerator extends plg.generator.process.ProcessGenerator {
    protected RandomProcessGenerator(Process process, GenerationConfiguration parameters) {
        super(process, parameters);
    }

    protected RandomProcessGenerator(ProcessWithImpacts process, GenerationConfiguration parameters) {
        super(process, parameters);
    }

    public static void randomizeProcess(Process process, GenerationConfiguration parameters) {
        new RandomProcessGenerator(process, parameters).begin(parameters);
    }

    public static void randomizeProcess(ProcessWithImpacts process, GenerationConfiguration parameters) {
        new RandomProcessGenerator(process, parameters).begin(parameters);
    }

    protected void begin(GenerationConfiguration parameters) {
        Logger.instance().info("Starting process generation");
        final TreeGenerator treeGenerator = new TreeGenerator(parameters.getMaxFanOut(), parameters.getMaxDepth());

        final DirectedAcyclicGraph<UUID, DefaultEdge> nestedXORs = generateNestedXORs(treeGenerator);

        Logger.instance().info("Process generation completed");
    }

    private DirectedAcyclicGraph<UUID, DefaultEdge> generateNestedXORs(TreeGenerator treeGenerator) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> seseIn = treeGenerator.generateRandomTree();
        final DirectedAcyclicGraph<UUID, DefaultEdge> seseOut = treeGenerator.reverseEdges(seseIn);

        return treeGenerator.mergeTrees(seseIn, seseOut);
    }
}
