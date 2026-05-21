package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.addEdge;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addForeignAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addPrimaryAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.editFk;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;
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
        Graph<Node, Edge> graph = new DirectedPseudograph<>(Edge.class);

        Map<String, ErEntityDTO> entityDTOMap = new HashMap<>();
        Map<String, ErAttributeDTO> attributeDTOMap = new HashMap<>();

        for (ErEntityDTO e : input.entities()) {
            entityDTOMap.put(e.id(), e);
        }
        for (ErAttributeDTO a : input.attributes()) {
            attributeDTOMap.put(a.id(), a);
        }

        Map<String, String> customDomainMap = new HashMap<>();
        if (input.domains() != null) {
            for (var d : input.domains()) {
                if (d.baseType() != null) {
                    customDomainMap.put(d.name().toUpperCase(), d.baseType().toUpperCase());
                }
            }
        }

        List<ErRelationshipDTO> normalRel = new ArrayList<>();
        List<ErRelationshipDTO> otherRel = new ArrayList<>();

        for (ErRelationshipDTO erRel : input.relationships()) {
            if (erRel.type().equalsIgnoreCase("normal")) {
                normalRel.add(erRel);
            } else {
                otherRel.add(erRel);
            }
        }

        Set<String> originalEntityIds = new HashSet<>(entityDTOMap.keySet());

        List<ErRelationshipDTO> aggregations = new ArrayList<>();
        for (ErRelationshipDTO erRel : otherRel) {
            if (erRel.type().equalsIgnoreCase("aggregation")) {
                generateAggregation(erRel, entityDTOMap, attributeDTOMap, customDomainMap, graph);
                aggregations.add(erRel);
            } else if (erRel.type().equalsIgnoreCase("isa")) {
                generateIsA(erRel, entityDTOMap, attributeDTOMap, customDomainMap, graph);
            }
        }

        for (ErEntityDTO erEnt : input.entities()) {
            processOwnAttributes(erEnt, attributeDTOMap, customDomainMap, graph);
        }

        for (ErRelationshipDTO erRel : aggregations) {
            addPksToAggregation(
                    erRel.id(), erRel.aggregationName(), entityDTOMap, attributeDTOMap, graph);
        }

        for (ErRelationshipDTO erRel : normalRel) {
            processRel(
                    erRel,
                    entityDTOMap,
                    attributeDTOMap,
                    customDomainMap,
                    graph,
                    !erRel.attributes().isEmpty());
        }

        for (Map.Entry<String, ErEntityDTO> entry : entityDTOMap.entrySet()) {
            if (!originalEntityIds.contains(entry.getKey())) {
                processOwnAttributes(entry.getValue(), attributeDTOMap, customDomainMap, graph);
            }
        }

        for (ErUndefinedDTO u : input.undefineds()) {
            getOrCreateNode(u.name(), graph);
        }

        return Diagram.builder().diagram(graph).build();
    }

    private void processRel(
            ErRelationshipDTO erRel,
            Map<String, ErEntityDTO> entityDTOMap,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph,
            boolean createIntermediate) {

        String name = erRel.aggregationName() != null ? erRel.aggregationName() : erRel.name();
        Node relationship = getOrCreateNode(name, graph);
        processAttributes(
                relationship, erRel.attributes(), attributeDTOMap, customDomainMap, graph);

        boolean self =
                erRel.participants().stream()
                                .map(ErRelationshipParticipantDTO::entityId)
                                .distinct()
                                .count()
                        == 1;

        if (erRel.participants().size() > 2) {
            generateNAria(relationship, erRel, entityDTOMap, attributeDTOMap, graph, self);
        } else if (erRel.participants().size() == 2) {
            ErRelationshipParticipantDTO one = erRel.participants().get(0);
            ErRelationshipParticipantDTO other = erRel.participants().get(1);

            boolean isNM =
                    one.cardinalityMax().equalsIgnoreCase("n")
                            && other.cardinalityMax().equalsIgnoreCase("n");
            boolean isOneOne =
                    one.cardinalityMax().equals("1") && other.cardinalityMax().equals("1");

            ErEntityDTO entityOne = entityDTOMap.get(one.entityId());
            ErEntityDTO entityOther = entityDTOMap.get(other.entityId());
            if (entityOne == null || entityOther == null) {
                return;
            }

            if (isNM) {
                generateNM(
                        relationship,
                        one,
                        other,
                        entityOne,
                        entityOther,
                        attributeDTOMap,
                        graph,
                        self);
            } else if (isOneOne) {
                generateOneOne(
                        relationship,
                        one,
                        other,
                        entityOne,
                        entityOther,
                        attributeDTOMap,
                        graph,
                        createIntermediate,
                        self);
            } else {
                ErRelationshipParticipantDTO oneSide =
                        one.cardinalityMax().equalsIgnoreCase("1") ? one : other;
                ErRelationshipParticipantDTO nSide =
                        !one.cardinalityMax().equalsIgnoreCase("1") ? one : other;

                generateOneN(
                        relationship,
                        oneSide,
                        nSide,
                        entityDTOMap.get(oneSide.entityId()),
                        entityDTOMap.get(nSide.entityId()),
                        nSide.cardinalityMin(),
                        attributeDTOMap,
                        graph,
                        createIntermediate,
                        self);
            }
        }
    }

    private void addPksToAggregation(
            String id,
            String name,
            Map<String, ErEntityDTO> entityDTOMap,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Graph<Node, Edge> graph) {

        Node node = getOrCreateNode(name, graph);

        List<Node> pks = Graphs.successorListOf(graph, node).stream().filter(Node::isPk).toList();

        List<String> pksId = new ArrayList<>();
        for (Node pk : pks) {
            attributeDTOMap.put(
                    pk.getUuid(),
                    new ErAttributeDTO(
                            pk.getUuid(),
                            pk.getName(),
                            null,
                            null,
                            true,
                            false,
                            false,
                            false,
                            false,
                            pk.getDataType().toString(),
                            pk.getDataType().length(),
                            List.of()));
            pksId.add(pk.getUuid());
        }
        entityDTOMap.put(id, new ErEntityDTO(id, name, null, false, List.of(), pksId));
    }

    private void generateIsA(
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        ErRelationshipParticipantDTO parentId =
                rel.participants().stream()
                        .filter(p -> p.role().equalsIgnoreCase("parent"))
                        .findFirst()
                        .orElse(null);

        assert parentId != null;
        List<ErAttributeDTO> pks = new ArrayList<>();
        ErEntityDTO parent = entityMap.get(parentId.entityId());
        for (String pkId : parent.primaryKeys()) {
            pks.add(attrMap.get(pkId));
        }

        for (ErRelationshipParticipantDTO participantDTO : rel.participants()) {
            ErEntityDTO entity = entityMap.get(participantDTO.entityId());
            processOwnAttributes(entity, attrMap, customDomainMap, graph);
            if (!entity.id().equals(parent.id())) {
                for (ErAttributeDTO pk : pks) {
                    Node node = getOrCreateNode(entity.name(), graph);
                    addFkToRef(pk.name(), node, parent.name(), true, false, false, graph, "");
                }
            }
        }
    }

    private void generateAggregation(
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        processRel(rel, entityMap, attrMap, customDomainMap, graph, true);
    }

    private void generateNAria(
            Node relationshipNode,
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean self) {

        for (ErRelationshipParticipantDTO participant : rel.participants()) {
            ErEntityDTO entity = entityMap.get(participant.entityId());
            List<String> pksName = getPksName(entity, attrMap);
            for (String pk : pksName) {
                String role = self ? participant.role() : "";
                addFkToRef(pk, relationshipNode, entity.name(), true, false, false, graph, role);
            }
        }
    }

    private void generateNM(
            Node relationshipNode,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantOther,
            ErEntityDTO entityOne,
            ErEntityDTO entityOther,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean self) {

        List<String> pksName = getPksName(entityOne, attrMap);
        for (String pk : pksName) {
            String role = self ? participantOne.role() : "";
            addFkToRef(pk, relationshipNode, entityOne.name(), true, false, false, graph, role);
        }

        pksName = getPksName(entityOther, attrMap);
        for (String pk : pksName) {
            String role = self ? participantOther.role() : "";
            addFkToRef(pk, relationshipNode, entityOther.name(), true, false, false, graph, role);
        }
    }

    private void generateOneN(
            Node relationshipNode,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantN,
            ErEntityDTO oneSide,
            ErEntityDTO nSide,
            String minCard,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean createIntermediate,
            boolean self) {

        boolean nIsTotal = !minCard.equals("0");
        Node node = createIntermediate ? relationshipNode : getOrCreateNode(nSide.name(), graph);
        List<String> pksName = null;
        if (createIntermediate) {
            pksName = getPksName(nSide, attrMap);
            for (String pk : pksName) {
                String role = self ? participantN.role() : "";
                addFkToRef(pk, node, nSide.name(), true, false, false, graph, role);
            }
        }
        pksName = getPksName(oneSide, attrMap);
        for (String pk : pksName) {
            String role = self ? participantOne.role() : "";
            addFkToRef(pk, node, oneSide.name(), false, false, nIsTotal, graph, role);
        }
    }

    private void generateOneOne(
            Node relationship,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantOther,
            ErEntityDTO entityOne,
            ErEntityDTO entityOther,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean createIntermediate,
            boolean self) {

        boolean oneIsTotal = !participantOne.cardinalityMin().equals("0");
        boolean otherIsTotal = !participantOther.cardinalityMin().equals("0");
        Node nodeOne = createIntermediate ? relationship : getOrCreateNode(entityOne.name(), graph);
        Node nodeOther =
                createIntermediate ? relationship : getOrCreateNode(entityOther.name(), graph);

        List<String> pksName = null;
        if (createIntermediate) {
            boolean onePk = oneIsTotal || !otherIsTotal;

            pksName = getPksName(entityOther, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOther.role() : "";
                addFkToRef(
                        pk,
                        relationship,
                        entityOther.name(),
                        onePk,
                        !onePk,
                        oneIsTotal,
                        graph,
                        role);
            }

            pksName = getPksName(entityOne, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOne.role() : "";
                addFkToRef(
                        pk,
                        relationship,
                        entityOne.name(),
                        !onePk,
                        onePk,
                        otherIsTotal,
                        graph,
                        role);
            }
        } else {
            if (oneIsTotal || !otherIsTotal) {
                pksName = getPksName(entityOther, attrMap);
                for (String pk : pksName) {
                    String role = self ? participantOther.role() : "";
                    addFkToRef(
                            pk, nodeOne, entityOther.name(), false, true, oneIsTotal, graph, role);
                }
            }
            if (!oneIsTotal || otherIsTotal) {
                pksName = getPksName(entityOne, attrMap);
                for (String pk : pksName) {
                    String role = self ? participantOne.role() : "";
                    addFkToRef(
                            pk,
                            nodeOther,
                            entityOne.name(),
                            false,
                            true,
                            otherIsTotal,
                            graph,
                            role);
                }
            }
        }
    }

    private List<String> getPksName(ErEntityDTO entity, Map<String, ErAttributeDTO> attrMap) {
        List<String> pks = new ArrayList<>();

        if (entity.primaryKeys().isEmpty()) {
            pks.add("id_" + entity.name().toLowerCase());
        } else {
            for (String pkId : entity.primaryKeys()) {
                pks.add(attrMap.get(pkId).name());
            }
        }

        return pks;
    }

    private void addFkToRef(
            String attrName,
            Node owner,
            String ref,
            boolean isPk,
            boolean isUnique,
            boolean isNotNull,
            Graph<Node, Edge> graph,
            String role) {
        Node attrNode = getOrCreateAttr(attrName + role, owner, graph);
        editFk(attrNode, isPk, isUnique, isNotNull);
        addForeignAttr(attrNode, owner, ref, graph);

        Node refNode = getOrCreateNode(ref, graph);
        Node attrRef = getOrCreateAttr(attrName, refNode, graph);
        addPrimaryAttr(attrRef, refNode, graph);

        addEdge(attrNode, attrRef, graph);
    }

    private void processAttributes(
            Node owner,
            List<String> attrIds,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {
        for (String attrId : attrIds) {
            ErAttributeDTO erAttr = attributeDTOMap.get(attrId);
            if (erAttr == null) {
                continue;
            }
            Node attr = getOrCreateAttr(erAttr.name(), owner, graph);
            attr.setNotNull(erAttr.isNotNull());
            attr.setUnique(erAttr.isUnique());
            Domain domain = null;
            if (erAttr.domain() != null) {
                String domainKey = erAttr.domain().toUpperCase();
                String resolved = customDomainMap.getOrDefault(domainKey, domainKey);
                try {
                    domain = Domain.valueOf(resolved);
                } catch (Exception ignored) {
                }
            }
            if (domain != null) {
                attr.setDataType(
                        erAttr.size() > 0
                                ? DataType.of(domain, erAttr.size())
                                : DataType.of(domain));
            }
            if (erAttr.isKey()) {
                addPrimaryAttr(attr, owner, graph);
            } else {
                addEdge(owner, attr, graph);
            }
        }
    }

    private void processOwnAttributes(
            ErEntityDTO entity,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        Node node = getOrCreateNode(entity.name(), graph);
        processAttributes(node, entity.attributes(), attributeDTOMap, customDomainMap, graph);
        processAttributes(node, entity.primaryKeys(), attributeDTOMap, customDomainMap, graph);
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<ErEntityDTO> entities = new ArrayList<>();
        List<ErRelationshipDTO> relationships = new ArrayList<>();
        List<ErAttributeDTO> attributes = new ArrayList<>();
        List<ErUndefinedDTO> undefineds = new ArrayList<>();

        Set<Node> nodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .collect(Collectors.toSet());

        Set<String> visited = new HashSet<>();
        List<Node> isaChildren = new ArrayList<>();

        int total = nodes.size();
        int idx = 0;
        for (Node node : nodes) {
            Position pos = circlePos(new Position(0, 0), idx++, total, Math.max(200, total * 80));
            List<Node> ownAttrs =
                    Graphs.successorListOf(graph, node).stream().filter(Node::isAttribute).toList();

            if (ownAttrs.isEmpty()) {
                undefineds.add(new ErUndefinedDTO(node.getUuid(), node.getName(), pos, List.of()));
                continue;
            }
            if (NodeClassifier.isIsA(node, graph)) {
                isaChildren.add(node);
            } else if (NodeClassifier.isNMRel(node, graph)) {
                long distinctRefs =
                        Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.getReference() != null)
                                .map(Node::getReference)
                                .distinct()
                                .count();
                if (distinctRefs > 2) {
                    buildNArias(node, graph, visited, pos, relationships, entities, attributes);
                } else {
                    buildNMRel(node, graph, visited, pos, relationships, entities, attributes);
                }
            } else if (NodeClassifier.isEntity(node, graph)) {
                Set<Node> uniqueFks = NodeClassifier.hasUniqueFk(node, graph);
                Set<Node> plainFks =
                        Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && !a.isUnique()
                                                        && !a.isPk())
                                .collect(Collectors.toSet());
                if (uniqueFks.isEmpty() && plainFks.isEmpty()) {
                    buildEntity(node, graph, visited, pos, entities, attributes);
                } else if (uniqueFks.isEmpty()) {
                    plainFks.forEach(
                            fk -> {
                                graph.vertexSet().stream()
                                        .filter(n -> n.getName().equals(fk.getReference()))
                                        .findFirst()
                                        .ifPresent(
                                                ref ->
                                                        buildOneNRel(
                                                                node,
                                                                ref,
                                                                graph,
                                                                visited,
                                                                pos,
                                                                relationships,
                                                                entities,
                                                                attributes));
                            });
                } else {
                    uniqueFks.forEach(
                            fk -> {
                                graph.vertexSet().stream()
                                        .filter(n -> n.getName().equals(fk.getReference()))
                                        .findFirst()
                                        .ifPresent(
                                                ref -> {
                                                    Node first =
                                                            node.getName().compareTo(ref.getName())
                                                                            <= 0
                                                                    ? node
                                                                    : ref;
                                                    Node second = first == node ? ref : node;
                                                    buildOneOneRel(
                                                            first,
                                                            second,
                                                            graph,
                                                            visited,
                                                            pos,
                                                            relationships,
                                                            entities,
                                                            attributes);
                                                });
                            });
                }
            } else {
                undefineds.add(new ErUndefinedDTO(node.getUuid(), node.getName(), pos, List.of()));
            }
        }

        Map<String, List<Node>> isaByParent = new java.util.LinkedHashMap<>();
        for (Node child : isaChildren) {
            String parentName =
                    Graphs.successorListOf(graph, child).stream()
                            .filter(a -> a.isAttribute() && a.isFk())
                            .map(Node::getReference)
                            .findFirst()
                            .orElse(null);
            if (parentName != null) {
                isaByParent.computeIfAbsent(parentName, k -> new ArrayList<>()).add(child);
            }
        }
        for (Map.Entry<String, List<Node>> entry : isaByParent.entrySet()) {
            Position isaPos =
                    circlePos(new Position(0, 0), idx++, total, Math.max(200, total * 80));
            buildIsA(
                    entry.getKey(),
                    entry.getValue(),
                    graph,
                    visited,
                    isaPos,
                    relationships,
                    entities,
                    attributes);
        }

        return new ErInput(entities, relationships, attributes, List.of(), undefineds);
    }

    private void buildEntity(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());

            List<String> attrs = new ArrayList<>();
            List<String> pks = new ArrayList<>();

            List<Node> ownAttrList =
                    Graphs.successorListOf(graph, node).stream()
                            .filter(n -> n.isAttribute() && !n.isFk())
                            .toList();
            int attrTotal = ownAttrList.size();
            AtomicInteger attrIdx = new AtomicInteger(0);
            ownAttrList.forEach(
                    attr -> {
                        Position attrPos = circlePos(pos, attrIdx.getAndIncrement(), attrTotal, 80);
                        attributes.add(
                                new ErAttributeDTO(
                                        attr.getUuid(),
                                        attr.getName(),
                                        attrPos,
                                        node.getUuid(),
                                        attr.isPk(),
                                        false,
                                        false,
                                        attr.isNotNull(),
                                        attr.isUnique(),
                                        attr.getDataType() != null
                                                ? attr.getDataType().domain().name()
                                                : null,
                                        attr.getDataType() != null
                                                ? attr.getDataType().length()
                                                : 0,
                                        List.of()));
                        if (attr.isPk()) {
                            pks.add(attr.getUuid());
                        } else {
                            attrs.add(attr.getUuid());
                        }
                    });

            entities.add(new ErEntityDTO(node.getUuid(), node.getName(), pos, false, attrs, pks));
        }
    }

    private void buildIsA(
            String parentName,
            List<Node> children,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        Node parent = getOrCreateNode(parentName, graph);
        Position parentPos = circlePos(pos, 0, children.size() + 1, 150);
        buildEntity(parent, graph, visited, parentPos, entities, attributes);

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
        participants.add(new ErRelationshipParticipantDTO(parent.getUuid(), "", "", "Parent"));

        int childIdx = 1;
        for (Node child : children) {
            if (visited.contains(child.getUuid())) {
                continue;
            }
            visited.add(child.getUuid());

            List<Node> childOwnAttrs =
                    Graphs.successorListOf(graph, child).stream()
                            .filter(a -> a.isAttribute() && !a.isFk())
                            .toList();
            List<Node> childFkPkAttrs =
                    Graphs.successorListOf(graph, child).stream()
                            .filter(a -> a.isAttribute() && a.isFk() && a.isPk())
                            .toList();

            List<String> childAttrIds = new ArrayList<>();
            List<String> childPkIds = new ArrayList<>();
            int attrTotal = childOwnAttrs.size() + childFkPkAttrs.size();
            AtomicInteger attrIdx = new AtomicInteger(0);

            Position childPos = circlePos(pos, childIdx++, children.size() + 1, 150);

            for (Node fkPk : childFkPkAttrs) {
                if (fkPk.getReference() == null) {
                    Position aPos = circlePos(childPos, attrIdx.getAndIncrement(), attrTotal, 80);
                    attributes.add(
                            new ErAttributeDTO(
                                    fkPk.getUuid(),
                                    fkPk.getName(),
                                    aPos,
                                    child.getUuid(),
                                    true,
                                    false,
                                    false,
                                    fkPk.isNotNull(),
                                    fkPk.isUnique(),
                                    fkPk.getDataType() != null
                                            ? fkPk.getDataType().domain().name()
                                            : null,
                                    fkPk.getDataType() != null ? fkPk.getDataType().length() : 0,
                                    List.of()));
                    childPkIds.add(fkPk.getUuid());
                }
            }
            for (Node attr : childOwnAttrs) {
                Position aPos = circlePos(childPos, attrIdx.getAndIncrement(), attrTotal, 80);
                attributes.add(
                        new ErAttributeDTO(
                                attr.getUuid(),
                                attr.getName(),
                                aPos,
                                child.getUuid(),
                                false,
                                false,
                                false,
                                attr.isNotNull(),
                                attr.isUnique(),
                                attr.getDataType() != null
                                        ? attr.getDataType().domain().name()
                                        : null,
                                attr.getDataType() != null ? attr.getDataType().length() : 0,
                                List.of()));
                childAttrIds.add(attr.getUuid());
            }

            entities.add(
                    new ErEntityDTO(
                            child.getUuid(),
                            child.getName(),
                            childPos,
                            false,
                            childAttrIds,
                            childPkIds));
            participants.add(new ErRelationshipParticipantDTO(child.getUuid(), "", "", "Child"));
        }

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        "IsA",
                        pos,
                        "IsA",
                        null,
                        participants,
                        List.of()));
    }

    private void buildNArias(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid())) {
            return;
        }
        visited.add(node.getUuid());

        List<Node> ownAttrsList =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(a -> a.isAttribute() && !a.isPk())
                        .toList();
        List<String> ownAttrsId = new ArrayList<>();
        AtomicInteger ownIdx = new AtomicInteger(0);
        ownAttrsList.forEach(
                a -> {
                    Position aPos =
                            circlePos(pos, ownIdx.getAndIncrement(), ownAttrsList.size(), 80);
                    attributes.add(
                            new ErAttributeDTO(
                                    a.getUuid(),
                                    a.getName(),
                                    aPos,
                                    node.getUuid(),
                                    a.isPk(),
                                    false,
                                    false,
                                    a.isNotNull(),
                                    a.isUnique(),
                                    a.getDataType() != null
                                            ? a.getDataType().domain().name()
                                            : null,
                                    a.getDataType() != null ? a.getDataType().length() : 0,
                                    List.of()));
                    ownAttrsId.add(a.getUuid());
                });

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
        Graphs.successorListOf(graph, node).stream()
                .filter(a -> a.isAttribute() && a.isPk() && a.isFk())
                .toList()
                .forEach(
                        attr ->
                                buildParticipants(
                                        attr,
                                        graph,
                                        visited,
                                        pos,
                                        participants,
                                        entities,
                                        attributes));

        relationships.add(
                new ErRelationshipDTO(
                        node.getUuid(),
                        node.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        ownAttrsId));
    }

    private void buildNMRel(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());

            List<String> ownAttrsId = new ArrayList<>();

            List<Node> ownAttrsList =
                    Graphs.neighborListOf(graph, node).stream()
                            .filter(attr -> attr.isAttribute() && !attr.isPk())
                            .distinct()
                            .toList();
            int nmAttrTotal = ownAttrsList.size();
            AtomicInteger nmAttrIdx = new AtomicInteger(0);
            ownAttrsList.forEach(
                    a -> {
                        Position aPos =
                                circlePos(pos, nmAttrIdx.getAndIncrement(), nmAttrTotal, 80);
                        attributes.add(
                                new ErAttributeDTO(
                                        a.getUuid(),
                                        a.getName(),
                                        aPos,
                                        node.getUuid(),
                                        a.isPk(),
                                        false,
                                        false,
                                        a.isNotNull(),
                                        a.isUnique(),
                                        a.getDataType() != null
                                                ? a.getDataType().domain().name()
                                                : null,
                                        a.getDataType() != null ? a.getDataType().length() : 0,
                                        List.of()));

                        ownAttrsId.add(a.getUuid());
                    });

            List<ErRelationshipParticipantDTO> participants = new ArrayList<>();

            Graphs.successorListOf(graph, node).stream()
                    .filter(Node::isPk)
                    .collect(Collectors.toSet())
                    .forEach(
                            attr ->
                                    buildParticipants(
                                            attr,
                                            graph,
                                            visited,
                                            pos,
                                            participants,
                                            entities,
                                            attributes));

            relationships.add(
                    new ErRelationshipDTO(
                            node.getUuid(),
                            node.getName(),
                            pos,
                            "Normal",
                            null,
                            participants,
                            ownAttrsId));
        }
    }

    private void buildParticipants(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipParticipantDTO> participants,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {
        Node entity =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(node.getReference()))
                        .findFirst()
                        .orElse(null);

        if (entity != null) {
            participants.add(new ErRelationshipParticipantDTO(entity.getUuid(), "0", "n", ""));

            int pIdx = participants.size() - 1;
            Position entityPos = circlePos(pos, pIdx, Math.max(participants.size(), 2), 120);
            buildEntity(entity, graph, visited, entityPos, entities, attributes);
        }
    }

    private void buildOneOneRel(
            Node node,
            Node ref,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid()) && visited.contains(ref.getUuid())) {
            return;
        }

        String minNode =
                Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.isUnique()
                                                        && ref.getName().equals(a.getReference()))
                                .anyMatch(Node::isNotNull)
                        ? "1"
                        : "0";
        String minRef =
                Graphs.successorListOf(graph, ref).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.isUnique()
                                                        && node.getName().equals(a.getReference()))
                                .anyMatch(Node::isNotNull)
                        ? "1"
                        : "0";

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
        participants.add(new ErRelationshipParticipantDTO(node.getUuid(), minNode, "1", ""));
        participants.add(new ErRelationshipParticipantDTO(ref.getUuid(), minRef, "1", ""));

        buildEntity(node, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);
        buildEntity(ref, graph, visited, circlePos(pos, 1, 2, 120), entities, attributes);

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        node.getName() + ref.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        List.of()));
    }

    private void buildOneNRel(
            Node node,
            Node ref,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid()) && visited.contains(ref.getUuid())) {
            return;
        }

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();

        String min =
                Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.getReference() != null
                                                        && a.getReference().equals(ref.getName())
                                                        && a.isNotNull())
                                .collect(Collectors.toSet())
                                .isEmpty()
                        ? "0"
                        : "1";
        participants.add(new ErRelationshipParticipantDTO(node.getUuid(), min, "n", ""));

        participants.add(new ErRelationshipParticipantDTO(ref.getUuid(), "0", "1", ""));

        buildEntity(node, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);
        buildEntity(ref, graph, visited, circlePos(pos, 1, 2, 120), entities, attributes);

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        node.getName() + ref.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        List.of()));
    }

    private Position circlePos(Position center, int index, int total, double radius) {
        if (total == 0) {
            return center;
        }
        double angle = Math.PI * index / total;
        return new Position(
                center.x() + radius * Math.cos(angle), center.y() + radius * Math.sin(angle));
    }
}
