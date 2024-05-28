package it.univr.xplg.generator.process;

import it.univr.xplg.generator.configuration.GenerationConfiguration;
import it.univr.xplg.generator.utils.TreeConverter;
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
        final TreeGenerator generator = new TreeGenerator(parameters.getMaxFanOut(), parameters.getMaxDepth());

        final DirectedAcyclicGraph<UUID, DefaultEdge> nestedXORRegion = generator.generateNestedXORRegion();
        final DirectedAcyclicGraph<UUID, DefaultEdge> independentXORRegion = generator.generateIndependentXORRegion();

        final DirectedAcyclicGraph<UUID, DefaultEdge> seseRegion = generator.attach(nestedXORRegion, independentXORRegion);

        final TreeConverter converter = new TreeConverter(seseRegion);
        converter.toBPMN("region.bpmn");

        Logger.instance().info("Process generation completed");
    }
}
