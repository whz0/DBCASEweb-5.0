package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.addEdge;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addForeignAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addPrimaryAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
import org.jgrapht.graph.DirectedPseudograph;
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
        Graph<Node, Edge> result = new DirectedPseudograph<>(Edge.class);

        for (String statement : diagram.sql().split(";")) {

            if (!statement.isBlank()) {
                parseStatement(statement.trim(), result);
            }
        }

        Set<Node> uniques =
                result.vertexSet().stream()
                        .filter(a -> a.isAttribute() && a.isFk() && a.isUnique())
                        .collect(Collectors.toSet());

        for (Node unique : uniques) {
            Node node = Graphs.predecessorListOf(result, unique).getFirst();
            Node pk =
                    Graphs.successorListOf(result, node).stream()
                            .filter(a -> a.isAttribute() && a.isPk() && !a.isFk())
                            .findFirst()
                            .orElse(null);
            Node refNode =
                    result.vertexSet().stream()
                            .filter(
                                    n ->
                                            !n.isAttribute()
                                                    && n.getName().equals(unique.getReference()))
                            .findFirst()
                            .orElse(null);
            if (pk != null && refNode != null) {
                Node attr = getOrCreateAttr(pk.getName(), refNode, result);
                attr.setUnique(true);
                if (unique.isNotNull()) {
                    attr.setNotNull(true);
                }
                addForeignAttr(attr, refNode, node.getName(), result);
                addEdge(attr, pk, result);
            }
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> allNodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .sorted(Comparator.comparing(Node::getName))
                        .toList();

        StringBuilder sql = new StringBuilder();
        Set<String> usedRefs = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        for (Node node : allNodes) {
            sql.append(buildTable(node, graph, usedRefs, visited));
        }
        return sql;
    }

    private String buildTable(
            Node entity, Graph<Node, Edge> graph, Set<String> usedRefs, Set<Node> visited) {

        if (visited.contains(entity)) {
            return "";
        }
        visited.add(entity);

        List<Node> attrs =
                Graphs.successorListOf(graph, entity).stream().filter(Node::isAttribute).toList();

        long pkCount = attrs.stream().filter(Node::isPk).count();
        boolean compositePk = pkCount > 1;

        StringBuilder table = new StringBuilder();
        StringBuilder pkColumns = new StringBuilder();
        List<String> pkNames = new ArrayList<>();
        StringBuilder pkConstraints = new StringBuilder();
        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        java.util.Map<String, List<Node>> fksByRef = new java.util.LinkedHashMap<>();

        for (Node attr : attrs) {
            String dataType = attr.getDataType() != null ? attr.getDataType().toString() : "?";

            if (attr.isPk()) {
                pkColumns.append("\t").append(attr.getName()).append(" ").append(dataType);
                if (compositePk) {
                    pkNames.add(attr.getName());
                } else {
                    pkColumns.append(" PRIMARY KEY");
                }
                pkColumns.append(",\n");
            }

            if (attr.isFk()) {
                if (attr.isUnique() && usedRefs.contains(attr.getReference())) {
                    continue;
                }
                if (attr.isUnique()) {
                    usedRefs.add(entity.getName());
                }
                if (attr.getReference() != null) {
                    fksByRef.computeIfAbsent(attr.getReference(), k -> new ArrayList<>()).add(attr);
                }
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(attr.getReference()))
                        .findFirst()
                        .ifPresent(
                                node -> table.append(buildTable(node, graph, usedRefs, visited)));
            }

            if (!attr.isPk()) {
                columns.append("\t")
                        .append(attr.getName())
                        .append(" ")
                        .append(dataType)
                        .append(attr.isUnique() ? " UNIQUE" : "")
                        .append(attr.isNotNull() ? " NOT NULL" : "")
                        .append(",\n");
            }
        }

        for (java.util.Map.Entry<String, List<Node>> entry : fksByRef.entrySet()) {
            String refTable = entry.getKey();
            List<Node> fks = entry.getValue();
            boolean refTableExists =
                    graph.vertexSet().stream()
                            .anyMatch(n -> !n.isAttribute() && n.getName().equals(refTable));
            if (!refTableExists) {
                continue;
            }

            List<String> srcCols = new ArrayList<>();
            List<String> refCols = new ArrayList<>();
            for (Node fk : fks) {
                List<Node> successors = Graphs.successorListOf(graph, fk);
                if (successors.isEmpty()) {
                    continue;
                }
                srcCols.add(fk.getName());
                refCols.add(successors.getFirst().getName());
            }
            if (srcCols.isEmpty()) {
                continue;
            }

            constraints
                    .append("\tFOREIGN KEY (")
                    .append(String.join(", ", srcCols))
                    .append(") REFERENCES ")
                    .append(refTable)
                    .append("(")
                    .append(String.join(", ", refCols))
                    .append("),\n");
        }

        if (!pkNames.isEmpty()) {
            pkConstraints
                    .append("\tPRIMARY KEY (")
                    .append(String.join(", ", pkNames))
                    .append("),\n");
        }

        if (columns.isEmpty() && pkColumns.isEmpty()) {
            return "";
        }
        String body =
                pkColumns.toString()
                        + columns.toString()
                        + pkConstraints.toString()
                        + constraints.toString();
        if (body.endsWith(",\n")) {
            body = body.substring(0, body.length() - 2) + "\n";
        }

        table.append("CREATE TABLE ")
                .append(entity.getName())
                .append("(\n")
                .append(body)
                .append(");\n\n");

        return table.toString();
    }

    private void validateSinglePrimaryKey(CreateTable createTable, String tableName)
            throws Exception {
        long pkInIndexes =
                createTable.getIndexes() == null
                        ? 0
                        : createTable.getIndexes().stream()
                                .filter(i -> i.getType().equalsIgnoreCase("primary key"))
                                .count();
        long pkInColumns =
                createTable.getColumnDefinitions() == null
                        ? 0
                        : createTable.getColumnDefinitions().stream()
                                .filter(
                                        c ->
                                                c.getColumnSpecs() != null
                                                        && c.getColumnSpecs().stream()
                                                                .anyMatch(
                                                                        s ->
                                                                                s.equalsIgnoreCase(
                                                                                        "primary"))
                                                        && c.getColumnSpecs().stream()
                                                                .anyMatch(
                                                                        s ->
                                                                                s.equalsIgnoreCase(
                                                                                        "key")))
                                .count();
        if (pkInIndexes + pkInColumns > 1) {
            throw new Exception("Table " + tableName + " cannot have more than one PRIMARY KEY");
        }
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String nodeName = createTable.getTable().getName();

                validateSinglePrimaryKey(createTable, nodeName);

                Node entity = getOrCreateNode(nodeName, diagram);
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

    private void parseIndex(List<Index> indexes, Node node, Graph<Node, Edge> diagram) {
        for (Index index : indexes) {
            boolean isPk = index.getType().equalsIgnoreCase("primary key");
            boolean isFk =
                    index instanceof ForeignKeyIndex
                            || index.getType().equalsIgnoreCase("foreign key");
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Node attr = getOrCreateAttr(name, node, diagram);
                                if (isFk && index instanceof ForeignKeyIndex fki) {
                                    String tableName = fki.getTable().getName();

                                    List<String> ref = fki.getReferencedColumnNames();
                                    List<String> src = fki.getColumnsNames();

                                    int i = src.indexOf(name);

                                    if (i != -1) {
                                        String refAttrName = ref.get(i);
                                        Node refTable = getOrCreateNode(tableName, diagram);
                                        Node refAttr =
                                                getOrCreateAttr(refAttrName, refTable, diagram);
                                        addEdge(refTable, refAttr, diagram);
                                        addEdge(attr, refAttr, diagram);
                                    }

                                    addForeignAttr(attr, node, tableName, diagram);
                                } else if (isPk) {
                                    addPrimaryAttr(attr, node, diagram);
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

            String baseType =
                    type.contains("(") ? type.substring(0, type.indexOf("(")).trim() : type.trim();

            Domain domain;
            try {
                domain = Domain.valueOf(baseType.toUpperCase());
            } catch (IllegalArgumentException e) {
                domain = null;
            }

            List<String> typeParams = col.getColDataType().getArgumentsStringList();
            DataType dataType =
                    (domain != null)
                            ? (typeParams != null && !typeParams.isEmpty()
                                    ? DataType.of(
                                            domain, Integer.parseInt(typeParams.getFirst().trim()))
                                    : DataType.of(domain))
                            : null;

            Node attr = getOrCreateAttr(name, entity, diagram);
            attr.setDataType(dataType);
            attr.setNotNull(isNotNull);
            attr.setUnique(isUnique);
            if (isPk) {
                addPrimaryAttr(attr, entity, diagram);
            } else {
                addEdge(entity, attr, diagram);
            }
        }
    }
}
