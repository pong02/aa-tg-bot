package aa.helper;

import aa.exception.ParseError;
import aa.model.Label;
import aa.repository.LabelDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class ExcelProcessor {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> REQUIRED_HEADERS = List.of(
            "Date", "Order ID", "Name", "Address", "City", "State", "Postal Code", "Label"
    );


    public static List<Label> read(InputStream inputStream, LabelDao labelDao) throws ParseError {
        List<Label> labels = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);

            // check if header is valid Date	Order ID	Post To Name	Post To Address	Post To City	Post To State	Post To Postal Code	Custom Label
            if (!validHeader(header)){
                throw new ParseError("File not parsable, invalid headers");
            }
            // Explicitly skip header by row index
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue; // stop early on blank row

                try {
                    Label label = Label.builder()
                            .entryDate(LocalDateTime.now())
                            .date(parseDateOrNull(row))
                            .orderId(getStringValue(row, 1))
                            .postToName(getStringValue(row, 2))
                            .postToAddress(getStringValue(row, 3))
                            .postToCity(getStringValue(row, 4))
                            .postToState(getStringValue(row, 5))
                            .postToPostalCode(getStringValue(row, 6))
                            .customLabel(getStringValue(row, 7))
                            .pending(true)
                            .deleted(false)
                            .build();

                    log.debug("Parsed label successfully:{}", label.toString());
                    labelDao.save(label);
                    labels.add(label);
                } catch (Exception ex) {
                    log.warn("Failed to parse row {}: {}", row.getRowNum() + 1, ex.getMessage());
                }
            }
            return labels;
        } catch (Exception e) {
            log.error("Error reading Excel file", e);
            throw new ParseError("Failed to parse the Excel file.");
        }
    }

    private static String getStringValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()).trim(); // avoid .0 for integers
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue()).trim();
            case FORMULA -> cell.getCellFormula().trim(); // or evaluate the result if needed
            default -> "";
        };
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !getStringValue(row, cell.getColumnIndex()).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static LocalDateTime parseDateOrNull(Row row) {
        Cell cell = row.getCell(0);

        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else {
                String raw = getStringValue(row, 0);
                if (raw.isBlank()) return null;
                LocalDate date = LocalDate.parse(raw, DATE_FORMATTER);
                return date.atStartOfDay();
            }
        } catch (Exception e) {
            int rowNum = row.getRowNum() + 1;
            log.warn("Invalid date format at row {}, col {}: '{}'", rowNum, 0, cell);

            return null;
        }
    }

    private static boolean validHeader(Row headerRow) {
        if (headerRow == null || headerRow.getPhysicalNumberOfCells() < REQUIRED_HEADERS.size()) {
            return false;
        }

        for (int i = 0; i < REQUIRED_HEADERS.size(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || cell.getCellType() != CellType.STRING) {
                log.warn("Header cell at index {} is missing or not a string", i);
                return false;
            }

            String actual = cell.getStringCellValue().trim().toLowerCase();
            String expected = REQUIRED_HEADERS.get(i).toLowerCase();

            if (!actual.contains(expected)) {
                log.warn("Expected header '{}' at column {}, but found '{}'", expected, i, actual);
                return false;
            }
        }

        return true;
    }

}
