package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class Auxiliary {

    static Node getOrCreate(String name, Graph<Node, Edge> diagram) {
        Node node =
                diagram.vertexSet().stream()
                        .filter(n -> n.getName().equals(name))
                        .findFirst()
                        .orElse(null);
        if (node == null) {
            node = Node.builder().name(name).build();
            diagram.addVertex(node);
        }
        return node;
    }

    static Node getOrCreateAttr(String name, Node owner, Graph<Node, Edge> diagram) {
        return diagram.edgesOf(owner).stream()
                .map(e -> Graphs.getOppositeVertex(diagram, e, owner))
                .filter(n -> n.isAttribute() && n.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> {
                            Node node = Node.builder().name(name).build();
                            diagram.addVertex(node);
                            return node;
                        });
    }

    record FkInjection(String attrName, String referencedTable, boolean isTotal) {}

    static Map<Node, List<FkInjection>> resolveFkInjections(
            List<Node> tableNodes, Graph<Node, Edge> graph) {
        Map<Node, List<FkInjection>> injections = new HashMap<>();

        for (Node node : tableNodes) {
            if (!NodeClassifier.isRelationship(node, graph)) {
                continue;
            }
            NodeClassifier.RelationshipKind kind = NodeClassifier.classify(node, graph);
            List<Edge> fkEdges = NodeClassifier.getFkEdges(node, graph);
            if (fkEdges.size() != 2 || kind == NodeClassifier.RelationshipKind.NM) {
                continue;
            }

            Edge edgeA = fkEdges.get(0);
            Edge edgeB = fkEdges.get(1);
            Node attrA = Graphs.getOppositeVertex(graph, edgeA, node);
            Node attrB = Graphs.getOppositeVertex(graph, edgeB, node);
            Node entityA = findByName(tableNodes, attrA.getReference());
            Node entityB = findByName(tableNodes, attrB.getReference());
            if (entityA == null || entityB == null) {
                continue;
            }

            boolean totalA = "1".equals(edgeA.getCardinalityMin());
            boolean totalB = "1".equals(edgeB.getCardinalityMin());

            if (kind == NodeClassifier.RelationshipKind.ONE_TO_ONE) {
                injections
                        .computeIfAbsent(entityA, k -> new ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(edgeB),
                                        entityB.getName(),
                                        totalA || totalB));
            } else {
                boolean isOneToN = kind == NodeClassifier.RelationshipKind.ONE_TO_N;
                Node nSide = isOneToN ? entityB : entityA;
                Node oneSide = isOneToN ? entityA : entityB;
                Edge oneSideEdge = isOneToN ? edgeA : edgeB;
                boolean totalNSide = isOneToN ? totalB : totalA;
                injections
                        .computeIfAbsent(nSide, k -> new ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(oneSideEdge),
                                        oneSide.getName(),
                                        totalNSide));
            }
        }
        return injections;
    }

    private static Node findByName(List<Node> nodes, String name) {
        return nodes.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }
}
