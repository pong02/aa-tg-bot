package aa.helper;

import java.util.Collection;
import java.util.List;

public class ListMessageFormatter {

    public static <T> String format(List<T> list) {
        if (list == null || list.isEmpty()) {
            return "(empty list)";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            result.append("Item ").append(i + 1).append(":\n");

            if (item == null) {
                result.append("  null\n");
            } else if (item instanceof Collection<?>) {
                int count = 1;
                for (Object subItem : (Collection<?>) item) {
                    result.append("  - Item ").append(count++).append(": ").append(toSafeString(subItem)).append("\n");
                }
            } else {
                result.append(toSafeString(item)).append("\n");
            }
        }

        return result.toString().trim();
    }

    private static String toSafeString(Object obj) {
        if (obj == null) return "null";
        try {
            return obj.toString();
        } catch (Exception e) {
            return "<error calling toString()>";
        }
    }
}

