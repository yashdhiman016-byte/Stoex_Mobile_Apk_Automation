package utils;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExcelReportWriter {

    private ExcelReportWriter() {
    }

    public static void write(Path outputFile, List<ExecutionRecord> records) {
        try {
            Files.createDirectories(outputFile.getParent());
            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream out = Files.newOutputStream(outputFile)) {

                Sheet sheet = workbook.createSheet("Execution Results");
                String[] headers = {
                        "Test Case ID",
                        "Scenario",
                        "Input",
                        "Expected",
                        "Actual",
                        "Status",
                        "Error Message",
                        "Screenshot URL"
                };

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                CreationHelper creationHelper = workbook.getCreationHelper();
                CellStyle hyperlinkStyle = workbook.createCellStyle();
                var font = workbook.createFont();
                font.setUnderline((byte) 1);
                font.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(font);

                CellStyle passStyle = workbook.createCellStyle();
                passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellStyle failStyle = workbook.createCellStyle();
                failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                int rowIndex = 1;
                for (ExecutionRecord record : records) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(safe(record.testCaseId));
                    row.createCell(1).setCellValue(safe(record.scenario));
                    row.createCell(2).setCellValue(safe(record.input));
                    row.createCell(3).setCellValue(safe(record.expected));
                    row.createCell(4).setCellValue(safe(record.actual));
                    Cell statusCell = row.createCell(5);
                    statusCell.setCellValue(safe(record.status));
                    if ("PASS".equalsIgnoreCase(record.status)) {
                        statusCell.setCellStyle(passStyle);
                    } else if ("FAIL".equalsIgnoreCase(record.status)) {
                        statusCell.setCellStyle(failStyle);
                    }
                    row.createCell(6).setCellValue(safe(record.errorMessage));

                    Cell screenshotCell = row.createCell(7);
                    if (!safe(record.screenshotUrl).isEmpty()) {
                        screenshotCell.setCellValue("Open Screenshot");
                        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.FILE);
                        hyperlink.setAddress(Path.of(record.screenshotUrl).toUri().toString());
                        screenshotCell.setHyperlink(hyperlink);
                        screenshotCell.setCellStyle(hyperlinkStyle);
                    } else {
                        screenshotCell.setCellValue("");
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to write Excel report", e);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
