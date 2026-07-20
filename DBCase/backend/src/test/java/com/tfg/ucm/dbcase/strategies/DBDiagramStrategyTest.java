package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DBDiagramStrategyTest {

    private DBDiagramStrategy strategy;

    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new DBDiagramStrategy();
        try {
            diagram =
                    strategy.generate(
                            new PhysicalInput(
                                    """
                    CREATE TABLE A(
                        b INTEGER PRIMARY KEY,
                        c VARCHAR UNIQUE NOT NULL
                    );
                    CREATE TABLE D(
                        e INTEGER,
                        f VARCHAR,
                        PRIMARY KEY (e),
                        FOREIGN KEY (e) REFERENCES A(b)
                    );
                    """));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to build test diagram", ex);
        }
    }

    @Test
    void generateSingleTableCreatesEntityNode() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    b INTEGER PRIMARY KEY,
                    c VARCHAR UNIQUE NOT NULL
                );
                """);

        Diagram d = strategy.generate(input);
        assertNotNull(d.getDiagram());

        Node node =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(node);
        assertFalse(node.isAttribute());
    }

    @Test
    void generateSingleTableAttributeFlags() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    b INTEGER PRIMARY KEY,
                    c VARCHAR UNIQUE NOT NULL
                );
                """);

        Diagram d = strategy.generate(input);

        Node pk =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(pk.isPk());
        assertEquals(Domain.INTEGER, pk.getDataType().domain());

        Node unique =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("c"))
                        .findFirst()
                        .orElseThrow();
        assertFalse(unique.isPk());
        assertTrue(unique.isUnique());
        assertTrue(unique.isNotNull());
        assertEquals(Domain.VARCHAR, unique.getDataType().domain());
    }

    @Test
    void generateMultipleTablesCreatesMultipleEntities() throws Exception {
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

        Diagram d = strategy.generate(input);
        assertEquals(2, d.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count());
    }

    @Test
    void generateForeignKeySetsFkFlag() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE B(
                    d INTEGER PRIMARY KEY
                );
                CREATE TABLE A(
                    c INTEGER PRIMARY KEY,
                    d INTEGER,
                    FOREIGN KEY (d) REFERENCES B(d)
                );
                """);

        Diagram d = strategy.generate(input);

        Node fkAttr =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.isFk() && "B".equals(n.getReference()))
                        .findFirst()
                        .orElse(null);
        assertNotNull(fkAttr);
        assertEquals("d", fkAttr.getName());
    }

    @Test
    void generateCompositeForeignKeyCreatesTwoFkAttributes() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    id1 INTEGER,
                    id2 INTEGER,
                    PRIMARY KEY (id1, id2)
                );
                CREATE TABLE B(
                    a_id1 INTEGER,
                    a_id2 INTEGER,
                    extra VARCHAR,
                    PRIMARY KEY (a_id1, a_id2),
                    FOREIGN KEY (a_id1, a_id2) REFERENCES A(id1, id2)
                );
                """);

        Diagram d = strategy.generate(input);

        Node entityB =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> !n.isAttribute() && n.getName().equals("B"))
                        .findFirst()
                        .orElseThrow();

        long fksToA =
                Graphs.successorListOf(d.getDiagram(), entityB).stream()
                        .filter(n -> n.isAttribute() && n.isFk() && "A".equals(n.getReference()))
                        .count();
        assertEquals(2, fksToA);
    }

    @Test
    void generateInvalidSqlThrowsException() {
        PhysicalInput input = new PhysicalInput("NOT SQL AT ALL");
        assertThrows(Exception.class, () -> strategy.generate(input));
    }

    @Test
    void transformProducesCreateTableStatements() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("CREATE TABLE A"));
        assertTrue(sql.contains("CREATE TABLE D"));
    }

    @Test
    void transformSinglePkInline() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("b INTEGER PRIMARY KEY"));
    }

    @Test
    void transformNonPkColumnsWithConstraints() {
        String sql = strategy.transform(diagram).toString();
        assertTrue(sql.contains("c VARCHAR UNIQUE NOT NULL"));
    }

    @Test
    void transformFkGroupedInSingleConstraint() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    id1 INTEGER,
                    id2 INTEGER,
                    PRIMARY KEY (id1, id2)
                );
                CREATE TABLE B(
                    a_id1 INTEGER,
                    a_id2 INTEGER,
                    PRIMARY KEY (a_id1, a_id2),
                    FOREIGN KEY (a_id1, a_id2) REFERENCES A(id1, id2)
                );
                """);

        Diagram d = strategy.generate(input);
        String sql = strategy.transform(d).toString();

        assertTrue(sql.contains("FOREIGN KEY (a_id1, a_id2) REFERENCES A(id1, id2)"));
        long fkLines =
                sql.lines()
                        .filter(
                                line ->
                                        line.trim().startsWith("FOREIGN KEY")
                                                && line.contains("REFERENCES A"))
                        .count();
        assertEquals(1, fkLines);
    }

    @Test
    void transformCompositePkUsesConstraint() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    id1 INTEGER,
                    id2 INTEGER,
                    PRIMARY KEY (id1, id2)
                );
                """);

        Diagram d = strategy.generate(input);
        String sql = strategy.transform(d).toString();

        assertTrue(sql.contains("PRIMARY KEY (id1, id2)"));
        assertFalse(sql.contains("id1 INTEGER PRIMARY KEY"));
    }

    @Test
    void transformNoTrailingCommaBeforeClosingParen() {
        String sql = strategy.transform(diagram).toString();
        assertFalse(sql.matches("(?s).*,\\s*\\).*"));
    }

    @Test
    void roundTripSimpleTable() throws Exception {
        PhysicalInput original =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    b INTEGER PRIMARY KEY,
                    c VARCHAR
                );
                """);

        Diagram d1 = strategy.generate(original);
        String regenerated = strategy.transform(d1).toString();
        Diagram d2 = strategy.generate(new PhysicalInput(regenerated));

        long tables1 = d1.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count();
        long tables2 = d2.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count();
        assertEquals(tables1, tables2);

        long attrs1 = d1.getDiagram().vertexSet().stream().filter(Node::isAttribute).count();
        long attrs2 = d2.getDiagram().vertexSet().stream().filter(Node::isAttribute).count();
        assertEquals(attrs1, attrs2);
    }

    @Test
    void roundTripCompositeFk() throws Exception {
        PhysicalInput original =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    id1 INTEGER,
                    id2 INTEGER,
                    PRIMARY KEY (id1, id2)
                );
                CREATE TABLE B(
                    a_id1 INTEGER,
                    a_id2 INTEGER,
                    PRIMARY KEY (a_id1, a_id2),
                    FOREIGN KEY (a_id1, a_id2) REFERENCES A(id1, id2)
                );
                """);

        Diagram d1 = strategy.generate(original);
        String sql = strategy.transform(d1).toString();
        Diagram d2 = strategy.generate(new PhysicalInput(sql));

        long fks1 =
                d1.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.isFk())
                        .count();
        long fks2 =
                d2.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.isFk())
                        .count();
        assertEquals(fks1, fks2);
    }
}
