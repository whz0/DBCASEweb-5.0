package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Participant;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.Undefined;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.input.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.input.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
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

        Map<String, Node> nodeById = new HashMap<>();

        for (ErEntityDTO e : input.entities()) {
            Entity entity = Entity.builder().name(e.name()).weak(e.isWeak()).build();
            graph.addVertex(entity);
            nodeById.put(e.id(), entity);
        }

        for (ErRelationshipDTO r : input.relationships()) {
            if (r.participants().size() < 2) {
                Undefined undef = Undefined.builder().name(r.name()).attributes(List.of()).build();
                graph.addVertex(undef);
                nodeById.put(r.id(), undef);
            } else {
                List<Participant> participants =
                        r.participants().stream()
                                .map(
                                        p -> {
                                            Participant part = new Participant();
                                            part.setEntity(nodeById.get(p.entityId()));
                                            part.setRole(p.role());
                                            part.setCardinality(p.cardinalityMax());
                                            return part;
                                        })
                                .filter(p -> p.getEntity() != null)
                                .toList();
                Relationship rel =
                        Relationship.builder()
                                .name(r.name())
                                .participants(participants)
                                .attributes(List.of())
                                .build();
                graph.addVertex(rel);
                nodeById.put(r.id(), rel);
            }
        }

        for (ErAttributeDTO a : input.attributes()) {
            Node parent = nodeById.get(a.parentId());
            String fk = (parent instanceof Relationship) ? parent.getName() : null;
            Attribute attr =
                    Attribute.builder()
                            .name(a.name())
                            .pk(a.isKey())
                            .compose(a.isComposite())
                            .multivalue(a.isMultivalued())
                            .noEmpty(a.isNotNull())
                            .unique(a.isUnique())
                            .size(a.size())
                            .fk(fk)
                            .build();
            graph.addVertex(attr);
            nodeById.put(a.id(), attr);

            if (parent != null) {
                graph.addEdge(
                        parent,
                        attr,
                        Edge.builder().label("attr:" + a.parentId() + ":" + a.id()).build());
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

        Map<String, String> idByName = new HashMap<>();
        int[] counter = {0};
        graph.vertexSet().forEach(n -> idByName.put(n.getName(), "n" + counter[0]++));

        for (Node node : graph.vertexSet()) {
            String id = idByName.get(node.getName());

            if (node instanceof Entity entity) {
                List<String> attrIds = new ArrayList<>();
                List<String> pkIds = new ArrayList<>();
                BreadthFirstIterator<Node, Edge> bfs = new BreadthFirstIterator<>(graph, node);
                bfs.next();
                while (bfs.hasNext()) {
                    Node n = bfs.next();
                    if (n instanceof Attribute a) {
                        String aid = idByName.get(a.getName());
                        attrIds.add(aid);
                        if (a.isPk()) {
                            pkIds.add(aid);
                        }
                    }
                }
                entities.add(
                        new ErEntityDTO(
                                id,
                                entity.getName(),
                                new Position(1, 1),
                                entity.isWeak(),
                                attrIds,
                                pkIds));

            } else if (node instanceof Relationship rel) {
                List<ErRelationshipParticipantDTO> participants =
                        rel.getParticipants() == null
                                ? List.of()
                                : rel.getParticipants().stream()
                                .map(
                                        p ->
                                                new ErRelationshipParticipantDTO(
                                                        idByName.get(
                                                                p.getEntity().getName()),
                                                        "",
                                                        p.getCardinality(),
                                                        p.getRole()))
                                .toList();
                relationships.add(
                        new ErRelationshipDTO(
                                id,
                                rel.getName(),
                                new Position(1, 1),
                                "Normal",
                                participants,
                                List.of()));

            } else if (node instanceof Undefined undef) {
                relationships.add(
                        new ErRelationshipDTO(
                                id,
                                undef.getName() + "?",
                                new Position(1, 1),
                                "Normal",
                                List.of(),
                                List.of()));

            } else if (node instanceof Attribute attr) {
                String parentId =
                        attr.getFk() != null
                                ? attr.getFk()
                                : graph.incomingEdgesOf(attr).stream()
                                .map(graph::getEdgeSource)
                                .filter(n -> !(n instanceof Attribute))
                                .map(n -> idByName.get(n.getName()))
                                .findFirst()
                                .orElse(null);
                attributes.add(
                        new ErAttributeDTO(
                                id,
                                attr.getName(),
                                new Position(1, 1),
                                parentId,
                                attr.isPk(),
                                attr.isCompose(),
                                attr.isMultivalue(),
                                attr.isNoEmpty(),
                                attr.isUnique(),
                                null,
                                attr.getSize(),
                                List.of()));
            }
        }

        return new ErInput(entities, relationships, attributes, List.of());
    }
}
