package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Pendiente de implementación")
class LogicalDiagramStrategyTest {

    private LogicalDiagramStrategy strategy;
    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new LogicalDiagramStrategy();

        Graph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);

        Entity entity1 = Entity.builder().name("A").build();
        graph.addVertex(entity1);

        Attribute attribute1 =
                Attribute.builder().name("B").pk(true).dataType(Domain.INTEGER).build();
        Attribute attribute2 =
                Attribute.builder()
                        .name("C")
                        .unique(true)
                        .noEmpty(true)
                        .dataType(Domain.VARCHAR)
                        .build();

        graph.addVertex(attribute1);
        graph.addVertex(attribute2);

        graph.addEdge(entity1, attribute1);
        graph.addEdge(entity1, attribute2);

        Entity entity2 = Entity.builder().name("D").build();
        graph.addVertex(entity2);

        Attribute attribute3 =
                Attribute.builder().name("E").pk(true).dataType(Domain.INTEGER).build();

        graph.addVertex(attribute3);

        graph.addEdge(entity2, attribute3);

        graph.addEdge(attribute3, entity1);

        diagram = Diagram.builder().diagram(graph).build();
    }

    @Test
    void testGenerateSingleEntity() throws Exception {
        LogicalInput input = new LogicalInput("A (B, C)", "", "");

        Diagram diagram = strategy.generate(input);

        assertNotNull(diagram.getDiagram());
        assertEquals(
                1,
                diagram.getDiagram().vertexSet().stream().filter(n -> n instanceof Entity).count());
        assertEquals(
                2,
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n instanceof Attribute)
                        .count());
        assertTrue(
                diagram.getDiagram().vertexSet().stream()
                        .anyMatch(n -> n instanceof Entity && n.getName().equals("A")));
        assertTrue(
                diagram.getDiagram().vertexSet().stream()
                        .anyMatch(n -> n instanceof Attribute && n.getName().equals("B")));
    }

    @Test
    void testGeneratePkAttribute() throws Exception {
        LogicalInput input = new LogicalInput("A (__B__, C)", "", "");

        Diagram diagram = strategy.generate(input);

        Attribute attribute =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n instanceof Attribute && n.getName().equals("B"))
                                .findFirst()
                                .orElseThrow();
        assertTrue(attribute.isPk());
    }

    @Test
    void testGenerateRelationshipWithRestrictionInput() throws Exception {
        LogicalInput input =
                new LogicalInput(
                        """
                        A (__D__, __E__)
                        B (__D__)
                        C (__E__)
                        """,
                        """
                        A.D -> B.D
                        A.E -> C.E
                        """,
                        "");

        Diagram diagram = strategy.generate(input);

        assertTrue(
                diagram.getDiagram().vertexSet().stream()
                        .anyMatch(n -> n instanceof Relationship && n.getName().equals("A")));
        assertEquals(
                2,
                diagram.getDiagram().vertexSet().stream().filter(n -> n instanceof Entity).count());

        Node target =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElseGet(() -> null);
        Node source =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("D"))
                        .findFirst()
                        .orElseGet(() -> null);

        assertTrue(diagram.getDiagram().containsEdge(source, target));
        assertFalse(diagram.getDiagram().containsEdge(target, source));
    }

    @Test
    void testGenerateGraphWithEmptyInput() throws Exception {
        LogicalInput input = new LogicalInput("", "", "");

        Diagram diagram = strategy.generate(input);

        assertTrue(diagram.getDiagram().vertexSet().isEmpty());
    }

    @Test
    void testTransformGraphIntoLinkedHashMap() throws Exception {

        Object raw = strategy.transform(diagram);
        assertInstanceOf(LinkedHashMap.class, raw);
        strategy.transform(diagram);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result = (LinkedHashMap<String, String>) raw;

        assertNotNull(result.get("relationship"));
        assertNotNull(result.get("restriction"));
        assertNotNull(result.get("lossRestriction"));
    }

    @Test
    void testPkAttributeDataTypeIsInteger() throws Exception {
        LogicalInput input = new LogicalInput("A (__B__)", "", "");
        Diagram d = strategy.generate(input);

        Attribute pk =
                (Attribute)
                        d.getDiagram().vertexSet().stream()
                                .filter(n -> n instanceof Attribute && n.getName().equals("B"))
                                .findFirst()
                                .orElseThrow();

        assertEquals(Domain.INTEGER, pk.getDataType());
    }

    @Test
    void testSharedAttributeCreateOneVertex() throws Exception {
        LogicalInput input =
                new LogicalInput(
                        """
                        A (__C__)
                        B (__C__)
                        """,
                        "",
                        "");
        Diagram d = strategy.generate(input);

        long count =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n instanceof Attribute && n.getName().equals("X"))
                        .count();

        assertEquals(1, count);
    }

    @Test
    void testRoundTripRelationship() throws Exception {
        LogicalInput input = new LogicalInput("A (__B__, C) ", "", "");
        Diagram diagram = strategy.generate(input);

        Object raw = strategy.transform(diagram);
        assertInstanceOf(LinkedHashMap.class, raw);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result = (LinkedHashMap<String, String>) raw;

        assertTrue(result.get("relationship").contains("A"));
        assertTrue(result.get("relationship").contains("__B__"));
        assertTrue(result.get("relationship").contains("C"));
    }

    @Test
    void testRoundTripRestriction() throws Exception {
        LogicalInput input =
                new LogicalInput(
                        """
                        A (__D__, __E__)
                        B (__D__)
                        C (__E__)
                        """,
                        """
                        A.D -> B.D
                        A.E -> C.E
                        """,
                        "");
        Diagram diagram = strategy.generate(input);

        Object raw = strategy.transform(diagram);
        assertInstanceOf(LinkedHashMap.class, raw);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result = (LinkedHashMap<String, String>) raw;

        assertTrue(result.get("restriction").contains("A.D -> B.D"));
        assertTrue(result.get("restriction").contains("A.E -> C.E"));
    }
}
