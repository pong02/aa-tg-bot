package aa.helper;

import java.lang.reflect.Field;
import java.util.*;

public class ListMessageFormatter {

    public static <T> String format(List<T> list) {
        if (list == null || list.isEmpty()) {
            return "(empty list)";
        }

        StringBuilder result = new StringBuilder();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            result.append("Item ").append(i + 1).append(":\n");
            formatObject(item, result, "  ", visited);
            result.append("\n");
        }

        return result.toString().trim();
    }

    private static void formatObject(Object obj, StringBuilder result, String indent, Set<Object> visited) {
        if (obj == null) {
            result.append(indent).append("null\n");
            return;
        }

        if (visited.contains(obj)) {
            result.append(indent).append("[Already Visited]\n");
            return;
        }

        visited.add(obj);

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // Skip static fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // Skip core JDK classes
            if (field.getDeclaringClass().getPackageName().startsWith("java.")) {
                continue;
            }
            try {
                field.setAccessible(true);  // Only after safe check
                Object value = field.get(obj);
                if (value == null) {
                    result.append(indent).append(field.getName()).append(": null\n");
                    continue;
                }

                if (isPrimitiveLike(value.getClass())) {
                    result.append(indent).append(field.getName()).append(": ").append(value).append("\n");

                } else if (value instanceof Collection<?>) {
                    Collection<?> collection = (Collection<?>) value;
                    result.append(indent).append(field.getName())
                            .append(" (List) [").append(collection.size()).append(" items]\n");

                    int count = 1;
                    for (Object item : collection) {
                        result.append(indent).append("  - Item ").append(count++).append(":\n");
                        formatObject(item, result, indent + "    ", visited);
                    }

                } else {
                    result.append(indent).append(field.getName()).append(":\n");
                    formatObject(value, result, indent + "  ", visited);
                }

            } catch (IllegalAccessException e) {
                result.append(indent).append(field.getName()).append(": <access error>\n");
            }
        }

        visited.remove(obj); // Optional: for re-entry on other paths
    }

    private static boolean isPrimitiveLike(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type.isEnum()
                || type.getName().startsWith("java.time") // LocalDate, etc.
                || type.getName().startsWith("java.util.UUID");
    }
}