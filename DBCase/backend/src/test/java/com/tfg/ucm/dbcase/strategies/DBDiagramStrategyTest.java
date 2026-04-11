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
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
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
                Attribute.builder().name("E").pk(true).dataType(Domain.INTEGER).build();

        graph.addVertex(attribute3);

        graph.addEdge(entity2, attribute3);

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
                                    C VARCHAR UNIQUE NOT NULL,
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        assertNotNull(diagram.getDiagram());

        Node node =
                diagram.getDiagram().vertexSet().stream()
                        .filter((n -> n.getName().equals("A")))
                        .findFirst()
                        .orElseGet(() -> null);

        assertNotNull(node);
        assertInstanceOf(Entity.class, node);

        Entity entity = (Entity) node;

        BreadthFirstIterator<Node, Edge> bfs =
                new BreadthFirstIterator<>(diagram.getDiagram(), entity);
        bfs.next();

        while (bfs.hasNext()) {
            node = bfs.next();
            assertInstanceOf(Attribute.class, node);
            Attribute attribute = (Attribute) node;

            if (attribute.getName().equals("B")) {
                assertNotNull(attribute.getDataType());
                assertTrue(attribute.isPk());
                assertEquals(Domain.INTEGER, attribute.getDataType());
            } else if (attribute.getName().equals("C")) {
                assertFalse(attribute.isPk());
                assertTrue(attribute.isNoEmpty());
                assertTrue(attribute.isUnique());
                assertNotNull(attribute.getDataType());
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
                                    C VARCHAR,
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

        long entityCount =
                diagram.getDiagram().vertexSet().stream().filter(n -> n instanceof Entity).count();
        assertEquals(2, entityCount);
    }

    @Test
    void testGenerateGraphWithForeignKey() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    C INTEGER PRIMARY KEY,
                                    D INTEGER,
                                    FOREIGN KEY D REFERENCES B(D)
                                );

                                CREATE TABLE B(
                                    D INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Node target =
                diagram.getDiagram().vertexSet().stream()
                        .filter((n -> n.getName().equals("A")))
                        .findFirst()
                        .orElseGet(() -> null);
        Node source =
                diagram.getDiagram().vertexSet().stream()
                        .filter((n -> n.getName().equals("D")))
                        .findFirst()
                        .orElseGet(() -> null);

        assertTrue(diagram.getDiagram().containsEdge(source, target));
        assertFalse(diagram.getDiagram().containsEdge(target, source));
    }

    @Test
    void testThrowExceptionForInvalidSQL() {
        PhysicalInput input = new PhysicalInput("SQL");
        assertThrows(Exception.class, () -> strategy.generate(input));
    }

    @Test
    void testTransformGraphIntoSQL() throws Exception {

        String sql = strategy.transform(diagram).toString();

        assertTrue(sql.contains("CREATE TABLE"));
        assertTrue(sql.matches("(?s).*CREATE TABLE A\\s*\\(.*\\).*"));
        assertTrue(sql.contains("B INTEGER PRIMARY KEY"));
        assertTrue(sql.contains("C VARCHAR UNIQUE NOT NULL"));
        assertTrue(sql.contains("E INTEGER"));
        assertTrue(sql.contains("FOREIGN KEY (E) REFERENCES D(E)"));
    }

    @Test
    void testRoundTrip() throws Exception {
        PhysicalInput OriginalInput =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    B INTEGER PRIMARY KEY,
                                    C VARCHAR
                                );
                                """);
        Diagram diagram1 = strategy.generate(OriginalInput);
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
