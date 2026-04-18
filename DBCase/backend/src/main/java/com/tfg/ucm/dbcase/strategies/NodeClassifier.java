package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class NodeClassifier {

    public static boolean isAttribute(Node node) {
        return node.isAttribute();
    }

    public static boolean isRelationship(Node node, Graph<Node, Edge> graph) {
        if (node.isAttribute()) {
            return false;
        }
        var pkAttrs =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(a -> a.isAttribute() && a.isPk())
                        .toList();
        if (pkAttrs.isEmpty()) {
            return false;
        }
        long externalPks =
                pkAttrs.stream()
                        .filter(
                                a ->
                                        a.getReference() != null
                                                && !a.getReference().equals(node.getName()))
                        .count();
        return externalPks == pkAttrs.size();
    }

    public static boolean isEntity(Node node, Graph<Node, Edge> graph) {
        return !node.isAttribute() && !isRelationship(node, graph);
    }

    public static boolean isForeignKey(Node attr, Node owner) {
        return attr.isAttribute()
                && attr.isPk()
                && attr.getReference() != null
                && !attr.getReference().equals(owner.getName());
    }
}
