package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import org.jgrapht.Graph;

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
}
