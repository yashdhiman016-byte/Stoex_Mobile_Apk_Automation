package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            try {
                // One timestamped report per suite run to preserve execution history.
                Path reportsDir = Path.of("reports");
                Files.createDirectories(reportsDir);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String reportPath = reportsDir.resolve("Stoex_Automation_Report_" + timestamp + ".html").toString();

                ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
                spark.config().setReportName("Stoex Mobile Automation");
                spark.config().setDocumentTitle("Stoex Field Validation Results");

                extent = new ExtentReports();
                extent.attachReporter(spark);
                extent.setSystemInfo("Framework", "Appium + TestNG");
                extent.setSystemInfo("Platform", "Android");
                extent.setSystemInfo("Suite", "Name Field Validation");
            } catch (Exception e) {
                throw new RuntimeException("Unable to initialize Extent report", e);
            }
        }
        return extent;
    }
}
