package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DBDiagramStrategyTest {

    private DBDiagramStrategy strategy;
    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new DBDiagramStrategy();

        Graph<Node, Edge> graph = new Multigraph<>(Edge.class);

        Node entity1 = Node.builder().name("A").build();
        graph.addVertex(entity1);

        Node attribute1 =
                Node.builder().name("b").isAttribute(true).isPk(true).reference("A").build();
        Node attribute2 =
                Node.builder()
                        .name("c")
                        .isAttribute(true)
                        .isUnique(true)
                        .isNotNull(true)
                        .dataType(Domain.VARCHAR)
                        .build();

        graph.addVertex(attribute1);
        graph.addVertex(attribute2);
        graph.addEdge(
                entity1,
                attribute1,
                Edge.builder().label("attr" + entity1.getName() + attribute1.getName()).build());
        graph.addEdge(
                entity1,
                attribute2,
                Edge.builder().label("attr" + entity1.getName() + attribute2.getName()).build());

        Node entity2 = Node.builder().name("D").build();
        graph.addVertex(entity2);

        Node attribute3 =
                Node.builder()
                        .name("e")
                        .isAttribute(true)
                        .isPk(true)
                        .dataType(Domain.INTEGER)
                        .reference("A")
                        .build();
        Node attribute4 = Node.builder().name("f").isAttribute(true).build();

        graph.addVertex(attribute3);
        graph.addVertex(attribute4);
        graph.addEdge(
                entity2,
                attribute3,
                Edge.builder().label("attr" + entity2.getName() + attribute3.getName()).build());
        graph.addEdge(
                entity2,
                attribute4,
                Edge.builder().label("attr" + entity2.getName() + attribute4.getName()).build());
        graph.addEdge(
                entity1,
                attribute3,
                Edge.builder().label("attr" + entity1.getName() + attribute3.getName()).build());

        diagram = Diagram.builder().diagram(graph).build();
    }

    @Test
    void testGenerateGraphWithSingleTable() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    b INTEGER PRIMARY KEY,
                                    c VARCHAR UNIQUE NOT NULL
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
        assertFalse(node.isAttribute());

        BreadthFirstIterator<Node, Edge> bfs =
                new BreadthFirstIterator<>(diagram.getDiagram(), node);
        bfs.next();

        while (bfs.hasNext()) {
            node = bfs.next();
            assertTrue(node.isAttribute());

            if (node.getName().equals("b")) {
                assertTrue(node.isPk());
                assertEquals(Domain.INTEGER, node.getDataType());
            } else if (node.getName().equals("c")) {
                assertFalse(node.isPk());
                assertTrue(node.isNotNull());
                assertTrue(node.isUnique());
                assertEquals(Domain.VARCHAR, node.getDataType());
            }
        }
    }

    @Test
    void testGeneratePrimaryKey() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    b INTEGER PRIMARY KEY,
                                    c VARCHAR
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Node pk =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
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
                                    c INTEGER PRIMARY KEY
                                );

                                CREATE TABLE B(
                                    d INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        assertEquals(
                2, diagram.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count());
    }

    @Test
    void testGenerateGraphWithForeignKey() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    c INTEGER PRIMARY KEY,
                                    d INTEGER,
                                    FOREIGN KEY (d) REFERENCES B(d)
                                );
                                CREATE TABLE B(
                                    d INTEGER PRIMARY KEY
                                );
                                """);

        Diagram diagram = strategy.generate(input);

        Node entity =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElse(null);

        Node attribute =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("d"))
                        .findFirst()
                        .orElseThrow();

        assertTrue(diagram.getDiagram().containsEdge(entity, attribute));
        assertEquals("B", attribute.getReference());
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
        assertTrue(sql.contains("b INTEGER PRIMARY KEY"));
        assertTrue(sql.contains("c VARCHAR UNIQUE NOT NULL"));
        assertTrue(sql.contains("e INTEGER"));
        assertTrue(sql.contains("FOREIGN KEY (e) REFERENCES A(e)"));
    }

    @Test
    void testTransformUntypedAttributeIntoInterrogation() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("f ?"));
    }

    @Test
    void testTransformNoCommaAtTheEnd() {
        String sql = strategy.transform(diagram).toString();
        assertFalse(sql.matches("(?s).*,\\s*\\).*"));
    }

    @Test
    void testTransformPkAttributeIntoInterrogationWhenIsNotReferencedTable() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("e INTEGER ?"));
    }

    @Test
    void testRoundTrip() throws Exception {
        PhysicalInput originalInput =
                new PhysicalInput(
                        """
                                CREATE TABLE A(
                                    b INTEGER PRIMARY KEY,
                                    c VARCHAR
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
