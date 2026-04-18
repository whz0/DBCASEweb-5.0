package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
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

    /** Gets or creates an attribute node scoped to a specific owner (avoids name collisions). */
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
}
