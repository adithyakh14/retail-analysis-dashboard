package com.retailproject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return "\"" + escape(text) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                builder.append(toJson(String.valueOf(entry.getKey())));
                builder.append(":");
                builder.append(toJson(entry.getValue()));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
            return builder.toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                builder.append(toJson(list.get(i)));
                if (i < list.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return builder.toString();
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return toJson(List.of(array));
        }
        return toJson(String.valueOf(value));
    }

    private static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
