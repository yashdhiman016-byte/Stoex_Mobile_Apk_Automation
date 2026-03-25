package utils;

import base.BaseTest;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestListener implements ITestListener {

    private static final ExtentReports EXTENT = ExtentManager.getExtent();
    private static final ThreadLocal<ExtentTest> TEST_NODE = new ThreadLocal<>();
    private static final List<ExecutionRecord> RECORDS = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onStart(ITestContext context) {
        RECORDS.clear();
        System.out.println("Suite execution started: " + context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        if (params != null && params.length >= 2) {
            testName = String.valueOf(params[0]) + " | " + String.valueOf(params[1]);
        } else if (params != null && params.length > 0) {
            testName = testName + " " + Arrays.toString(params);
        }
        TEST_NODE.set(EXTENT.createTest(testName));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = TEST_NODE.get();
        logTestDetails(result, test);
        test.pass("Test passed");
        String screenshotPath = captureAndAttachEvidence(result, "PASS");
        addExecutionRecord(result, "PASS", "", screenshotPath);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = TEST_NODE.get();
        logTestDetails(result, test);
        test.fail(result.getThrowable());
        String screenshotPath = captureAndAttachEvidence(result, "FAIL");
        String error = result.getThrowable() == null ? "" : result.getThrowable().getMessage();
        addExecutionRecord(result, "FAIL", error, screenshotPath);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = TEST_NODE.get();
        logTestDetails(result, test);
        test.skip("Test skipped");
        String screenshotPath = captureAndAttachEvidence(result, "SKIP");
        addExecutionRecord(result, "SKIP", "Skipped", screenshotPath);
    }

    @Override
    public void onFinish(ITestContext context) {
        EXTENT.flush();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path excelPath = Path.of("reports", "Stoex_Test_Results_" + timestamp + ".xlsx");
        ExcelReportWriter.write(excelPath, RECORDS);

        System.out.println("Extent report generated under reports/ directory");
        System.out.println("Excel report generated: " + excelPath);
    }

    private String captureAndAttachEvidence(ITestResult result, String status) {
        Object instance = result.getInstance();
        if (!(instance instanceof BaseTest baseTest)) {
            return "";
        }

        try {
            String label = status + "_" + result.getMethod().getMethodName();
            String filePath = baseTest.captureScreenshot(label);
            String screenshotBase64 = baseTest.captureScreenshotBase64();

            result.setAttribute("screenshotPath", filePath);

            TEST_NODE.get().info("Screenshot: <a href='" + filePath + "'>Open Screenshot</a>");
            TEST_NODE.get().addScreenCaptureFromPath(filePath);
            TEST_NODE.get().info("Embedded screenshot",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshotBase64, label).build());

            return filePath;
        } catch (Exception e) {
            TEST_NODE.get().warning("Screenshot capture failed: " + e.getMessage());
            return "";
        }
    }

    private void addExecutionRecord(ITestResult result, String status, String fallbackError, String screenshotPath) {
        ExecutionRecord record = new ExecutionRecord();
        Object[] params = result.getParameters();

        record.testCaseId = value(result, "caseId", params.length > 0 ? String.valueOf(params[0]) : result.getMethod().getMethodName());
        record.scenario = value(result, "scenario", "");
        record.input = value(result, "input", "");
        record.expected = value(result, "expected", "");
        record.actual = value(result, "actual", "");
        record.status = status;

        String attributedError = value(result, "errorMessage", "");
        record.errorMessage = !attributedError.isEmpty() ? attributedError : fallbackError;
        record.screenshotUrl = screenshotPath;

        RECORDS.add(record);
    }

    private String value(ITestResult result, String key, String fallback) {
        Object attribute = result.getAttribute(key);
        return attribute == null ? fallback : String.valueOf(attribute);
    }

    private void logTestDetails(ITestResult result, ExtentTest test) {
        test.info("Scenario: " + value(result, "scenario", ""));
        test.info("Input: " + value(result, "input", ""));
        test.info("Expected: " + value(result, "expected", ""));
        test.info("Actual: " + value(result, "actual", ""));
        test.info("Error Message: " + value(result, "errorMessage", ""));
    }
}
