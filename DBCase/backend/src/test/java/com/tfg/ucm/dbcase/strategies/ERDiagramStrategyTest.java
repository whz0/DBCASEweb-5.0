package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErUndefinedDTO;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ERDiagramStrategyTest {

    private ERDiagramStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ERDiagramStrategy();
    }

    private ErInput input(
            List<ErEntityDTO> entities,
            List<ErRelationshipDTO> relationships,
            List<ErAttributeDTO> attributes) {
        return new ErInput(entities, relationships, attributes, List.of(), List.of());
    }

    private Node findByName(Diagram d, String name) {
        return d.getDiagram().vertexSet().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private ErAttributeDTO attr(String id, String name, String parentId, boolean isKey) {
        return new ErAttributeDTO(
                id, name, null, parentId, isKey, false, false, false, false, null, 0, List.of());
    }

    private ErEntityDTO entity(String id, String name, List<String> attrs, List<String> pks) {
        return new ErEntityDTO(id, name, null, false, attrs, pks);
    }

    @Test
    void generateEntityCreatesNonAttributeNode() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of(), List.of())),
                                List.of(),
                                List.of()));

        Node n = findByName(d, "Person");
        assertNotNull(n);
        assertFalse(n.isAttribute());
    }

    @Test
    void generateAttributeCreatesAttributeNodeWithEdgeFromParent() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of("a1"), List.of())),
                                List.of(),
                                List.of(attr("a1", "name", "e1", false))));

        Graph<Node, Edge> g = d.getDiagram();
        Node entity = findByName(d, "Person");
        Node attrNode = findByName(d, "name");

        assertNotNull(attrNode);
        assertTrue(attrNode.isAttribute());
        assertTrue(g.containsEdge(entity, attrNode));
    }

    @Test
    void generatePkAttributeSetsPkFlag() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of(), List.of("a1"))),
                                List.of(),
                                List.of(attr("a1", "id", "e1", true))));

        Node pk = findByName(d, "id");
        assertNotNull(pk);
        assertTrue(pk.isPk());
    }

    @Test
    void generateCompositePkExpandsIntoIndividualPks() {

        ErAttributeDTO comp =
                new ErAttributeDTO(
                        "comp1",
                        "id1+id2",
                        null,
                        "e1",
                        true,
                        true,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of("pk1", "pk2"));
        ErAttributeDTO pk1 = attr("pk1", "id1", "comp1", true);
        ErAttributeDTO pk2 = attr("pk2", "id2", "comp1", true);

        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of(), List.of("comp1"))),
                                List.of(),
                                List.of(comp, pk1, pk2)));

        Graph<Node, Edge> g = d.getDiagram();
        Node entity = findByName(d, "Person");

        long pkCount =
                Graphs.successorListOf(g, entity).stream()
                        .filter(n -> n.isAttribute() && n.isPk())
                        .count();
        assertEquals(2, pkCount);
        assertNotNull(findByName(d, "id1"));
        assertNotNull(findByName(d, "id2"));
    }

    @Test
    void generateUndefinedCreatesNonAttributeNode() {
        ErInput erInput =
                new ErInput(
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new ErUndefinedDTO("u1", "Mystery", null, List.of())));

        Diagram d = strategy.generate(erInput);
        Node n = findByName(d, "Mystery");

        assertNotNull(n);
        assertFalse(n.isAttribute());
    }

    @Test
    void generateOneNAddsFkToNSide() {

        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        entity("e1", "A", List.of(), List.of("a1")),
                                        entity("e2", "B", List.of(), List.of("b1"))),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "Works",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "0", "1", ""),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "0", "N", "")),
                                                List.of())),
                                List.of(
                                        attr("a1", "idA", "e1", true),
                                        attr("b1", "idB", "e2", true))));

        Graph<Node, Edge> g = d.getDiagram();
        Node b = findByName(d, "B");
        assertNotNull(b);

        boolean hasFkToA =
                Graphs.successorListOf(g, b).stream()
                        .anyMatch(n -> n.isAttribute() && n.isFk() && "A".equals(n.getReference()));
        assertTrue(hasFkToA);
    }

    @Test
    void generateNMCreatesIntermediateNodeWithTwoFks() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        entity("e1", "A", List.of(), List.of("a1")),
                                        entity("e2", "B", List.of(), List.of("b1"))),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "Rel",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "0", "N", ""),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "0", "N", "")),
                                                List.of())),
                                List.of(
                                        attr("a1", "idA", "e1", true),
                                        attr("b1", "idB", "e2", true))));

        Graph<Node, Edge> g = d.getDiagram();
        Node rel = findByName(d, "Rel");
        assertNotNull(rel);

        long fkPks =
                Graphs.successorListOf(g, rel).stream()
                        .filter(n -> n.isAttribute() && n.isFk() && n.isPk())
                        .count();
        assertEquals(2, fkPks);
    }

    @Test
    void generateNMWithCompositePkPropagatesAllComponents() {
        ErAttributeDTO comp =
                new ErAttributeDTO(
                        "comp1",
                        "id1+id2",
                        null,
                        "e1",
                        true,
                        true,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of("pk1", "pk2"));
        ErAttributeDTO pk1 = attr("pk1", "id1", "comp1", true);
        ErAttributeDTO pk2 = attr("pk2", "id2", "comp1", true);
        ErAttributeDTO bPk = attr("b1", "idB", "e2", true);

        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        entity("e1", "A", List.of(), List.of("comp1")),
                                        entity("e2", "B", List.of(), List.of("b1"))),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "Rel",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "0", "N", ""),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "0", "N", "")),
                                                List.of())),
                                List.of(comp, pk1, pk2, bPk)));

        Graph<Node, Edge> g = d.getDiagram();
        Node rel = findByName(d, "Rel");
        assertNotNull(rel);

        long fkPks =
                Graphs.successorListOf(g, rel).stream()
                        .filter(n -> n.isAttribute() && n.isFk() && n.isPk())
                        .count();
        assertEquals(3, fkPks);
    }

    @Test
    void transformSinglePkEntityEmitsSinglePkInList() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of(), List.of("a1"))),
                                List.of(),
                                List.of(attr("a1", "id", "e1", true))));

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(1, result.entities().size());
        ErEntityDTO ent = result.entities().get(0);
        assertEquals(1, ent.primaryKeys().size());
    }

    @Test
    void transformMultiplePkEntityEmitsCompositePkNode() {
        ErAttributeDTO comp =
                new ErAttributeDTO(
                        "comp1",
                        "id1+id2",
                        null,
                        "e1",
                        true,
                        true,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of("pk1", "pk2"));
        ErAttributeDTO pk1 = attr("pk1", "id1", "comp1", true);
        ErAttributeDTO pk2 = attr("pk2", "id2", "comp1", true);

        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Person", List.of(), List.of("comp1"))),
                                List.of(),
                                List.of(comp, pk1, pk2)));

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(1, result.entities().size());
        ErEntityDTO ent = result.entities().get(0);
        assertEquals(1, ent.primaryKeys().size());

        String compositeId = ent.primaryKeys().get(0);
        ErAttributeDTO compositeAttr =
                result.attributes().stream()
                        .filter(a -> a.id().equals(compositeId))
                        .findFirst()
                        .orElse(null);
        assertNotNull(compositeAttr);
        assertTrue(compositeAttr.isComposite());
        assertEquals(2, compositeAttr.components().size());
    }

    @Test
    void transformIsolatedNodeBecomesUndefined() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "Ghost", List.of(), List.of())),
                                List.of(),
                                List.of()));

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(1, result.undefineds().size());
        assertEquals("Ghost", result.undefineds().get(0).name());
    }

    @Test
    void transformAttributeFlagsPreserved() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(entity("e1", "A", List.of("a1"), List.of("pk1"))),
                                List.of(),
                                List.of(
                                        attr("pk1", "id", "e1", true),
                                        new ErAttributeDTO(
                                                "a1", "desc", null, "e1", false, false, false, true,
                                                false, null, 0, List.of()))));

        ErInput result = (ErInput) strategy.transform(d);

        ErAttributeDTO descAttr =
                result.attributes().stream()
                        .filter(a -> a.name().equals("desc"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(descAttr);
        assertTrue(descAttr.isNotNull());
        assertFalse(descAttr.isKey());
    }

    @Test
    void transformOneNReconstructsRelationship() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        entity("e1", "A", List.of(), List.of("a1")),
                                        entity("e2", "B", List.of(), List.of("b1"))),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "Works",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "0", "1", ""),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "0", "N", "")),
                                                List.of())),
                                List.of(
                                        attr("a1", "idA", "e1", true),
                                        attr("b1", "idB", "e2", true))));

        ErInput result = (ErInput) strategy.transform(d);

        assertTrue(
                result.relationships().stream()
                        .anyMatch(
                                r ->
                                        r.name().equals("Works")
                                                || r.participants().stream()
                                                        .anyMatch(
                                                                p ->
                                                                        p.cardinalityMax()
                                                                                .equalsIgnoreCase(
                                                                                        "N"))));
        assertEquals(2, result.entities().size());
    }

    @Test
    void roundTripEntityWithRegularAttributes() {
        ErInput original =
                input(
                        List.of(entity("e1", "Person", List.of("a2"), List.of("a1"))),
                        List.of(),
                        List.of(attr("a1", "id", "e1", true), attr("a2", "name", "e1", false)));

        ErInput result = (ErInput) strategy.transform(strategy.generate(original));

        assertEquals(1, result.entities().size());
        assertEquals("Person", result.entities().get(0).name());
        assertEquals(
                2,
                result.attributes().stream()
                        .filter(
                                a ->
                                        a.parentId() != null
                                                && a.parentId()
                                                        .equals(result.entities().get(0).id()))
                        .count());
    }

    @Test
    void roundTripNMRelationship() {
        ErInput original =
                input(
                        List.of(
                                entity("e1", "A", List.of(), List.of("a1")),
                                entity("e2", "B", List.of(), List.of("b1"))),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "Rel",
                                        null,
                                        "Normal",
                                        null,
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "0", "N", ""),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "0", "N", "")),
                                        List.of())),
                        List.of(attr("a1", "idA", "e1", true), attr("b1", "idB", "e2", true)));

        ErInput result = (ErInput) strategy.transform(strategy.generate(original));

        assertEquals(1, result.relationships().size());
        assertEquals(2, result.relationships().get(0).participants().size());
        assertTrue(
                result.relationships().get(0).participants().stream()
                        .allMatch(p -> p.cardinalityMax().equalsIgnoreCase("N")));
    }
}
