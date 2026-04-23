package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static NodeType hasEdgesWithPk(){

        return null;
    }

    public static NodeType hasEdgesWithFk(){

        return null;
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

        Set<Node> fks = Graphs.neighborSetOf(graph, node).stream().filter(Node::isFk).collect(Collectors.toSet());
        Set<Node> pks = Graphs.neighborSetOf(graph, node).stream().filter(Node::isPk).collect(Collectors.toSet());

        if (fks.isEmpty()) {
            return false;
        }

        return fks.equals(pks);
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
}
