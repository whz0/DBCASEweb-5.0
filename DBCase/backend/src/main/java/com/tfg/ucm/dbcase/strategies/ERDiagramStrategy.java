package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;

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
import org.jgrapht.graph.Multigraph;
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
        Graph<Node, Edge> graph = new Multigraph<>(Edge.class);
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
            Node parent = byId.get(a.parentId());
            if (parent == null) {
                continue;
            }
            Node attr = getOrCreateAttr(a.name(), parent, graph);
            attr.setAttribute(true);
            attr.setPk(a.isKey());
            attr.setNotNull(a.isNotNull());
            attr.setUnique(a.isUnique());
            byId.put(a.id(), attr);
            graph.addEdge(parent, attr, Edge.builder().label("attr:" + a.id()).build());
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
                Graphs.neighborListOf(graph, entity).stream()
                        .filter(n -> n.isAttribute() && n.isPk())
                        .forEach(
                                pkAttr -> {
                                    Node fkNode =
                                            Node.builder()
                                                    .name(UUID.randomUUID().toString())
                                                    .isAttribute(true)
                                                    .isFk(true)
                                                    .reference(entity.getName())
                                                    .build();
                                    graph.addVertex(fkNode);
                                    graph.addEdge(
                                            rel,
                                            fkNode,
                                            Edge.builder()
                                                    .label(
                                                            "fk:"
                                                                    + rel.getName()
                                                                    + ":"
                                                                    + pkAttr.getName())
                                                    .cardinalityMin(p.cardinalityMin())
                                                    .cardinalityMax(p.cardinalityMax())
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
                        .filter(n -> !NodeClassifier.isAttribute(n))
                        .collect(Collectors.toSet());

        Map<String, String> nodeToId = new HashMap<>();
        nodes.forEach(n -> nodeToId.put(n.getName(), UUID.randomUUID().toString()));

        int idx = 0;
        for (final Node node : nodes) {
            String id = nodeToId.get(node.getName());
            Position pos = new Position((idx % 4) * 300 + 100, ((double) idx / 4) * 250 + 100);
            idx++;

            if (NodeClassifier.isRelationship(node, graph)) {
                buildRelationship(node, id, pos, graph, nodeToId, relationships, attributes);
            } else {
                buildEntity(node, id, pos, graph, nodeToId, entities, relationships, attributes);
            }
        }

        return new ErInput(entities, relationships, attributes, List.of(), List.of());
    }

    private void buildEntity(
            Node node,
            String id,
            Position pos,
            Graph<Node, Edge> graph,
            Map<String, String> nodeToId,
            List<ErEntityDTO> entities,
            List<ErRelationshipDTO> relationships,
            List<ErAttributeDTO> attributes) {
        List<Node> ownAttrs =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(
                                a ->
                                        NodeClassifier.isAttribute(a)
                                                && !NodeClassifier.isForeignKey(a, node))
                        .toList();
        List<String> attrIds = new ArrayList<>();
        List<String> pkIds = new ArrayList<>();
        for (int i = 0; i < ownAttrs.size(); i++) {
            Node attr = ownAttrs.get(i);
            String attrId = UUID.randomUUID().toString();
            Position attrPos =
                    new Position(pos.x() + (i - (double) ownAttrs.size() / 2) * 120, pos.y() + 130);
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
        entities.add(new ErEntityDTO(id, node.getName(), pos, false, attrIds, pkIds));

        Graphs.neighborListOf(graph, node).stream()
                .filter(a -> NodeClassifier.isForeignKey(a, node) && !a.isPk())
                .forEach(
                        fkAttr -> {
                            String refId = nodeToId.get(fkAttr.getReference());
                            if (refId == null) {
                                return;
                            }
                            String relName = node.getName() + fkAttr.getReference();
                            List<ErRelationshipParticipantDTO> participants =
                                    List.of(
                                            new ErRelationshipParticipantDTO(id, "1", "1", null),
                                            new ErRelationshipParticipantDTO(
                                                    refId, "1", "1", null));
                            relationships.add(
                                    new ErRelationshipDTO(
                                            UUID.randomUUID().toString(),
                                            relName,
                                            pos,
                                            "Normal",
                                            participants,
                                            List.of()));
                        });
    }

    private void buildRelationship(
            Node node,
            String id,
            Position pos,
            Graph<Node, Edge> graph,
            Map<String, String> nodeToId,
            List<ErRelationshipDTO> relationships,
            List<ErAttributeDTO> attributes) {
        List<Node> fkNodes =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(a -> NodeClassifier.isForeignKey(a, node))
                        .toList();
        NodeClassifier.RelationshipKind kind = NodeClassifier.classify(node, graph);

        List<Node> ownAttrs =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(
                                a ->
                                        NodeClassifier.isAttribute(a)
                                                && !NodeClassifier.isForeignKey(a, node))
                        .toList();
        List<String> attrIds = new ArrayList<>();
        for (int i = 0; i < ownAttrs.size(); i++) {
            Node attr = ownAttrs.get(i);
            String attrId = UUID.randomUUID().toString();
            Position attrPos =
                    new Position(pos.x() + (i - (double) ownAttrs.size() / 2) * 120, pos.y() + 130);
            attrIds.add(attrId);
            attributes.add(
                    new ErAttributeDTO(
                            attrId,
                            attr.getName(),
                            attrPos,
                            id,
                            false,
                            false,
                            false,
                            attr.isNotNull(),
                            attr.isUnique(),
                            null,
                            0,
                            List.of()));
        }

        if (kind == NodeClassifier.RelationshipKind.ONE_TO_ONE && fkNodes.size() == 2) {
            String nameA = fkNodes.get(0).getReference();
            String nameB = fkNodes.get(1).getReference();
            List<ErRelationshipParticipantDTO> participants =
                    List.of(
                            new ErRelationshipParticipantDTO(nodeToId.get(nameA), "1", "1", null),
                            new ErRelationshipParticipantDTO(nodeToId.get(nameB), "1", "1", null));
            relationships.add(
                    new ErRelationshipDTO(
                            UUID.randomUUID().toString(),
                            nameA + nameB,
                            pos,
                            "Normal",
                            participants,
                            attrIds));
        } else {
            List<ErRelationshipParticipantDTO> participants =
                    fkNodes.stream()
                            .map(
                                    a ->
                                            new ErRelationshipParticipantDTO(
                                                    nodeToId.get(a.getReference()), "0", "N", null))
                            .toList();
            relationships.add(
                    new ErRelationshipDTO(
                            id, node.getName(), pos, "Normal", participants, attrIds));
        }
    }
}
