package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Participant;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Pendiente de implementación")
class DBDiagramStrategyTest {

    private DBDiagramStrategy strategy;
    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new DBDiagramStrategy();

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
                Attribute.builder().name("E").pk(true).dataType(Domain.INTEGER).fk("A").build();
        Attribute attribute4 = Attribute.builder().name("F").build();

        graph.addVertex(attribute3);
        graph.addVertex(attribute4);
        graph.addEdge(entity2, attribute3);
        graph.addEdge(entity2, attribute4);
        graph.addEdge(attribute3, entity1);

        diagram = Diagram.builder().diagram(graph).build();
    }

    @Test
    void testGenerateGraphWithSingleTable() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    B INTEGER PRIMARY KEY,
                                    C VARCHAR UNIQUE NOT NULL
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        assertNotNull(diagram.getDiagram());

        Node node =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElse(null);

        assertNotNull(node);
        assertInstanceOf(Entity.class, node);

        BreadthFirstIterator<Node, Edge> bfs =
                new BreadthFirstIterator<>(diagram.getDiagram(), node);
        bfs.next();

        while (bfs.hasNext()) {
            node = bfs.next();
            assertInstanceOf(Attribute.class, node);
            Attribute attribute = (Attribute) node;

            if (attribute.getName().equals("B")) {
                assertTrue(attribute.isPk());
                assertEquals(Domain.INTEGER, attribute.getDataType());
            } else if (attribute.getName().equals("C")) {
                assertFalse(attribute.isPk());
                assertTrue(attribute.isNoEmpty());
                assertTrue(attribute.isUnique());
                assertEquals(Domain.VARCHAR, attribute.getDataType());
            }
        }
    }

    @Test
    void testGeneratePrimaryKey() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    B INTEGER PRIMARY KEY,
                                    C VARCHAR
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Attribute pk =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n instanceof Attribute && n.getName().equals("B"))
                                .findFirst()
                                .orElseThrow();
        assertTrue(pk.isPk());
    }

    @Test
    void testGenerateGraphWithMultipleTables() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    C INTEGER PRIMARY KEY
                                );

                                CREATE TABLE B(
                                    D INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        assertEquals(
                2,
                diagram.getDiagram().vertexSet().stream().filter(n -> n instanceof Entity).count());
    }

    @Test
    void testGenerateGraphWithForeignKey() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    C INTEGER PRIMARY KEY,
                                    D INTEGER,
                                    FOREIGN KEY (D) REFERENCES B(D)
                                );
                                CREATE TABLE B(
                                    D INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Node target =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElse(null);

        Node source =
                diagram.getDiagram().vertexSet().stream()
                        .filter(
                                n ->
                                        n instanceof Attribute
                                                && n.getName().equals("D")
                                                && ((Attribute) n).getFk() != null)
                        .findFirst()
                        .orElse(null);

        assertTrue(diagram.getDiagram().containsEdge(source, target));
        assertFalse(diagram.getDiagram().containsEdge(target, source));
    }

    @Test
    void testGenerateForeignKeyAttributeHasFkField() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    C INTEGER PRIMARY KEY,
                                    FOREIGN KEY (C) REFERENCES B(C)
                                );
                                CREATE TABLE B(
                                    C INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Attribute fkAttr =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n instanceof Attribute && n.getName().equals("C"))
                                .findFirst()
                                .orElseThrow();
        assertNotNull(fkAttr.getFk());
        assertEquals("B", fkAttr.getFk());
    }

    @Test
    void testThrowExceptionForInvalidSQL() {
        PhysicalInput input = new PhysicalInput("NOT SQL");
        assertThrows(Exception.class, () -> strategy.generate(input));
    }

    @Test
    void testTransformGraphIntoSQL() {
        String sql = strategy.transform(diagram).toString();

        assertTrue(sql.contains("CREATE TABLE"));
        assertTrue(sql.matches("(?s).*CREATE TABLE A\\s*\\(.*\\).*"));
        assertTrue(sql.contains("B INTEGER PRIMARY KEY"));
        assertTrue(sql.contains("C VARCHAR UNIQUE NOT NULL"));
        assertTrue(sql.contains("E INTEGER"));
        assertTrue(sql.contains("FOREIGN KEY (E) REFERENCES A(E)"));
    }

    @Test
    void testTransformUntypedAttributeIntoInterrogation() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("F ?"));
    }

    @Test
    void testTransformNMRelationshipCreatesIntermediateTable() {
        Graph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);

        Entity entity1 = Entity.builder().name("A").build();
        Entity entity2 = Entity.builder().name("B").build();
        graph.addVertex(entity1);
        graph.addVertex(entity2);

        Attribute pk1 = Attribute.builder().name("pkA").pk(true).dataType(Domain.INTEGER).build();
        Attribute pk2 = Attribute.builder().name("pkB").pk(true).dataType(Domain.INTEGER).build();
        graph.addVertex(pk1);
        graph.addVertex(pk2);
        graph.addEdge(entity1, pk1);
        graph.addEdge(entity2, pk2);

        Participant p1 = new Participant();
        p1.setEntity(entity1);
        p1.setCardinality("N");
        Participant p2 = new Participant();
        p2.setEntity(entity2);
        p2.setCardinality("N");

        Relationship rel =
                Relationship.builder()
                        .name("Intermediate")
                        .participants(new ArrayList<>(List.of(p1, p2)))
                        .attributes(new ArrayList<>())
                        .build();
        graph.addVertex(rel);
        graph.addEdge(rel, entity1);
        graph.addEdge(rel, entity2);

        Diagram d = Diagram.builder().diagram(graph).build();
        String sql = strategy.transform(d).toString();

        assertTrue(sql.contains("CREATE TABLE Intermediate"));
        assertTrue(sql.contains("FOREIGN KEY (pkA) REFERENCES A(pkA)"));
        assertTrue(sql.contains("FOREIGN KEY (pkB) REFERENCES B(pkB)"));
    }

    @Test
    void testTransformNonNMRelationshipNotCreatesIntermediateTable() {
        Graph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);

        Entity entity1 = Entity.builder().name("A").build();
        Entity entity2 = Entity.builder().name("B").build();
        graph.addVertex(entity1);
        graph.addVertex(entity2);

        Participant p1 = new Participant();
        p1.setEntity(entity1);
        p1.setCardinality("N");
        Participant p2 = new Participant();
        p2.setEntity(entity2);
        p2.setCardinality("1");

        Relationship rel =
                Relationship.builder()
                        .name("NotIntermediate")
                        .participants(new ArrayList<>(List.of(p1, p2)))
                        .attributes(new ArrayList<>())
                        .build();
        graph.addVertex(rel);
        graph.addEdge(rel, entity1);
        graph.addEdge(rel, entity2);

        Diagram d = Diagram.builder().diagram(graph).build();
        String sql = strategy.transform(d).toString();

        assertFalse(sql.contains("CREATE TABLE NotIntermediate"));
    }

    @Test
    void testTransformNoCommaAtTheEnd() {
        String sql = strategy.transform(diagram).toString();
        assertFalse(sql.matches("(?s).*,\\s*\\).*"));
    }

    @Test
    void testRoundTrip() throws Exception {
        PhysicalInput originalInput =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    B INTEGER PRIMARY KEY,
                                    C VARCHAR
                                );
                                """);
        Diagram diagram1 = strategy.generate(originalInput);
        String regeneratedInput = strategy.transform(diagram1).toString();
        Diagram diagram2 = strategy.generate(new PhysicalInput(regeneratedInput));

        assertEquals(
                diagram1.getDiagram().vertexSet().size(), diagram2.getDiagram().vertexSet().size());
        assertEquals(
                diagram1.getDiagram().edgeSet().size(), diagram2.getDiagram().edgeSet().size());

        for (Edge e : diagram1.getDiagram().edgeSet()) {
            Node target = diagram1.getDiagram().getEdgeTarget(e);
            Node source = diagram1.getDiagram().getEdgeSource(e);
            assertTrue(diagram2.getDiagram().containsEdge(source, target));
        }
    }
}
