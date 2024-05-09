package it.univr.xplg.generator.utils;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TreeGenerator {
    private final RandomDataGenerator randomDataGenerator;
    private final int fanOut;
    private int maxDepth;

    public TreeGenerator(int fanOut, int maxDepth) {
        this.randomDataGenerator = new RandomDataGenerator();
        this.fanOut = fanOut;
        this.maxDepth = maxDepth;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> generateRandomTree() {
        final DirectedAcyclicGraph<UUID, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        final UUID root = UUID.randomUUID();

        graph.addVertex(root);
        generateChildren(graph, root, 0);

        return graph;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> generateRandomTree(int maxDepth) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        final UUID root = UUID.randomUUID();

        final int previousMaxDepth = this.maxDepth;
        this.maxDepth = maxDepth;

        graph.addVertex(root);
        generateChildren(graph, root, 0);

        this.maxDepth = previousMaxDepth;

        return graph;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> generateRandomTree(int maxDepth, int minFanOut) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        final UUID root = UUID.randomUUID();

        final int previousMaxDepth = this.maxDepth;
        this.maxDepth = maxDepth;

        graph.addVertex(root);
        generateChildren(graph, root, 0, minFanOut);

        this.maxDepth = previousMaxDepth;

        return graph;
    }

    private void generateChildren(DirectedAcyclicGraph<UUID, DefaultEdge> graph, UUID parent, int depth) {
        if (depth >= maxDepth) {
            return;
        }

        int childrenCount = randomDataGenerator.nextInt(1, fanOut);
        for (int i = 0; i < childrenCount; i++) {
            final UUID child = UUID.randomUUID();
            graph.addVertex(child);
            graph.addEdge(parent, child);

            generateChildren(graph, child, depth + 1);
        }
    }

    private void generateChildren(DirectedAcyclicGraph<UUID, DefaultEdge> graph, UUID parent, int depth, int minFanOut) {
        if (depth >= maxDepth) {
            return;
        }

        int childrenCount = randomDataGenerator.nextInt(minFanOut, fanOut);
        for (int i = 0; i < childrenCount; i++) {
            final UUID child = UUID.randomUUID();
            graph.addVertex(child);
            graph.addEdge(parent, child);

            generateChildren(graph, child, depth + 1, minFanOut);
        }
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> reverseEdges(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> reversedGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (final UUID vertex : graph.vertexSet()) {
            reversedGraph.addVertex(vertex);
        }

        for (final DefaultEdge edge : graph.edgeSet()) {
            final UUID source = graph.getEdgeSource(edge);
            final UUID target = graph.getEdgeTarget(edge);
            reversedGraph.addEdge(target, source);
        }

        return this.updateUUIDs(reversedGraph);
    }

    private DirectedAcyclicGraph<UUID, DefaultEdge> updateUUIDs(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> updatedGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        final Map<UUID, UUID> updatedUUIDs = new HashMap<>();

        for (final UUID vertex : graph.vertexSet()) {
            if (graph.incomingEdgesOf(vertex).isEmpty()) {
                updatedGraph.addVertex(vertex);
            } else {
                final UUID newUUID = UUID.randomUUID();
                updatedUUIDs.put(vertex, newUUID);
                updatedGraph.addVertex(newUUID);
            }
        }

        for (final DefaultEdge edge : graph.edgeSet()) {
            final UUID source = graph.getEdgeSource(edge);
            final UUID target = graph.getEdgeTarget(edge);

            final UUID updatedSource = updatedUUIDs.getOrDefault(source, source);
            final UUID updatedTarget = updatedUUIDs.getOrDefault(target, target);

            updatedGraph.addEdge(updatedSource, updatedTarget);
        }

        return updatedGraph;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> clone(DirectedAcyclicGraph<UUID, DefaultEdge> originalGraph) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> copiedGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (final UUID vertex : originalGraph.vertexSet()) {
            copiedGraph.addVertex(vertex);
        }

        for (final DefaultEdge edge : originalGraph.edgeSet()) {
            copiedGraph.addEdge(originalGraph.getEdgeSource(edge), originalGraph.getEdgeTarget(edge));
        }

        return copiedGraph;
    }

    public List<UUID> getNodesWithZeroInDegree(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final List<UUID> nodesWithZeroInDegree = new ArrayList<>();

        for (final UUID node : graph.vertexSet()) {
            if (graph.incomingEdgesOf(node).isEmpty()) {
                nodesWithZeroInDegree.add(node);
            }
        }

        return nodesWithZeroInDegree;
    }

    public List<UUID> getNodesWithZeroOutDegree(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final List<UUID> nodesWithZeroOutDegree = new ArrayList<>();

        for (final UUID node : graph.vertexSet()) {
            if (graph.outgoingEdgesOf(node).isEmpty()) {
                nodesWithZeroOutDegree.add(node);
            }
        }

        return nodesWithZeroOutDegree;
    }

    private void addNodesAndEdgesWithNonZeroInDegree(
            DirectedAcyclicGraph<UUID, DefaultEdge> graph1,
            DirectedAcyclicGraph<UUID, DefaultEdge> graph2
    ) {
        for (final UUID vertex : graph2.vertexSet()) {
            if (!graph2.incomingEdgesOf(vertex).isEmpty()) {
                graph1.addVertex(vertex);
            }
        }

        for (final DefaultEdge edge : graph2.edgeSet()) {
            final UUID source = graph2.getEdgeSource(edge);
            if (!graph2.incomingEdgesOf(source).isEmpty()) {
                final UUID target = graph2.getEdgeTarget(edge);
                if (graph1.containsVertex(source) && graph1.containsVertex(target)) {
                    graph1.addEdge(source, target);
                }
            }
        }
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> copyGraphWithoutZeroInDegreeNodes(
            DirectedAcyclicGraph<UUID, DefaultEdge> originalGraph
    ) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> copiedGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (final UUID vertex : originalGraph.vertexSet()) {
            if (!originalGraph.incomingEdgesOf(vertex).isEmpty()) {
                copiedGraph.addVertex(vertex);
            }
        }

        for (final DefaultEdge edge : originalGraph.edgeSet()) {
            final UUID source = originalGraph.getEdgeSource(edge);
            final UUID target = originalGraph.getEdgeTarget(edge);
            if (copiedGraph.containsVertex(source) && copiedGraph.containsVertex(target)) {
                copiedGraph.addEdge(source, target);
            }
        }

        return copiedGraph;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> mergeTrees(
            DirectedAcyclicGraph<UUID, DefaultEdge> tree1,
            DirectedAcyclicGraph<UUID, DefaultEdge> tree2
    ) {
        final DirectedAcyclicGraph<UUID, DefaultEdge> mergedTree = this.clone(tree1);
        final DirectedAcyclicGraph<UUID, DefaultEdge> tree2Wrk = this.copyGraphWithoutZeroInDegreeNodes(tree2);

        final Map<UUID, List<UUID>> edgesToAdd = this.getNodesWithZeroInDegree(tree2Wrk).parallelStream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        node -> tree2.incomingEdgesOf(node).parallelStream()
                                .map(tree2::getEdgeSource)
                                .collect(Collectors.toList())
                ));

        this.addNodesAndEdgesWithNonZeroInDegree(mergedTree, tree2);

        for (final Map.Entry<UUID, List<UUID>> entry : edgesToAdd.entrySet()) {
            final UUID target = entry.getKey();
            for (final UUID source : entry.getValue()) {
                if (mergedTree.containsVertex(source) && mergedTree.containsVertex(target)) {
                    mergedTree.addEdge(source, target);
                }
            }
        }

        return mergedTree;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> mergeGraphs(
            DirectedAcyclicGraph<UUID, DefaultEdge> graph1,
            DirectedAcyclicGraph<UUID, DefaultEdge> graph2
    ) {
        DirectedAcyclicGraph<UUID, DefaultEdge> mergedGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        // Add all vertices and edges from the first graph
        for (final UUID vertex : graph1.vertexSet()) {
            if (!mergedGraph.containsVertex(vertex)) {
                mergedGraph.addVertex(vertex);
            }
        }

        for (final DefaultEdge edge : graph1.edgeSet()) {
            final UUID source = graph1.getEdgeSource(edge);
            final UUID target = graph1.getEdgeTarget(edge);
            if (!mergedGraph.containsEdge(source, target)) {
                mergedGraph.addEdge(source, target);
            }
        }

        // Add all vertices and edges from the second graph
        for (final UUID vertex : graph2.vertexSet()) {
            if (!mergedGraph.containsVertex(vertex)) {
                mergedGraph.addVertex(vertex);
            }
        }
        for (final DefaultEdge edge : graph2.edgeSet()) {
            final UUID source = graph2.getEdgeSource(edge);
            final UUID target = graph2.getEdgeTarget(edge);
            if (!mergedGraph.containsEdge(source, target)) {
                mergedGraph.addEdge(source, target);
            }
        }

        return mergedGraph;
    }

    // FIXME: This methods will be renamed to getTasks
    public List<UUID> getSeseNodes(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final List<UUID> seseNodes = new ArrayList<>();
        for (final UUID node : graph.vertexSet()) {
            if (graph.inDegreeOf(node) == 1 && graph.outDegreeOf(node) == 1) {
                seseNodes.add(node);
            }
        }

        return seseNodes;
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> expandTreeWithIndependentXOR(DirectedAcyclicGraph<UUID, DefaultEdge> graph) {
        final List<UUID> seseNodes = this.getSeseNodes(graph);
        final UUID selectedNode = seseNodes.get(randomDataGenerator.nextInt(0, seseNodes.size() - 1));

        final DirectedAcyclicGraph<UUID, DefaultEdge> independentXORIn = this.generateRandomTree(1, 2);
        final DirectedAcyclicGraph<UUID, DefaultEdge> independentXOROut = this.reverseEdges(independentXORIn);
        final DirectedAcyclicGraph<UUID, DefaultEdge> independentXOR = this.mergeTrees(independentXORIn, independentXOROut);

        return this.attachXORRegion(graph, independentXOR, selectedNode);
    }

    public DirectedAcyclicGraph<UUID, DefaultEdge> attachXORRegion(
            DirectedAcyclicGraph<UUID, DefaultEdge> process,
            DirectedAcyclicGraph<UUID, DefaultEdge> xor,
            UUID selectedNode
    ) {
        final UUID regionStart = this.getNodesWithZeroInDegree(xor).get(0);
        final UUID regionEnd = this.getNodesWithZeroOutDegree(xor).get(0);

        final DirectedAcyclicGraph<UUID, DefaultEdge> updatedGraph = this.mergeGraphs(process, xor);

        for (final UUID node : updatedGraph.vertexSet()) {
            if (node == selectedNode) {
                final DefaultEdge outgoingEdge = (DefaultEdge) updatedGraph.outgoingEdgesOf(node).toArray()[0];
                final DefaultEdge incomingEdge = (DefaultEdge) updatedGraph.incomingEdgesOf(node).toArray()[0];

                final UUID selectedNodeParent = updatedGraph.getEdgeSource(incomingEdge);
                final UUID selectedNodeChild = updatedGraph.getEdgeTarget(outgoingEdge);

                updatedGraph.removeEdge(incomingEdge);
                updatedGraph.removeEdge(outgoingEdge);
                updatedGraph.removeVertex(selectedNode);

                updatedGraph.addEdge(selectedNodeParent, regionStart);
                updatedGraph.addEdge(regionEnd, selectedNodeChild);

                break;
            }
        }

        return updatedGraph;
    }
}
