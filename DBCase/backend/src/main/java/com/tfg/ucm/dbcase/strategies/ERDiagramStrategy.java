package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErUndefinedDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.Position;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;

@Service
public class ERDiagramStrategy implements DiagramStrategy<ErInput> {

    @Override
    public DiagramType getType() {
        return DiagramType.ER;
    }

    @Override
    public Class<ErInput> getInputType() {
        return ErInput.class;
    }

    @Override
    public Diagram generate(ErInput input) {
        Graph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);
        Map<String, Node> byId = new HashMap<>();

        for (var e : input.entities()) {
            Node n = Node.builder().name(e.name()).build();
            graph.addVertex(n);
            byId.put(e.id(), n);
        }

        if (input.undefineds() != null) {
            for (var u : input.undefineds()) {
                Node n = Node.builder().name(u.name()).build();
                graph.addVertex(n);
                byId.put(u.id(), n);
            }
        }

        for (var r : input.relationships()) {
            Node rel = Node.builder().name(r.name()).build();
            graph.addVertex(rel);
            byId.put(r.id(), rel);
            for (var p : r.participants()) {
                Node entity = byId.get(p.entityId());
                if (entity != null) {
                    String role = p.role() != null ? p.role() : "";
                    String label =
                            "rel:" + p.cardinalityMin() + ":" + p.cardinalityMax() + ":" + role;
                    graph.addEdge(rel, entity, Edge.builder().label(label).build());
                }
            }
        }

        for (var a : input.attributes()) {
            Node attr =
                    Node.builder()
                            .name(a.name())
                            .isAttribute(true)
                            .isPk(a.isKey())
                            .isNotNull(a.isNotNull())
                            .isUnique(a.isUnique())
                            .build();
            graph.addVertex(attr);
            byId.put(a.id(), attr);
            Node parent = byId.get(a.parentId());
            if (parent != null) {
                graph.addEdge(parent, attr, Edge.builder().label("attr:" + a.id()).build());
            }
        }

        return Diagram.builder().diagram(graph).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<ErEntityDTO> entities = new ArrayList<>();
        List<ErRelationshipDTO> relationships = new ArrayList<>();
        List<ErAttributeDTO> attributes = new ArrayList<>();
        List<ErUndefinedDTO> undefineds = new ArrayList<>();

        Map<Node, String> idOf = new HashMap<>();
        int[] counter = {0};
        graph.vertexSet().forEach(n -> idOf.put(n, "n" + counter[0]++));

        Set<Node> nonAttrs =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .collect(Collectors.toSet());

        for (Node n : nonAttrs) {
            String id = idOf.get(n);

            List<Edge> relEdgesOut =
                    graph.outgoingEdgesOf(n).stream()
                            .filter(e -> e.getLabel() != null && e.getLabel().startsWith("rel:"))
                            .toList();

            List<Node> attrChildren =
                    graph.outgoingEdgesOf(n).stream()
                            .filter(e -> e.getLabel() != null && e.getLabel().startsWith("attr:"))
                            .map(graph::getEdgeTarget)
                            .toList();

            List<String> attrIds = attrChildren.stream().map(idOf::get).toList();
            List<String> pkIds = attrChildren.stream().filter(Node::isPk).map(idOf::get).toList();

            boolean isTargetOfRel =
                    graph.incomingEdgesOf(n).stream()
                            .anyMatch(e -> e.getLabel() != null && e.getLabel().startsWith("rel:"));

            if (!relEdgesOut.isEmpty()) {
                List<ErRelationshipParticipantDTO> participants =
                        relEdgesOut.stream()
                                .map(
                                        e -> {
                                            Node target = graph.getEdgeTarget(e);
                                            String[] parts = e.getLabel().split(":", -1);
                                            String min = parts.length > 1 ? parts[1] : "";
                                            String max = parts.length > 2 ? parts[2] : "";
                                            String role =
                                                    parts.length > 3 && !parts[3].isEmpty()
                                                            ? parts[3]
                                                            : null;
                                            return new ErRelationshipParticipantDTO(
                                                    idOf.get(target), min, max, role);
                                        })
                                .toList();
                relationships.add(
                        new ErRelationshipDTO(
                                id,
                                n.getName(),
                                new Position(1, 1),
                                "Normal",
                                participants,
                                attrIds));
            } else if (isTargetOfRel || !attrChildren.isEmpty()) {
                entities.add(
                        new ErEntityDTO(
                                id, n.getName(), new Position(1, 1), false, attrIds, pkIds));
            } else {
                undefineds.add(new ErUndefinedDTO(id, n.getName(), new Position(1, 1), List.of()));
            }
        }

        for (Node n : graph.vertexSet()) {
            if (!n.isAttribute()) {
                continue;
            }
            String id = idOf.get(n);
            String parentId =
                    graph.incomingEdgesOf(n).stream()
                            .map(graph::getEdgeSource)
                            .map(idOf::get)
                            .findFirst()
                            .orElse(null);
            attributes.add(
                    new ErAttributeDTO(
                            id,
                            n.getName(),
                            new Position(1, 1),
                            parentId,
                            n.isPk(),
                            false,
                            false,
                            n.isNotNull(),
                            n.isUnique(),
                            null,
                            0,
                            List.of()));
        }

        return new ErInput(entities, relationships, attributes, List.of(), undefineds);
    }
}
