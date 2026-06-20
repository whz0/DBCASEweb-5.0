package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class NodeClassifier {

    public static boolean isEntity(Node node, Graph<Node, Edge> graph) {

        return !Graphs.successorListOf(graph, node).stream()
                .filter(attr -> attr.isAttribute() && attr.isPk() && !attr.isFk())
                .collect(Collectors.toSet())
                .isEmpty();
    }

    public static boolean isNMRel(Node node, Graph<Node, Edge> graph) {
        Set<Node> fks =
                Graphs.successorListOf(graph, node).stream()
                        .filter(Node::isFk)
                        .collect(Collectors.toSet());
        Set<Node> pks =
                Graphs.successorListOf(graph, node).stream()
                        .filter(Node::isPk)
                        .collect(Collectors.toSet());

        if (fks.isEmpty()) {
            return false;
        }

        return fks.equals(pks);
    }

    public static Set<Node> hasUniqueFk(Node node, Graph<Node, Edge> graph) {
        return Graphs.successorListOf(graph, node).stream()
                .filter(
                        attr ->
                                attr.isAttribute()
                                        && attr.isFk()
                                        && attr.isUnique()
                                        && attr.getReference() != null)
                .collect(Collectors.toSet());
    }

    public static boolean isTotalRel(Node node1, Node node2, Graph<Node, Edge> graph) {
        return !Graphs.successorListOf(graph, node1).stream()
                        .filter(
                                attr ->
                                        attr.isAttribute()
                                                && attr.isFk()
                                                && attr.isNotNull()
                                                && attr.isUnique()
                                                && attr.getReference().equals(node2.getName()))
                        .collect(Collectors.toSet())
                        .isEmpty()
                && !Graphs.successorListOf(graph, node2).stream()
                        .filter(
                                attr ->
                                        attr.isAttribute()
                                                && attr.isFk()
                                                && attr.isNotNull()
                                                && attr.isUnique()
                                                && attr.getReference().equals(node1.getName()))
                        .collect(Collectors.toSet())
                        .isEmpty();
    }
}
