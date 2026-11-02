package com.knightsofdarkness.web.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonValue;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;

/**
 * the API described as Jackson writes it, so clients can generate their types from it: every field is always present,
 * a field is null only when it is an Optional or its type is marked @Nullable, and a count map keyed by an enum names
 * its keys, all of them present when the field is marked @EveryKey
 */
@Configuration
public class ApiDescription {
    static
    {
        // enums become named schemas, so clients reuse UnitName or BuildingName instead of repeating their values
        ModelResolver.enumsAsRef = true;
    }

    /** the enums whose values key the count maps, named in the description so clients can reuse them */
    private static final Set<Class<?>> countKeys = ConcurrentHashMap.newKeySet();

    @Bean
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public OpenApiCustomizer everyFieldPresent()
    {
        return api ->
        {
            api.getComponents().getSchemas().values().forEach(schema ->
            {
                if (schema.getProperties() != null && !schema.getProperties().isEmpty())
                {
                    schema.setRequired(new ArrayList<>(schema.getProperties().keySet()));
                }
            });
            for (var keys : countKeys)
            {
                var values = new Schema().specVersion(SpecVersion.V31);
                values.addType("string");
                values.setEnum(Arrays.stream(keys.getEnumConstants()).map(key -> ((Enum<?>) key).name()).toList());
                api.getComponents().getSchemas().putIfAbsent(keys.getSimpleName(), values);
            }
        };
    }

    @Bean
    public ModelConverter nullableFields()
    {
        return new NullableFields();
    }

    /**
     * reads each of our classes as it is described: marks the fields that can be null, and spells out count maps
     */
    static class NullableFields implements ModelConverter {
        @Override
        @SuppressWarnings("rawtypes")
        public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain)
        {
            var schema = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
            if (schema == null)
            {
                return null;
            }
            var raw = Json.mapper().constructType(type.getType()).getRawClass();
            if (!raw.getName().startsWith("com.knightsofdarkness."))
            {
                return schema;
            }
            var model = schema.get$ref() == null ? schema : context.getDefinedModels().get(schema.get$ref().substring(schema.get$ref().lastIndexOf('/') + 1));
            if (model != null && model.getProperties() != null)
            {
                for (var field : fields(raw))
                {
                    var property = (Schema) model.getProperties().get(field.getName());
                    if (property == null)
                    {
                        continue;
                    }
                    var counts = countMap(field.getGenericType());
                    if (counts != null)
                    {
                        property = counts.describe(field.isAnnotationPresent(EveryKey.class));
                    }
                    model.getProperties().put(field.getName(), canBeNull(field) ? orNull(property) : property);
                }
            }
            return schema;
        }

        /** a map from one of our enums to a number, or a class Jackson writes as one */
        record CountMap(Class<?> keys, Class<?> values) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Schema describe(boolean everyKey)
            {
                var properties = new LinkedHashMap<String, Schema>();
                for (var key : keys.getEnumConstants())
                {
                    var value = new Schema().specVersion(SpecVersion.V31);
                    value.addType(values == Double.class ? "number" : "integer");
                    value.format(values == Double.class ? "double" : "int32");
                    properties.put(((Enum<?>) key).name(), value);
                }
                var map = new Schema().specVersion(SpecVersion.V31).properties(properties);
                map.addType("object");
                if (everyKey)
                {
                    map.required(new ArrayList<>(properties.keySet()));
                }
                return map;
            }
        }

        private static CountMap countMap(Type type)
        {
            if (type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw && Map.class.isAssignableFrom(raw))
            {
                var arguments = parameterized.getActualTypeArguments();
                if (arguments[0] instanceof Class<?> key && key.isEnum() && key.getName().startsWith("com.knightsofdarkness.") && arguments[1] instanceof Class<?> value
                        && Number.class.isAssignableFrom(value))
                {
                    countKeys.add(key);
                    return new CountMap(key, value);
                }
                return null;
            }
            if (type instanceof Class<?> wrapper && wrapper.getName().startsWith("com.knightsofdarkness."))
            {
                return Arrays.stream(wrapper.getMethods()).filter(method -> method.isAnnotationPresent(JsonValue.class)).findFirst()
                        .map(method -> countMap(method.getGenericReturnType())).orElse(null);
            }
            return null;
        }

        private static List<Field> fields(Class<?> type)
        {
            var fields = new ArrayList<Field>();
            for (var current = type; current != null && current != Object.class; current = current.getSuperclass())
            {
                for (var field : current.getDeclaredFields())
                {
                    if (!Modifier.isStatic(field.getModifiers()))
                    {
                        fields.add(field);
                    }
                }
            }
            return fields;
        }

        private static boolean canBeNull(Field field)
        {
            return field.getType() == Optional.class || field.getAnnotatedType().isAnnotationPresent(Nullable.class);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static Schema orNull(Schema property)
        {
            if (property.get$ref() != null)
            {
                var nothing = new Schema().specVersion(SpecVersion.V31);
                nothing.addType("null");
                return new Schema().specVersion(SpecVersion.V31).anyOf(List.of(new Schema().$ref(property.get$ref()), nothing));
            }
            property.addType("null");
            return property;
        }
    }
}
