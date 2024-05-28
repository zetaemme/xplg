package it.univr.xplg.generator.utils;

import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TreeConverter {
    private final DirectedAcyclicGraph<UUID, DefaultEdge> graph;
    private final Map<UUID, FlowElement> nodeMap = new HashMap<>();

    public TreeConverter(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        this.graph = graph;
    }

    public void toBPMN(final String filename) {
        final BpmnModel modelInstance = new BpmnModel();
        final Process process = new Process();
        process.setId("process");
        modelInstance.addProcess(process);

        final DepthFirstIterator<UUID, DefaultEdge> iterator = new DepthFirstIterator<>(graph);
        while (iterator.hasNext()) {
            final UUID nodeId = iterator.next();
            final NodeType nodeType = getNodeType(nodeId);

            final FlowElement flowNode = switch (nodeType) {
                case START -> new StartEvent();
                case END -> new EndEvent();
                case TASK -> new UserTask();
                case XOR -> new ExclusiveGateway();
            };

            final String id = switch (nodeType) {
                case START -> "start_" + shorten(nodeId);
                case END -> "end_" + shorten(nodeId);
                case TASK -> "task_" + shorten(nodeId);
                case XOR -> "gateway_" + shorten(nodeId);
            };

            flowNode.setId(id);

            if (flowNode instanceof UserTask) {
                flowNode.setName("Task " + shorten(nodeId));
            }

            process.addFlowElement(flowNode);
            nodeMap.put(nodeId, flowNode);
        }

        for (final DefaultEdge edge : graph.edgeSet()) {
            final UUID sourceId = graph.getEdgeSource(edge);
            final UUID targetId = graph.getEdgeTarget(edge);

            final FlowElement sourceNode = nodeMap.get(sourceId);
            final FlowElement targetNode = nodeMap.get(targetId);

            final SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId("sequence_" + shorten(sourceId) + "_" + shorten(targetId));
            sequenceFlow.setSourceRef(sourceNode.getId());
            sequenceFlow.setTargetRef(targetNode.getId());

            process.addFlowElement(sequenceFlow);
        }

        new BpmnAutoLayout(modelInstance).execute();

        final BpmnXMLConverter converter = new BpmnXMLConverter();
        final byte[] xmlBytes = converter.convertToXML(modelInstance);
        try {
            Files.write(Paths.get(filename), xmlBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NodeType getNodeType(final UUID node) {
        if (graph.inDegreeOf(node) == 0) {
            return NodeType.START;
        }

        if (graph.outDegreeOf(node) == 0) {
            return NodeType.END;
        }

        if (graph.inDegreeOf(node) == 1 && graph.outDegreeOf(node) == 1) {
            return NodeType.TASK;
        }

        return NodeType.XOR;
    }

    private String shorten(UUID uuid) {
        final String hash = Integer.toHexString(uuid.hashCode());
        return hash.substring(0, Math.min(8, hash.length()));
    }

    private enum NodeType {
        START,
        END,
        XOR,
        TASK
    }
}
