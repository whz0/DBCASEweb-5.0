package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Participant;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.HashSet;
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
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
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

        List<Node> nodes =
                graph.vertexSet().stream()
                        .filter(n -> n instanceof Entity || n instanceof Relationship)
                        .toList();

        Set<Node> visited = new HashSet<>();

        for (Node node : nodes) {
            if (visited.contains(node)) {
                continue;
            }

            if (node instanceof Relationship rel && rel.isNM()) {
                sqlBuilder.append(buildIntermediateTable(rel, graph));
                visited.add(node);
            } else if (!(node instanceof Relationship)) {
                sqlBuilder.append(buildTable(node, graph, visited));
            }
        }
        return sqlBuilder;
    }

    private String buildIntermediateTable(Relationship rel, Graph<Node, Edge> graph) {
        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        for (Participant p : rel.getParticipants()) {
            Node entity = p.getEntity();
            graph.vertexSet().stream()
                    .filter(
                            n ->
                                    n instanceof Attribute
                                            && ((Attribute) n).isPk()
                                            && graph.containsEdge(entity, n))
                    .map(n -> (Attribute) n)
                    .forEach(
                            pk -> {
                                String dataType =
                                        pk.getDataType() != null
                                                ? pk.getDataType().toString()
                                                : "?";
                                columns.append("\t")
                                        .append(pk.getName())
                                        .append(" ")
                                        .append(dataType)
                                        .append(",\n");
                                constraints
                                        .append("\tFOREIGN KEY (")
                                        .append(pk.getName())
                                        .append(") REFERENCES ")
                                        .append(entity.getName())
                                        .append("(")
                                        .append(pk.getName())
                                        .append("),\n");
                            });
        }

        if (rel.getAttributes() != null) {
            for (Attribute attr : rel.getAttributes()) {
                String dataType = attr.getDataType() != null ? attr.getDataType().toString() : "?";
                columns.append("\t")
                        .append(attr.getName())
                        .append(" ")
                        .append(dataType)
                        .append(",\n");
            }
        }

        String body = columns.toString() + constraints.toString();
        if (body.endsWith(",\n")) {
            body = body.substring(0, body.length() - 2) + "\n";
        }
        return "CREATE TABLE " + rel.getName() + "(\n" + body + ");\n\n";
    }

    private String buildTable(Node entity, Graph<Node, Edge> graph, Set<Node> visited) {
        if (visited.contains(entity)) {
            return "";
        }
        visited.add(entity);

        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        graph.vertexSet().stream()
                .filter(n -> n instanceof Attribute && graph.containsEdge(entity, n))
                .map(n -> (Attribute) n)
                .forEach(
                        attr -> {
                            String dataType =
                                    attr.getDataType() != null
                                            ? attr.getDataType().toString()
                                            : "?";
                            String pk = attr.isPk() ? " PRIMARY KEY" : "";
                            String unique = attr.isUnique() ? " UNIQUE" : "";
                            String notNull = attr.isNoEmpty() ? " NOT NULL" : "";

                            if (attr.getFk() != null) {
                                constraints
                                        .append("\tFOREIGN KEY (")
                                        .append(attr.getName())
                                        .append(") REFERENCES ")
                                        .append(attr.getFk())
                                        .append("(")
                                        .append(attr.getName())
                                        .append("),\n");
                            }
                            columns.append("\t")
                                    .append(attr.getName())
                                    .append(" ")
                                    .append(dataType)
                                    .append(pk)
                                    .append(unique)
                                    .append(notNull)
                                    .append(",\n");
                        });

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
            boolean isFk =
                    index instanceof ForeignKeyIndex
                            || index.getType().equalsIgnoreCase("foreign key");
            String referencedTable = null;
            if (isFk && index instanceof ForeignKeyIndex fki && fki.getTable() != null) {
                referencedTable = fki.getTable().getName();
            }
            final String finalRef = referencedTable;
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
                                                                    .fk(finalRef)
                                                                    .build();
                                                    diagram.addVertex(a);
                                                    return a;
                                                });
                                if (attribute instanceof Attribute a) {
                                    a.setPk(isPk);
                                    if (isFk && finalRef != null) {
                                        a.setFk(finalRef);
                                    }
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
