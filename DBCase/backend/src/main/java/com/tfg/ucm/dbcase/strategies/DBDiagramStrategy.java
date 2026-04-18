package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreate;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isForeignKey;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
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
        Graph<Node, Edge> result = new Multigraph<>(Edge.class);

        for (String statement : diagram.sql().split(";")) {
            if (!statement.isBlank()) {
                parseStatement(statement.trim(), result);
            }
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        StringBuilder sqlBuilder = new StringBuilder();
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<Node> nodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();

        for (Node node : nodes) {
            sqlBuilder.append(buildTable(node, graph));
        }
        return sqlBuilder;
    }

    private String buildTable(Node entity, Graph<Node, Edge> graph) {

        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        Graphs.neighborListOf(graph, entity)
                .forEach(
                        (attr -> {
                            if (attr.isAttribute()) {
                                String dataType =
                                        attr.getDataType() != null
                                                ? attr.getDataType().toString()
                                                : "?";
                                String pk = "";

                                if (attr.isPk()) {
                                    if (dataType.equals("?")) {
                                        dataType = "INTEGER";
                                    }
                                    if (isForeignKey(attr, entity)) {
                                        pk = " ?";
                                        constraints
                                                .append("\tFOREIGN KEY (")
                                                .append(attr.getName())
                                                .append(") REFERENCES ")
                                                .append(attr.getReference())
                                                .append("(")
                                                .append(attr.getName())
                                                .append("),\n");
                                    } else {
                                        pk = " PRIMARY KEY";
                                    }
                                }
                                String unique = attr.isUnique() ? " UNIQUE" : "";
                                String notNull = attr.isNotNull() ? " NOT NULL" : "";
                                columns.append("\t")
                                        .append(attr.getName())
                                        .append(" ")
                                        .append(dataType)
                                        .append(pk)
                                        .append(unique)
                                        .append(notNull)
                                        .append(",\n");
                            }
                        }));

        if (columns.isEmpty()) {
            return "";
        }

        String body = columns.toString() + constraints.toString();
        if (body.endsWith(",\n")) {
            body = body.substring(0, body.length() - 2) + "\n";
        }
        return "CREATE TABLE " + entity.getName() + "(\n" + body + ");\n\n";
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entityName = createTable.getTable().getName();
                Node entity = Node.builder().name(entityName).build();
                diagram.addVertex(entity);
                if (createTable.getIndexes() != null) {
                    parseIndex(createTable.getIndexes(), entity, diagram);
                }
                parseColumns(createTable.getColumnDefinitions(), entity, diagram);
            }
        } catch (JSQLParserException e) {
            throw new Exception("Error en el formato");
        }
    }

    private void parseIndex(List<Index> indexes, Node entity, Graph<Node, Edge> diagram) {
        for (Index index : indexes) {
            boolean isPk = index.getType().equalsIgnoreCase("primary key");
            boolean isFk =
                    index instanceof ForeignKeyIndex
                            || index.getType().equalsIgnoreCase("foreign key");
            String referencedTable;
            if (isFk && index instanceof ForeignKeyIndex fki && fki.getTable() != null) {
                referencedTable = fki.getTable().getName();
            } else if (isPk) {
                referencedTable = entity.getName();
            } else {
                referencedTable = null;
            }
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Node attr = getOrCreate(name, diagram);
                                attr.setAttribute(true);
                                attr.setPk(isPk || isFk);
                                attr.setReference(referencedTable);

                                diagram.addEdge(
                                        entity,
                                        attr,
                                        Edge.builder()
                                                .label("attr" + entity.getName() + name)
                                                .build());
                            });
        }
    }

    private void parseColumns(
            List<ColumnDefinition> columns, Node entity, Graph<Node, Edge> diagram) {
        for (ColumnDefinition col : columns) {
            String name = col.getColumnName();
            String type = col.getColDataType().getDataType().toUpperCase();
            List<String> specs = col.getColumnSpecs();
            boolean isPk = false, isUnique = false, isNotNull = false;
            if (specs != null) {
                Set<String> normalizedSpecs =
                        specs.stream().map(String::toLowerCase).collect(Collectors.toSet());
                isPk = Stream.of("primary", "key").allMatch(normalizedSpecs::contains);
                isUnique = specs.stream().anyMatch(s -> s.equalsIgnoreCase("unique"));
                isNotNull = Stream.of("not", "null").allMatch(normalizedSpecs::contains);
            }
            Domain domain;
            try {
                domain = Domain.valueOf(type);
            } catch (IllegalArgumentException e) {
                domain = null;
            }

            Node attr = getOrCreate(name, diagram);
            attr.setAttribute(true);
            attr.setDataType(domain);
            if (isPk) {
                attr.setPk(true);
            }
            attr.setNotNull(isNotNull);
            attr.setUnique(isUnique);

            if (diagram.getAllEdges(entity, attr).isEmpty()) {
                diagram.addEdge(
                        entity,
                        attr,
                        Edge.builder().label("attr" + entity.getName() + name).build());
            }
        }
    }
}
