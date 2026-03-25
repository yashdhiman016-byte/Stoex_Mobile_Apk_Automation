package base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class BaseTest {

    protected AndroidDriver driver;

    // Locators for the sign-up onboarding flow.
    protected static final By SKIP_BUTTON = By.xpath("//android.widget.TextView[@text=\"Skip\"]");
    protected static final By SIGN_UP_NOW_BUTTON = By.xpath("//android.widget.TextView[@text=\"Sign Up Now\"]");
    protected static final By CONTINUE_BUTTON = By.xpath("//android.widget.TextView[@text=\"Continue\"]");

    protected static final By FIRST_NAME_FIELD = By.xpath("//android.widget.FrameLayout[@resource-id=\"android:id/content\"]/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup[1]/android.widget.EditText[1]");
    protected static final By LAST_NAME_FIELD = By.xpath("//android.widget.FrameLayout[@resource-id=\"android:id/content\"]/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup[1]/android.widget.EditText[2]");

    protected static final By FIRST_NAME_ERROR = By.xpath("//android.widget.TextView[@text=\"First name is required\"]");
    protected static final By LAST_NAME_ERROR = By.xpath("//android.widget.TextView[@text=\"Last name is required\"]");

    protected static final By NEXT_SCREEN_CONTAINER = By.xpath("//android.widget.FrameLayout[@resource-id=\"android:id/content\"]/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup[1]");
    protected static final By MOBILE_INPUT_FIELD = By.xpath("//android.widget.EditText");
    protected static final By MOBILE_ERROR = By.xpath("//android.widget.TextView[@text=\"Please enter a valid 10-digit mobile number\"]");
    protected static final By MOBILE_LABEL = By.xpath("//android.widget.TextView[contains(@text,\"Mobile\")]");
    protected static final By COUNTRY_CODE_TEXT = By.xpath("//android.widget.TextView[contains(@text,'+91')]");

    @BeforeMethod
    public void setup() throws Exception {
        // Keep capabilities configurable via Maven system properties for local/CI runs.
        String deviceName = System.getProperty("deviceName", "Pixel 3a");
        String appiumServer = System.getProperty("appiumServer", "http://127.0.0.1:4723");
        String apkPath = System.getProperty("appPath", "C:/Users/yash.dhiman/Downloads/stoex2.0.apk");

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName(deviceName);
        options.setAutomationName("UiAutomator2");
        options.setApp(apkPath);

        driver = new AndroidDriver(new URI(appiumServer).toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        System.out.println("Session started successfully.");
    }

    protected WebDriverWait waitForUi() {
        // Centralized explicit wait so all interactions use the same timeout policy.
        return new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    protected void navigateToSignUpForm() {
        // Standard entry path used by all tests before field validation starts.
        WebDriverWait wait = waitForUi();
        wait.until(ExpectedConditions.elementToBeClickable(SKIP_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_UP_NOW_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_FIELD));
    }

    protected void fillNameFields(String firstName, String lastName) {
        WebElement firstNameField = waitForUi().until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_FIELD));
        WebElement lastNameField = waitForUi().until(ExpectedConditions.visibilityOfElementLocated(LAST_NAME_FIELD));

        firstNameField.click();
        firstNameField.clear();
        if (firstName != null) {
            firstNameField.sendKeys(firstName);
        }

        lastNameField.click();
        lastNameField.clear();
        if (lastName != null) {
            lastNameField.sendKeys(lastName);
        }
    }

    protected void fillMobileField(String mobileNumber) {
        WebElement mobileInput = waitForUi().until(ExpectedConditions.visibilityOfElementLocated(MOBILE_INPUT_FIELD));
        mobileInput.click();
        mobileInput.clear();
        if (mobileNumber != null) {
            mobileInput.sendKeys(mobileNumber);
        }
    }

    protected boolean clickContinue() {
        try {
            try {
                // On some Android builds keyboard can block the Continue button.
                driver.hideKeyboard();
            } catch (Exception ignored) {
            }
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            shortWait.until(ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON)).click();
            return true;
        } catch (TimeoutException timeoutException) {
            return false;
        }
    }

    protected boolean isContinueEnabled() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement continueBtn = shortWait.until(ExpectedConditions.visibilityOfElementLocated(CONTINUE_BUTTON));
            String enabledAttr = continueBtn.getAttribute("enabled");
            if ("false".equalsIgnoreCase(enabledAttr)) {
                return false;
            }
            String clickableAttr = continueBtn.getAttribute("clickable");
            if ("false".equalsIgnoreCase(clickableAttr)) {
                return false;
            }
            return continueBtn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(By locator, int seconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isNameScreenVisible() {
        return isDisplayed(FIRST_NAME_FIELD, 4) || isDisplayed(LAST_NAME_FIELD, 4);
    }

    protected boolean isNextMobileScreenVisible() {
        // Mobile page is confirmed by mobile label + country code + mobile input.
        // Do not require name fields to be absent; that was causing false negatives.
        return isDisplayed(MOBILE_LABEL, 6)
                && isDisplayed(COUNTRY_CODE_TEXT, 6)
                && isDisplayed(MOBILE_INPUT_FIELD, 6);
    }

    protected boolean isFirstNameErrorVisible() {
        return isDisplayed(FIRST_NAME_ERROR, 4);
    }

    protected boolean isLastNameErrorVisible() {
        return isDisplayed(LAST_NAME_ERROR, 4);
    }

    protected boolean isMobileErrorVisible() {
        return isDisplayed(MOBILE_ERROR, 4);
    }

    public String captureScreenshot(String label) {
        try {
            Path reportsDir = Path.of("reports", "screenshots");
            Files.createDirectories(reportsDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeLabel = label.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path targetPath = reportsDir.resolve(safeLabel + "_" + timestamp + ".png");

            Path sourcePath = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to capture screenshot", e);
        }
    }

    public String captureScreenshotBase64() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    public void longPress(WebElement element) {
        // Appium mobile gesture API for long-press interactions.
        driver.executeScript("mobile: longClickGesture", Map.of(
                "elementId", ((RemoteWebElement) element).getId(),
                "duration", 2000
        ));
    }

    public boolean swipe(WebElement element, String direction, double percent) {
        // Returns whether further swipe in the same direction is possible.
        return (Boolean) driver.executeScript("mobile: swipeGesture", Map.of(
                "elementId", ((RemoteWebElement) element).getId(),
                "direction", direction,
                "percent", percent
        ));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
