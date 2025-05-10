package org.pixel.commons;

import org.pixel.commons.annotation.Property;
import org.pixel.commons.exception.MissingPropertyException;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class PropertyLoader {
    private PropertyLoader() {
    }

    public static <T> T load(Class<T> clazz, String propFilePath) throws IOException {
        //load raw props
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(propFilePath)) {
            properties.load(in);
        }

        return load(clazz, properties);
    }

    public static <T> T load(Class<T> clazz, Properties properties) {
        // instantiate root config
        T root = instantiate(clazz);

        // apply all present keys
        Set<String> seen = new HashSet<>();
        for (String key : properties.stringPropertyNames()) {
            seen.add(key);
            applyIfPresent(root, key, properties.getProperty(key));
        }

        // now enforce defaults & mandatory
        enforceDefaultsAndMandatory(clazz, root, properties, seen);

        return root;
    }

    private static <T> T instantiate(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();

        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate " + clazz, e);
        }
    }

    private static void applyIfPresent(Object root, String key, String value) {
        String[] parts = key.split("\\.", 2);
        if (parts.length != 2) return;

        try {
            Field scopeF = root.getClass().getDeclaredField(parts[0]);
            scopeF.setAccessible(true);
            Object scopeObj = scopeF.get(root);

            Field field = scopeObj.getClass().getDeclaredField(parts[1]);
            field.setAccessible(true);

            Object converted = convert(value, field.getType());
            field.set(scopeObj, converted);
        } catch (ReflectiveOperationException ignore) {
            // unknown key → skip
        }
    }

    private static <T> void enforceDefaultsAndMandatory(
            Class<T> clazz, T root, Properties props, Set<String> seen) {

        for (Field scopeF : clazz.getDeclaredFields()) {
            scopeF.setAccessible(true);
            Object scopeObj;
            try {
                scopeObj = scopeF.get(root);
            } catch (IllegalAccessException e) {
                continue;
            }
            String scopeName = scopeF.getName();

            for (Field f : scopeF.getType().getDeclaredFields()) {
                Property ann = f.getAnnotation(Property.class);
                if (ann == null) continue;

                String fullKey = scopeName + "." + f.getName();
                boolean hasKey = seen.contains(fullKey);

                f.setAccessible(true);
                try {
                    if (!hasKey) {
                        if (!ann.defaultValue().isEmpty()) {
                            // apply the default
                            Object def = convert(ann.defaultValue(), f.getType());
                            f.set(scopeObj, def);

                        } else if (ann.mandatory()) {
                            throw new MissingPropertyException("Missing required property: " + fullKey);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private static Object convert(String val, Class<?> type) {
        if (type == String.class) return val;
        if (type == int.class || type == Integer.class) return Integer.parseInt(val);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(val);
        if (type == long.class || type == Long.class) return Long.parseLong(val);
        if (type == double.class || type == Double.class) return Double.parseDouble(val);
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
