package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.DiagramType;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.PhysicalInput;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;

@Service
public class DBDiagramStrategy implements DiagramStrategy<PhysicalInput> {
    @Override
    public DiagramType getType() {
        return DiagramType.DB;
    }

    @Override
    public Class<PhysicalInput> getInputType() {
        return PhysicalInput.class;
    }

    @Override
    public Diagram generate(PhysicalInput diagram) throws Exception {

        Graph<Node, Edge> result = new DirectedMultigraph<>(Edge.class);

        String[] statements = diagram.sql().split(";");
        Iterator<String> i = Arrays.stream(statements).iterator();
        while (i.hasNext()) {
            String statement = i.next();
            parseStatement(statement, result);
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {

        StringBuilder sqlBuilder = new StringBuilder();
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<Node> entities =
                graph.vertexSet().stream().filter(n -> !(n instanceof Attribute)).toList();

        entities.forEach(
                ent -> {
                    String table = "CREATE TABLE " + ent.getName() + "(\n";
                    for (Edge edge : graph.edgesOf(ent)) {
                        Node node = graph.getEdgeTarget(edge);
                        if (node instanceof Attribute attribute) {
                            String dataType = attribute.getDataType().toString();
                            String primary = attribute.isPk() ? " PRIMARY KEY" : "";
                            String notNull = attribute.isNoEmpty() ? " NOT NULL" : "";
                            String unique = attribute.isUnique() ? " UNIQUE" : "";
                            table =
                                    table.concat(
                                            node.getName()
                                                    + " "
                                                    + dataType
                                                    + primary
                                                    + notNull
                                                    + unique
                                                    + ",\n");
                        }
                    }
                    table = table.concat(");\n\n");
                    sqlBuilder.append(table);
                });

        return sqlBuilder;
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entityName = createTable.getTable().getName();
                Node entity = Entity.builder().name(entityName).build();
                diagram.addVertex(entity);
                parseColumns(createTable.getColumnDefinitions(), entity, diagram);
                if (createTable.getIndexes() != null) {
                    parseIndex(createTable.getIndexes(), entity, diagram);
                }
            }
        } catch (JSQLParserException e) {
            throw new Exception("Error en el formato");
        }
    }

    private void parseIndex(List<Index> index, Node entity, Graph<Node, Edge> diagram) {
        for (Index i : index) {
            boolean isPk = i.getType().toLowerCase().contentEquals("primary key");
            boolean isFk = i.getType().toLowerCase().contentEquals("foreign key");
            i.getColumns()
                    .forEach(
                            (column) -> {
                                String name = column.getColumnName();
                                Node node =
                                        diagram.getEdgeTarget(
                                                Edge.builder()
                                                        .label(
                                                                "attribute"
                                                                        + name
                                                                        + entity.getName())
                                                        .build());
                                if (node instanceof Attribute attribute) {
                                    attribute.toBuilder().pk(isPk).fk(isFk).build();
                                }
                            });
        }
    }

    private void parseColumns(
            List<ColumnDefinition> columns, Node entity, Graph<Node, Edge> diagram) {
        for (ColumnDefinition col : columns) {
            String name = col.getColumnName();
            String type = col.getColDataType().getDataType();
            List<String> specs = col.getColumnSpecs();
            boolean isPk = false;
            boolean isUnique = false;
            boolean isNotNull = false;
            if (specs != null) {
                Set<String> normalizedSpecs =
                        specs.stream().map(String::toLowerCase).collect(Collectors.toSet());
                isPk = Stream.of("primary", "key").allMatch(normalizedSpecs::contains);
                isUnique = specs.stream().anyMatch(s -> s.equalsIgnoreCase("unique"));
                isNotNull = Stream.of("not", "null").allMatch(normalizedSpecs::contains);
            }
            Node attribute =
                    Attribute.builder()
                            .name(name)
                            .dataType(Domain.valueOf(type))
                            .pk(isPk)
                            .unique(isUnique)
                            .noEmpty(isNotNull)
                            .build();
            diagram.addVertex(attribute);
            diagram.addEdge(
                    entity,
                    attribute,
                    Edge.builder().label("attribute" + name + entity.getName()).build());
        }
    }
}
