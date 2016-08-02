package org.unipop.schema.property;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sbarzilay on 7/28/16.
 */
public class MultiFieldPropertySchema implements PropertySchema {
    private String key;
    private List<PropertySchema> schemas;

    public MultiFieldPropertySchema(String key, List<PropertySchema> schemas) {
        this.key = key;
        this.schemas = schemas;
    }

    @Override
    public Map<String, Object> toProperties(Map<String, Object> source) {
        List<Object> value = new ArrayList<>();
        schemas.forEach(schema -> {
            schema.toProperties(source).values().forEach(value::add);
        });
        return Collections.singletonMap(key, value);
    }

    @Override
    public Set<String> excludeDynamicFields() {
        return schemas.stream()
                .map(PropertySchema::excludeDynamicFields)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<String> excludeDynamicProperties() {
        return Collections.singleton(this.key);
    }

    @Override
    public Map<String, Object> toFields(Map<String, Object> properties) {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> toFields(Set<String> propertyKeys) {
        return propertyKeys.contains(key) ? schemas.stream()
                .flatMap(s -> s.toFields(propertyKeys).stream()).collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    @Override
    public PredicatesHolder toPredicates(PredicatesHolder predicatesHolder) {
        Stream<HasContainer> hasContainers = predicatesHolder.findKey(this.key);

        Set<PredicatesHolder> predicateHolders = hasContainers.map(this::toPredicate).collect(Collectors.toSet());
        return PredicatesHolderFactory.create(predicatesHolder.getClause(), predicateHolders);
    }

    private PredicatesHolder toPredicate(HasContainer hasContainer) {
        Set<PredicatesHolder> predicates = new HashSet<>();
        for (PropertySchema schema : schemas) {
            PredicatesHolder predicatesHolder = schema.toPredicates(PredicatesHolderFactory.predicate(hasContainer));
            if (predicatesHolder.equals(PredicatesHolderFactory.empty()))
                return predicatesHolder;
            if (!predicatesHolder.equals(PredicatesHolderFactory.abort()))
                predicates.add(predicatesHolder);
        }
        return PredicatesHolderFactory.or(predicates);
    }

    public static class Builder implements PropertySchemaBuilder {
        @Override
        public PropertySchema build(String key, Object conf) {
            if (!(conf instanceof JSONArray)) return null;
            JSONArray fieldsArray = (JSONArray) conf;
            List<PropertySchema> schemas = new ArrayList<>();
            for (int i = 0; i < fieldsArray.length(); i++) {
                schemas.add(AbstractPropertyContainer.createPropertySchema(key, fieldsArray.get(i), true));
            }
            return new MultiFieldPropertySchema(key, schemas);
        }
    }
}
