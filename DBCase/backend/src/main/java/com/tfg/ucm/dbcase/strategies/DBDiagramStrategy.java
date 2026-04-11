package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.List;
import java.util.Optional;
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
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
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

        List<Node> tables =
                graph.vertexSet().stream()
                        .filter(n -> n instanceof Entity || n instanceof Relationship)
                        .toList();

        for (Node table : tables) {
            StringBuilder columns = new StringBuilder();
            StringBuilder fkConstraints = new StringBuilder();

            BreadthFirstIterator<Node, Edge> bfs = new BreadthFirstIterator<>(graph, table);
            bfs.next();

            while (bfs.hasNext()) {
                Node vertex = bfs.next();
                if (!(vertex instanceof Attribute attr)) {
                    continue;
                }

                if (graph.containsEdge(attr, table)) {
                    String dataType =
                            attr.getDataType() != null ? attr.getDataType().toString() : "VARCHAR";
                    columns.append("  ")
                            .append(attr.getName())
                            .append(" ")
                            .append(dataType)
                            .append(",\n");
                    Graphs.predecessorListOf(graph, attr).stream()
                            .filter(n -> n instanceof Entity || n instanceof Relationship)
                            .map(Node::getName)
                            .findFirst()
                            .ifPresent(
                                    ref ->
                                            fkConstraints
                                                    .append("  FOREIGN KEY (")
                                                    .append(attr.getName())
                                                    .append(") REFERENCES ")
                                                    .append(ref)
                                                    .append("(")
                                                    .append(attr.getName())
                                                    .append("),\n"));
                    continue;
                }

                String dataType =
                        attr.getDataType() != null ? attr.getDataType().toString() : "VARCHAR";
                String pk = attr.isPk() ? " PRIMARY KEY" : "";
                String unique = attr.isUnique() ? " UNIQUE" : "";
                String notNull = attr.isNoEmpty() ? " NOT NULL" : "";
                columns.append("  ")
                        .append(attr.getName())
                        .append(" ")
                        .append(dataType)
                        .append(pk)
                        .append(unique)
                        .append(notNull)
                        .append(",\n");
            }

            sqlBuilder
                    .append("CREATE TABLE ")
                    .append(table.getName())
                    .append("(\n")
                    .append(columns)
                    .append(fkConstraints)
                    .append(");\n\n");
        }

        return sqlBuilder;
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entityName = createTable.getTable().getName();
                Node entity = Entity.builder().name(entityName).build();
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
            boolean isFk = index.getType().equalsIgnoreCase("foreign key");
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Optional<Node> exists =
                                        diagram.vertexSet().stream()
                                                .filter(n -> n.getName().equals(name))
                                                .findFirst();
                                Node attribute =
                                        exists.orElseGet(
                                                () -> {
                                                    Node a =
                                                            Attribute.builder()
                                                                    .name(name)
                                                                    .pk(isPk)
                                                                    .build();
                                                    diagram.addVertex(a);
                                                    return a;
                                                });
                                if (attribute instanceof Attribute a) {
                                    a.setPk(isPk);
                                }
                                Edge edge =
                                        Edge.builder()
                                                .label("attribute" + entity.getName() + name)
                                                .build();
                                if (isPk) {
                                    diagram.addEdge(entity, attribute, edge);
                                } else if (isFk) {
                                    diagram.addEdge(attribute, entity, edge);
                                }
                            });
        }
    }

    private void parseColumns(
            List<ColumnDefinition> columns, Node entity, Graph<Node, Edge> diagram) {
        for (ColumnDefinition col : columns) {
            String name = col.getColumnName();
            String type = col.getColDataType().getDataType().toUpperCase();
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
            Domain domain;
            try {
                domain = Domain.valueOf(type);
            } catch (IllegalArgumentException e) {
                domain = Domain.VARCHAR;
            }
            Optional<Node> exists =
                    diagram.vertexSet().stream().filter(n -> n.getName().equals(name)).findFirst();
            if (exists.isPresent()) {
                if (exists.get() instanceof Attribute a) {
                    a.setDataType(domain);
                    a.setUnique(isUnique);
                    a.setNoEmpty(isNotNull);
                    if (diagram.getAllEdges(entity, exists.get()).isEmpty()) {
                        diagram.addEdge(
                                entity,
                                exists.get(),
                                Edge.builder().label("attr" + name + entity.getName()).build());
                    }
                }
            } else {
                Node attribute =
                        Attribute.builder()
                                .name(name)
                                .dataType(domain)
                                .pk(isPk)
                                .unique(isUnique)
                                .noEmpty(isNotNull)
                                .build();
                diagram.addVertex(attribute);
                diagram.addEdge(
                        entity,
                        attribute,
                        Edge.builder().label("attr" + name + entity.getName()).build());
            }
        }
    }
}
