package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.Position;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
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

        for (var r : input.relationships()) {
            Node rel = Node.builder().name(r.name()).build();
            graph.addVertex(rel);
            byId.put(r.id(), rel);
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
                graph.addEdge(parent, attr, Edge.builder().label("attr" + a.id()).build());
            }
        }

        for (var r : input.relationships()) {
            Node rel = byId.get(r.id());
            if (rel == null) {
                continue;
            }
            for (var p : r.participants()) {
                Node entity = byId.get(p.entityId());
                if (entity == null) {
                    continue;
                }

                graph.vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.isPk() && graph.containsEdge(entity, n))
                        .forEach(
                                pkAttr -> {
                                    pkAttr.setReference(entity.getName());
                                    graph.addEdge(
                                            rel,
                                            pkAttr,
                                            Edge.builder()
                                                    .label(
                                                            "fk:"
                                                                    + rel.getName()
                                                                    + ":"
                                                                    + pkAttr.getName())
                                                    .build());
                                });
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

        Set<Node> nodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .collect(Collectors.toSet());

        Map<String, String> nodeToId = new HashMap<>();
        nodes.forEach(n -> nodeToId.put(n.getName(), UUID.randomUUID().toString()));

        int[] idx = {0};
        for (Node node : nodes) {
            String id = nodeToId.get(node.getName());
            int col = idx[0] % 4;
            int row = idx[0] / 4;
            Position pos = new Position(col * 300 + 100, row * 250 + 100);
            idx[0]++;

            List<Node> pkAttrs =
                    Graphs.neighborListOf(graph, node).stream()
                            .filter(a -> a.isAttribute() && a.isPk())
                            .toList();

            long externalPks =
                    pkAttrs.stream()
                            .filter(
                                    a ->
                                            a.getReference() != null
                                                    && !a.getReference().equals(node.getName()))
                            .count();

            boolean isRelationship = !pkAttrs.isEmpty() && externalPks == pkAttrs.size();

            List<Node> ownAttrs =
                    Graphs.neighborListOf(graph, node).stream()
                            .filter(a -> a.isAttribute() && !(isRelationship && a.isPk()))
                            .toList();

            List<String> attrIds = new ArrayList<>();
            List<String> pkIds = new ArrayList<>();

            for (int i = 0; i < ownAttrs.size(); i++) {
                Node attr = ownAttrs.get(i);
                String attrId = UUID.randomUUID().toString();
                Position attrPos =
                        new Position(pos.x() + (i - ownAttrs.size() / 2) * 120, pos.y() + 130);
                attrIds.add(attrId);
                if (attr.isPk()) {
                    pkIds.add(attrId);
                }
                attributes.add(
                        new ErAttributeDTO(
                                attrId,
                                attr.getName(),
                                attrPos,
                                id,
                                attr.isPk(),
                                false,
                                false,
                                attr.isNotNull(),
                                attr.isUnique(),
                                null,
                                0,
                                List.of()));
            }

            if (isRelationship) {
                List<ErRelationshipParticipantDTO> participants =
                        pkAttrs.stream()
                                .filter(a -> a.getReference() != null)
                                .map(
                                        a ->
                                                new ErRelationshipParticipantDTO(
                                                        nodeToId.get(a.getReference()),
                                                        "0",
                                                        "N",
                                                        null))
                                .toList();
                relationships.add(
                        new ErRelationshipDTO(
                                id, node.getName(), pos, "Normal", participants, attrIds));
            } else {
                entities.add(new ErEntityDTO(id, node.getName(), pos, false, attrIds, pkIds));
            }
        }

        return new ErInput(entities, relationships, attributes, List.of(), List.of());
    }
}
