package org.unipop.jdbc.controller.schemas;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.unipop.common.property.PropertySchema;
import org.unipop.common.schema.VertexSchema;
import org.unipop.common.schema.base.BaseEdgeSchema;
import org.unipop.structure.UniGraph;

import java.util.List;

/**
 * Created by GurRo on 6/13/2016.
 */
public class RowEdgeSchema extends BaseEdgeSchema implements RowSchema<Edge> {
    private final String database;
    private final String table;

    public RowEdgeSchema(VertexSchema outVertexSchema, VertexSchema inVertexSchema, List<PropertySchema> properties, UniGraph graph, String database, String table) {
        super(outVertexSchema, inVertexSchema, properties, graph);

        this.database = database;
        this.table = table;
    }

    @Override
    public String getDatabase(Edge element) {
        return this.database;
    }

    @Override
    public String getTable(Edge element) {
        return this.table == null ? element.label() : this.table;
    }
}