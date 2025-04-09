package aa.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HelpPaginator {

    private static final int ITEMS_PER_PAGE = 5;

    public static List<String> loadHelpItems() {
        List<String> helpItems = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                HelpPaginator.class.getClassLoader().getResourceAsStream("help.txt"),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    helpItems.add(line);
                }
            }
        } catch (Exception e) {
            helpItems.add("Error loading help content: " + e.getMessage());
        }
        return helpItems;
    }

    public static String getHelpPage(List<String> helpItems, int pageNumber) {
        int totalPages = (int) Math.ceil((double) helpItems.size() / ITEMS_PER_PAGE);

        if (pageNumber < 1 || pageNumber > totalPages) {
            return "Invalid page number. Please select between 1 and " + totalPages;
        }

        int start = (pageNumber - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, helpItems.size());

        StringBuilder sb = new StringBuilder();
        sb.append("Help - Page ").append(pageNumber).append(" of ").append(totalPages).append("\n\n");

        for (int i = start; i < end; i++) {
            sb.append(i + 1).append(". ").append(helpItems.get(i)).append("\n");
        }

        return sb.toString();
    }

    public static int getTotalPages(List<String> helpItems) {
        return (int) Math.ceil((double) helpItems.size() / ITEMS_PER_PAGE);
    }

}
