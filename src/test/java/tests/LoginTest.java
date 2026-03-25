package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @DataProvider(name = "nameValidationCases")
    public Object[][] nameValidationCases() {
        String long255 = "A".repeat(260);

        return new Object[][]{
                {"NAME_TC_01", "Valid standard name", "Yash", "Dhiman", true},
                {"NAME_TC_02", "Single chars (negative test case)", "A", "B", false},
                {"NAME_TC_03", "Small chars (negative test case)", "ab", "cd", false},
                {"NAME_TC_04", "Leading/trailing spaces (negative test case)", "  Yash", "Dhiman  ", false},
                {"NAME_TC_05", "Empty first name", "", "Dhiman", false},
                {"NAME_TC_06", "Empty last name", "Yash", "", false},
                {"NAME_TC_07", "Both empty", "", "", false},
                {"NAME_TC_08", "Only spaces", "   ", "   ", false},
                {"NAME_TC_09", "Numeric first name", "1234", "Dhiman", false},
                {"NAME_TC_10", "Special chars in names", "@#$", "%^&", false},
                {"NAME_TC_11", "Long text 255+", long255, long255, false}
        };
    }

    @DataProvider(name = "mobileValidationCases")
    public Object[][] mobileValidationCases() {
        return new Object[][]{
                {"MOB_TC_01", "Valid 10-digit number", "9876543210", true},
                {"MOB_TC_02", "Leading zero number (invalid as per rule set)", "0987654321", false},
                {"MOB_TC_03", "Less than 10 digits", "12345", false},
                {"MOB_TC_04", "More than 10 digits", "123456789012", false},
                {"MOB_TC_05", "Alphabets", "abcdefghij", false},
                {"MOB_TC_06", "Special characters", "@#$%^&*()!", false},
                {"MOB_TC_07", "Only spaces", "          ", false}
        };
    }

    @Test(dataProvider = "nameValidationCases")
    public void validateNameFlow(String testCaseId,
                                 String scenario,
                                 String firstName,
                                 String lastName,
                                 boolean shouldNavigateToMobileScreen) {
        ITestResult result = Reporter.getCurrentTestResult();

        navigateToSignUpForm();
        fillNameFields(firstName, lastName);

        boolean continueClicked = clickContinue();
        boolean navigatedToNextScreen = isNextMobileScreenVisible();
        boolean firstNameErrorVisible = isFirstNameErrorVisible();
        boolean lastNameErrorVisible = isLastNameErrorVisible();
        boolean anyNameError = firstNameErrorVisible || lastNameErrorVisible;

        String expected = shouldNavigateToMobileScreen
                ? "Navigate to mobile screen; no first/last name error"
                : "Must NOT navigate. Pass if name error is shown OR Continue is disabled";

        String actual = "continueClicked=" + continueClicked
                + ", navigatedToMobile=" + navigatedToNextScreen
                + ", firstNameError=" + firstNameErrorVisible
                + ", lastNameError=" + lastNameErrorVisible;

        String errorMsg = anyNameError
                ? (firstNameErrorVisible && lastNameErrorVisible
                    ? "First name is required; Last name is required"
                    : firstNameErrorVisible
                        ? "First name is required"
                        : "Last name is required")
                : "No explicit first/last name error shown";

        setReportData(result, testCaseId, scenario,
                "firstName=" + firstName + ", lastName=" + lastName,
                expected,
                actual,
                errorMsg);

        if (shouldNavigateToMobileScreen) {
            Assert.assertTrue(continueClicked, testCaseId + " - Continue click did not happen for positive case.");
            Assert.assertTrue(navigatedToNextScreen, testCaseId + " - Valid input did not navigate to mobile screen.");
            Assert.assertFalse(anyNameError, testCaseId + " - Unexpected first/last name error shown.");
        } else {
            Assert.assertFalse(navigatedToNextScreen,
                    testCaseId + " - Invalid input navigated to mobile screen. This is FAIL by rule.");
            Assert.assertTrue(anyNameError || !continueClicked,
                    testCaseId + " - Neither validation error was shown nor Continue was disabled.");
        }
    }

    @Test(dataProvider = "mobileValidationCases")
    public void validateMobileFlow(String testCaseId,
                                   String scenario,
                                   String mobileNumber,
                                   boolean isValidMobile) {
        ITestResult result = Reporter.getCurrentTestResult();

        navigateToSignUpForm();
        fillNameFields("Yash", "Dhiman");

        boolean nameContinueClicked = clickContinue();
        boolean mobileScreenVisible = isNextMobileScreenVisible();

        if (!(nameContinueClicked && mobileScreenVisible)) {
            setReportData(result, testCaseId, scenario,
                    "mobile=" + mobileNumber,
                    "Should reach mobile screen from valid name step before mobile validation",
                    "nameContinueClicked=" + nameContinueClicked + ", mobileScreenVisible=" + mobileScreenVisible,
                    "Could not reach mobile screen");
            Assert.fail(testCaseId + " - Could not reach mobile screen from valid name step.");
        }

        fillMobileField(mobileNumber);
        boolean mobileContinueClicked = clickContinue();
        boolean mobileErrorVisible = isMobileErrorVisible();
        boolean stillOnMobileScreen = isNextMobileScreenVisible();

        String expected = isValidMobile
                ? "Continue should work and no mobile error should appear"
                : "Pass if mobile error appears OR Continue is disabled; must remain on mobile screen";

        String actual = "continueClicked=" + mobileContinueClicked
                + ", mobileErrorVisible=" + mobileErrorVisible
                + ", stillOnMobileScreen=" + stillOnMobileScreen;

        String errorMsg = mobileErrorVisible
                ? "Please enter a valid 10-digit mobile number"
                : "No explicit mobile error shown";

        setReportData(result, testCaseId, scenario,
                "mobile=" + mobileNumber,
                expected,
                actual,
                errorMsg);

        if (isValidMobile) {
            Assert.assertTrue(mobileContinueClicked, testCaseId + " - Continue disabled for valid mobile.");
            Assert.assertFalse(mobileErrorVisible, testCaseId + " - Mobile error shown for valid number.");
        } else {
            Assert.assertTrue(stillOnMobileScreen,
                    testCaseId + " - Invalid mobile moved away from mobile screen.");
            Assert.assertTrue(mobileErrorVisible || !mobileContinueClicked || stillOnMobileScreen,
                    testCaseId + " - Neither mobile error was shown nor Continue was disabled.");
        }
    }

    private void setReportData(ITestResult result,
                               String caseId,
                               String scenario,
                               String input,
                               String expected,
                               String actual,
                               String errorMessage) {
        result.setAttribute("caseId", caseId);
        result.setAttribute("scenario", scenario);
        result.setAttribute("input", input);
        result.setAttribute("expected", expected);
        result.setAttribute("actual", actual);
        result.setAttribute("errorMessage", errorMessage);
    }
}
