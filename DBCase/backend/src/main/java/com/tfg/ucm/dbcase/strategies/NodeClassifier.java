package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class NodeClassifier {

    public static NodeType classifyA(Node node, Graph<Node, Edge> graph) {

        if (isAttribute(node)) {
            return NodeType.ATTRIBUTE;
        } else {
            return classifyNonAttribute(node, graph);
        }
    }

    public static NodeType classifyNonAttribute(Node node, Graph<Node, Edge> graph) {

        if (isRelationship(node, graph)) {
            return classifyRelationship(node, graph);
        } else {
            return classifyEntity(node, graph);
        }
    }

    public static NodeType classifyRelationship(Node node, Graph<Node, Edge> graph) {
        return NodeType.RELATIONSHIP;
    }

    public static NodeType classifyEntity(Node node, Graph<Node, Edge> graph) {
        return NodeType.ENTITY;
    }

    public static boolean isAttribute(Node node) {
        return node.isAttribute();
    }

    public static boolean isRelationship(Node node, Graph<Node, Edge> graph) {
        if (node.isAttribute()) {
            return false;
        }
        var fkAttrs =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(
                                a ->
                                        a.isAttribute()
                                                && a.isFk()
                                                && a.getReference() != null
                                                && !a.getReference().equals(node.getName()))
                        .toList();
        if (fkAttrs.isEmpty()) {
            return false;
        }
        long ownPks =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(a -> a.isAttribute() && a.isPk() && !a.isFk())
                        .count();
        return ownPks == 0;
    }

    public static boolean isEntity(Node node, Graph<Node, Edge> graph) {
        return !node.isAttribute() && !isRelationship(node, graph);
    }

    public static boolean isForeignKey(Node attr, Node owner) {
        return attr.isAttribute()
                && attr.isFk()
                && attr.getReference() != null
                && !attr.getReference().equals(owner.getName());
    }

    public static String getFkAttrName(Edge edge) {
        String label = edge.getLabel();
        if (label != null && label.startsWith("fk:")) {
            return label.substring(label.lastIndexOf(':') + 1);
        }
        return null;
    }

    public static List<Edge> getFkEdges(Node rel, Graph<Node, Edge> graph) {
        return graph.edgesOf(rel).stream()
                .filter(
                        e -> {
                            Node other = Graphs.getOppositeVertex(graph, e, rel);
                            return other.isAttribute()
                                    && other.isFk()
                                    && other.getReference() != null
                                    && !other.getReference().equals(rel.getName());
                        })
                .toList();
    }

    public static RelationshipKind classify(Node rel, Graph<Node, Edge> graph) {
        List<Edge> fkEdges = getFkEdges(rel, graph);
        if (fkEdges.size() != 2) {
            return RelationshipKind.NM;
        }

        Edge edgeA = fkEdges.get(0);
        Edge edgeB = fkEdges.get(1);
        String maxA = edgeA.getCardinalityMax();
        String maxB = edgeB.getCardinalityMax();

        boolean aIsOne = "1".equals(maxA);
        boolean bIsOne = "1".equals(maxB);

        if (aIsOne && bIsOne) {
            return RelationshipKind.ONE_TO_ONE;
        }
        if (aIsOne) {
            return RelationshipKind.ONE_TO_N;
        }
        if (bIsOne) {
            return RelationshipKind.N_TO_ONE;
        }
        return RelationshipKind.NM;
    }

    public enum RelationshipKind {
        ONE_TO_ONE,
        ONE_TO_N,
        N_TO_ONE,
        NM
    }
}
