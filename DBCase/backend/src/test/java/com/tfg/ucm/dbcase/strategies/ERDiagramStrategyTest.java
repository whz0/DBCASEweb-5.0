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
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Pendiente de implementación")
class ERDiagramStrategyTest {

    private ERDiagramStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ERDiagramStrategy();
    }

    private ErInput input(List<ErEntityDTO> e, List<ErRelationshipDTO> r, List<ErAttributeDTO> a) {
        return new ErInput(e, r, a, List.of(), List.of());
    }

    private Node findByName(Diagram d, String name) {
        return d.getDiagram().vertexSet().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Test
    void generateEntityCreatesNonAttributeNode() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1", "Person", null, false, List.of(), List.of())),
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
                                List.of(
                                        new ErEntityDTO(
                                                "e1",
                                                "Person",
                                                null,
                                                false,
                                                List.of("a1"),
                                                List.of())),
                                List.of(),
                                List.of(
                                        new ErAttributeDTO(
                                                "a1", "name", null, "e1", false, false, false,
                                                false, false, null, 0, List.of()))));

        Graph<Node, Edge> g = d.getDiagram();
        Node entity = findByName(d, "Person");
        Node attr = findByName(d, "name");

        assertTrue(attr.isAttribute());
        assertTrue(g.containsEdge(entity, attr));
    }

    @Test
    void generatePkAttributeSetsPkFlag() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1",
                                                "Person",
                                                null,
                                                false,
                                                List.of("a1"),
                                                List.of("a1"))),
                                List.of(),
                                List.of(
                                        new ErAttributeDTO(
                                                "a1", "id", null, "e1", true, false, false, false,
                                                false, null, 0, List.of()))));

        assertTrue(findByName(d, "id").isPk());
    }

    @Test
    void generateRelationshipCreatesNodeWithEdgesToParticipants() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1", "A", null, false, List.of(), List.of()),
                                        new ErEntityDTO(
                                                "e2", "B", null, false, List.of(), List.of())),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "Works",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "1", "N", null),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "1", "1", null)),
                                                List.of())),
                                List.of()));

        Graph<Node, Edge> g = d.getDiagram();
        Node rel = findByName(d, "Works");
        Node a = findByName(d, "A");
        Node b = findByName(d, "B");

        assertNotNull(rel);
        assertTrue(g.containsEdge(rel, a));
        assertTrue(g.containsEdge(rel, b));
    }

    @Test
    void generateRelationshipEdgeLabelEncodesCardinality() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1", "A", null, false, List.of(), List.of()),
                                        new ErEntityDTO(
                                                "e2", "B", null, false, List.of(), List.of())),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "R",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "1", "N", "boss"),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "0", "1", null)),
                                                List.of())),
                                List.of()));

        Graph<Node, Edge> g = d.getDiagram();
        Node rel = findByName(d, "R");
        Node a = findByName(d, "A");
        Edge edge = g.getEdge(rel, a);

        assertNotNull(edge);
        assertTrue(edge.getLabel().contains("N"));
        assertTrue(edge.getLabel().contains("boss"));
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
    void transformNodeWithRelEdgesBecomesRelationship() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1", "A", null, false, List.of(), List.of()),
                                        new ErEntityDTO(
                                                "e2", "B", null, false, List.of(), List.of())),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "R",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "1", "N", null),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "1", "1", null)),
                                                List.of())),
                                List.of()));

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(1, result.relationships().size());
        assertEquals("R", result.relationships().get(0).name());
        assertEquals(2, result.relationships().get(0).participants().size());
    }

    @Test
    void transformParticipantNodeBecomesEntity() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1", "A", null, false, List.of(), List.of()),
                                        new ErEntityDTO(
                                                "e2", "B", null, false, List.of(), List.of())),
                                List.of(
                                        new ErRelationshipDTO(
                                                "r1",
                                                "R",
                                                null,
                                                "Normal",
                                                null,
                                                List.of(
                                                        new ErRelationshipParticipantDTO(
                                                                "e1", "1", "N", null),
                                                        new ErRelationshipParticipantDTO(
                                                                "e2", "1", "1", null)),
                                                List.of())),
                                List.of()));

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(2, result.entities().size());
        assertTrue(result.entities().stream().anyMatch(e -> e.name().equals("A")));
        assertTrue(result.entities().stream().anyMatch(e -> e.name().equals("B")));
    }

    @Test
    void transformIsolatedNodeBecomesUndefined() {
        Graph<Node, Edge> g = new DirectedMultigraph<>(Edge.class);
        g.addVertex(Node.builder().name("Ghost").build());
        Diagram d = Diagram.builder().diagram(g).build();

        ErInput result = (ErInput) strategy.transform(d);

        assertEquals(1, result.undefineds().size());
        assertEquals("Ghost", result.undefineds().get(0).name());
        assertEquals(0, result.entities().size());
        assertEquals(0, result.relationships().size());
    }

    @Test
    void transformAttributesPreserveFlags() {
        Diagram d =
                strategy.generate(
                        input(
                                List.of(
                                        new ErEntityDTO(
                                                "e1",
                                                "A",
                                                null,
                                                false,
                                                List.of("a1"),
                                                List.of("a1"))),
                                List.of(),
                                List.of(
                                        new ErAttributeDTO(
                                                "a1", "id", null, "e1", true, false, false, true,
                                                false, null, 0, List.of()))));

        ErInput result = (ErInput) strategy.transform(d);
        ErAttributeDTO attr = result.attributes().get(0);

        assertEquals("id", attr.name());
        assertTrue(attr.isKey());
        assertTrue(attr.isNotNull());
    }

    @Test
    void roundTripEntityWithAttributes() {
        ErInput original =
                input(
                        List.of(
                                new ErEntityDTO(
                                        "e1",
                                        "Person",
                                        null,
                                        false,
                                        List.of("a1", "a2"),
                                        List.of("a1"))),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "id", null, "e1", true, false, false, false, false,
                                        null, 0, List.of()),
                                new ErAttributeDTO(
                                        "a2", "name", null, "e1", false, false, false, false, false,
                                        null, 0, List.of())));

        ErInput result = (ErInput) strategy.transform(strategy.generate(original));

        assertEquals(1, result.entities().size());
        assertEquals("Person", result.entities().get(0).name());
        assertEquals(2, result.attributes().size());
    }

    @Test
    void roundTripRelationshipCardinalityPreserved() {
        ErInput original =
                input(
                        List.of(
                                new ErEntityDTO("e1", "A", null, false, List.of(), List.of()),
                                new ErEntityDTO("e2", "B", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "R",
                                        null,
                                        "Normal",
                                        null,
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "0", "1", null)),
                                        List.of())),
                        List.of());

        ErInput result = (ErInput) strategy.transform(strategy.generate(original));
        ErRelationshipDTO rel = result.relationships().get(0);

        assertTrue(rel.participants().stream().anyMatch(p -> p.cardinalityMax().equals("N")));
        assertTrue(rel.participants().stream().anyMatch(p -> p.cardinalityMax().equals("1")));
    }
}
