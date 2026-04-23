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

import static com.tfg.ucm.dbcase.strategies.Auxiliary.*;

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
        Map<String, ErRelationshipDTO> relationshipDTOMap = new HashMap<>();
        Map<String, ErEntityDTO> entityDTOMap = new HashMap<>();
        Map<String, ErAttributeDTO> attributeDTOMap = new HashMap<>();

        for(ErRelationshipDTO r : input.relationships()) {
            relationshipDTOMap.put(r.id(), r);
        }

        for(ErEntityDTO e : input.entities()) {
            entityDTOMap.put(e.id(), e);
        }

        for(ErAttributeDTO a : input.attributes()) {
            attributeDTOMap.put(a.id(), a);
        }

        for (ErRelationshipDTO erRel : input.relationships()) {
            Map<String, String> listN = new HashMap<>();
            Map<String, String> listOne = new HashMap<>();
            for (ErRelationshipParticipantDTO erPart : erRel.participants()) {

                ErEntityDTO entity = entityDTOMap.get(erPart.entityId());
                // Si el número de relaciones N es igual que el número de
                // participantes entonces habrá que crear tabla intermedia
                if(erPart.cardinalityMax().equals("N")) {
                    mapAttribute(listN, entity.primaryKeys(), entity.name());
                }
                else {
                    mapAttribute(listOne, entity.primaryKeys(), entity.name());
                    for(String attrName : listOne.keySet()) {
                        Node entityRef = getOrCreateNode(listOne.get(attrName), graph);
                        Node attrRef = getOrCreateAttr(attrName, entityRef, graph);
                        Node entitySrc = getOrCreateNode(entity.name(), graph);
                        Node attrSrc = getOrCreateAttr(attrName, entitySrc, graph);
                        addEdge(attrSrc, attrRef, graph);
                    }
                }
                processEntity(entityDTOMap.get(erPart.entityId()), input.attributes(), graph);
            }
            if(listN.size() == erRel.participants().size() || !erRel.attributes().isEmpty()) {
                processRelationship(erRel, input.attributes(), graph);
            }
        }

        return Diagram.builder().diagram(graph).build();
    }

    private void mapAttribute(Map<String, String> map, List<String> pks,
                              String entity) {
        for(String pk : pks) {
            map.put(pk, entity);
        }
    }

    private void processEntity(ErEntityDTO erEnt,
                                     List<ErAttributeDTO> erAttributes,Graph<Node, Edge> graph){

        Node enity = getOrCreateNode(erEnt.name(), graph);

        for(String attrName : erEnt.attributes()) {
            ErAttributeDTO erAttr = erAttributes.stream().filter(a -> a.name().equals(attrName))
                    .findFirst().orElse(null);

            if(erAttr != null) {
                Node attr = getOrCreateAttr(attrName, enity, graph);

                if(erAttr.isKey()) {
                    addPrimaryAttr(attr, enity, graph);
                }
                else {
                    addEdge(enity, attr, graph);
                }
            }
        }

    }

    private void processRelationship(ErRelationshipDTO erRel,
                                     List<ErAttributeDTO> erAttributes,Graph<Node, Edge> graph){

        Node rel = getOrCreateNode(erRel.name(), graph);

        for(String attrName : erRel.attributes()) {
            ErAttributeDTO erAttr = erAttributes.stream().filter(a -> a.name().equals(attrName))
                    .findFirst().orElse(null);

            if(erAttr != null) {
                Node attr = getOrCreateAttr(attrName, rel, graph);

                if(erAttr.isKey()) {
                    addPrimaryAttr(attr, rel, graph);
                }
                else {
                    addEdge(rel, attr, graph);
                }
            }
        }

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

        int[] idx = {0};
        for (Node node : nodes) {
            String id = nodeToId.get(node.getName());
            Position pos = new Position((idx[0] % 4) * 300 + 100, ((double) idx[0] / 4) * 250 + 100);
            idx[0]++;

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
