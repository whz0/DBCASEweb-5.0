package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.UUID;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class Auxiliary {

    static Node getOrCreateNode(String name, Graph<Node, Edge> diagram) {
        return diagram.vertexSet().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> {
                            Node node =
                                    Node.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .isAttribute(false)
                                            .name(name)
                                            .build();

                            diagram.addVertex(node);

                            return node;
                        });
    }

    static Node getOrCreateAttr(String name, Node node, Graph<Node, Edge> diagram) {
        return Graphs.neighborListOf(diagram, node).stream()
                .filter(attr -> attr.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> {
                            String uuid = UUID.randomUUID().toString();
                            Node attr =
                                    Node.builder().uuid(uuid).isAttribute(true).name(name).build();

                            diagram.addVertex(attr);

                            return attr;
                        });
    }

    static void addForeignAttr(Node attr, Node node, String ref, Graph<Node, Edge> diagram) {

        attr.setFk(true);
        attr.setDataType(DataType.of(Domain.INTEGER));
        attr.setReference(ref);
        addEdge(node, attr, diagram);
    }

    static void addPrimaryAttr(Node attr, Node node, Graph<Node, Edge> diagram) {

        attr.setPk(true);
        attr.setDataType(DataType.of(Domain.INTEGER));
        addEdge(node, attr, diagram);
    }

    static void addEdge(Node src, Node target, Graph<Node, Edge> diagram) {

        if (!diagram.containsEdge(src, target)) {
            diagram.addEdge(
                    src,
                    target,
                    Edge.builder().label("edge" + src.getName() + target.getName()).build());
        }
    }
}
