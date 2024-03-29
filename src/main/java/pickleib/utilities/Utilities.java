package pickleib.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import context.ContextStore;
import io.appium.java_client.functions.ExpectedCondition;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.openqa.selenium.support.ui.WebDriverWait;
import pickleib.enums.ElementState;
import pickleib.exceptions.PickleibException;
import pickleib.utilities.screenshot.ScreenCaptureUtility;
import collections.Bundle;
import utils.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static pickleib.enums.ElementState.*;
import static pickleib.utilities.element.ElementAcquisition.*;
import static pickleib.web.driver.WebDriverFactory.getDriverTimeout;
import static utils.StringUtilities.Color.*;
import static utils.StringUtilities.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class Utilities {

    static {PropertyLoader.load();}

    public ScreenCaptureUtility capture = new ScreenCaptureUtility();
    public ObjectMapper objectMapper = new ObjectMapper();
    public Printer log = new Printer(this.getClass());
    public RemoteWebDriver driver;

    public long elementTimeout = Long.parseLong(ContextStore.get("element-timeout", "15000"));

    protected Utilities(RemoteWebDriver driver){
        this.driver = driver;
    }

    /**
     * Highlights a given text with a specified color (resets to plain)
     *
     * @param color target color
     * @param text target text
     */
    protected String highlighted(StringUtilities.Color color, CharSequence text){
        StringJoiner colorFormat = new StringJoiner("", color.getValue(), RESET.getValue());
        return String.valueOf(colorFormat.add(text));
    }

    /**
     * Acquires a specified attribute of a given element
     *
     * @param element target element
     * @param attribute target attribute
     * @return returns the element attribute
     */
    protected String getAttribute(WebElement element, String attribute){return element.getAttribute(attribute);}

    /**
     * Clicks an element after waiting for its state to be enabled
     *
     * @param element target element
     */
    protected void clickElement(WebElement element){
        clickElement(element, null);
    }

    /**
     * Clicks the specified {@code element} with retry mechanism and optional scrolling.
     *
     * <p>
     * This method attempts to click the given {@code element} with a retry mechanism.
     * It uses an implicit wait of 500 milliseconds during the retry attempts.
     * The method supports an optional {@code scroller} for scrolling before clicking the element.
     * If the {@code scroller} is provided, it scrolls towards the specified location before clicking.
     * </p>
     *
     * <p>
     * The method logs warning messages during the iteration process, indicating WebDriver exceptions.
     * After the maximum time specified by {@code elementTimeout}, if the element is still not clickable,
     * a {@code PickleibException} is thrown, including the last caught WebDriver exception.
     * </p>
     *
     * @param element   The target {@code WebElement} to be clicked with retry mechanism.
     * @param scroller  The {@code ScrollFunction} for scrolling before clicking. If {@code null}, no scrolling is performed.
     * @throws PickleibException If the element is not clickable after the retry attempts, a {@code PickleibException} is thrown
     *                          with the last caught WebDriver exception.
     */ //TODO: clickElement should use iterativeConditionalInvocation() instead of iterating in itself. (same for other similar methods).
    protected void clickElement(WebElement element, ScrollFunction scroller){
        long initialTime = System.currentTimeMillis();
        WebDriverException caughtException = null;
        int counter = 0;
        do {
            try {
                driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
                if (counter > 0 && scroller != null) clickTowards(scroller.scroll(element));
                else if (scroller != null) scroller.scroll(element).click();
                else element.click();
                return;
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException;
                }
                else if (!webDriverException.getClass().getName().equals(caughtException.getClass().getName())){
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException;
                }
                counter++;
            }
            finally {
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(getDriverTimeout()));
            }
        }
        while (!(System.currentTimeMillis() - initialTime > elementTimeout));
        if (counter > 0) log.warning("Iterated " + counter + " time(s)!");
        log.warning(caughtException.getMessage());
        throw new PickleibException(caughtException);
    }

    /**
     *
     * If present, click element {element name} on the {page name}
     * It does not scroll by default.
     *
     * @param element target element
     */
    public void clickButtonIfPresent(WebElement element){
        clickButtonIfPresent(element, null);
    }

    /**
     * If present, clicks the specified {@code element} on the {page name}.
     * It scrolls according to the provided {@code ScrollFunction} by default.
     *
     * <p>
     * This method checks if the given {@code element} is present and displayed.
     * If the element is present and displayed, it is clicked using the provided {@code scroller} for scrolling.
     * If the element is not present, a {@code WebDriverException} is caught, and a warning message is logged.
     * </p>
     *
     * @param element   The target {@code WebElement} to be clicked if present.
     * @param scroller  The {@code ScrollFunction} to be used for scrolling. If {@code null}, the default scrolling behavior is applied.
     */
    public void clickButtonIfPresent(WebElement element, ScrollFunction scroller){
        try {if (elementIs(element, ElementState.displayed)) clickElement(element, scroller);}
        catch (WebDriverException ignored){log.warning("The element was not present!");}
    }

    /**
     *
     * Click on a button that contains {button text} text
     * It does not scroll by default.
     *
     * @param buttonText target button text
     */
    public void clickButtonByItsText(String buttonText) {
        log.info("Clicking button by its text " + highlighted(BLUE, buttonText));
        WebElement element = getElementByText(buttonText);
        clickElement(element);
    }

    /**
     *
     * Click on a button that contains {button text} text
     * It does not scroll by default.
     *
     * @param buttonText target button text
     * @param scroller  The {@code ScrollFunction} to be used for scrolling. If {@code null}, the default scrolling behavior is applied.
     */
    public void clickButtonByItsText(String buttonText, ScrollFunction scroller) {
        log.info("Clicking button by its text " + highlighted(BLUE, buttonText));
        WebElement element = getElementByText(buttonText);
        clickElement(element, scroller);
    }

    /**
     *
     * Press {target key} key on {element name} element of the {}
     *
     * @param keys target keys
     * @param elementName target element name
     * @param pageName specified page instance name
     */
    public void pressKeysOnElement(WebElement element, String elementName, String pageName, Keys... keys){
        String combination = Keys.chord(keys);
        log.info("Pressing " + markup(BLUE, combination) + " keys on " + markup(BLUE, elementName) + " element.");
        element.sendKeys(combination);
    }

    /**
     * Click coordinates specified by the given offsets from the center of a given element
     *
     * @param element target element
     */
    protected void clickTowards(WebElement element){
        elementIs(element, ElementState.displayed);
        Actions builder = new org.openqa.selenium.interactions.Actions(driver);
        builder
                .moveToElement(element, 0, 0)
                .click()
                .build()
                .perform();
    }

    /**
     * Clicks an element if its present (in enabled state)
     *
     * @param element target element
     * @param scroller  The {@code ScrollFunction} to be used for scrolling. If {@code null}, the default scrolling behavior is applied.
     */
    protected void clickIfPresent(WebElement element, ScrollFunction scroller){
        try {clickElement(element, scroller);}
        catch (WebDriverException exception){log.warning(exception.getMessage());}
    }

    /**
     * Clicks an element if its present (in enabled state)
     * Does not scroll by default.
     *
     * @param element target element
     */
    protected void clickIfPresent(WebElement element){
        try {clickElement(element, null);}
        catch (WebDriverException exception){log.warning(exception.getMessage());}
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     * @param verify verifies the input text value equals to an expected text if true
     * @param scroller  The {@code ScrollFunction} to be used for scrolling. If {@code null}, the default scrolling behavior is applied.
     */
    protected void clearFillInput(WebElement inputElement, String inputText, ScrollFunction scroller, boolean verify){
        fillInputElement(inputElement, inputText, scroller, verify);
    }

    /**
     * Clears and fills a given input
     * Does not scroll by default.
     *
     * @param inputElement target input element
     * @param inputText input text
     * @param verify verifies the input text value equals to an expected text if true
     */
    protected void clearFillInput(WebElement inputElement, String inputText, boolean verify){
        fillInputElement(inputElement, inputText, verify);
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     */
    protected void fillInput(WebElement inputElement, String inputText){
        // This method clears the input field before filling it
        fillInputElement(inputElement, inputText, false);
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     */
    protected void fillAndVerifyInput(WebElement inputElement, String inputText){
        // This method clears the input field before filling it
        fillInputElement(inputElement, inputText, true);
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     */
    protected void fillAndVerifyInput(WebElement inputElement, String inputText, ScrollFunction scroller){
        // This method clears the input field before filling it
        fillInputElement(inputElement, inputText, scroller, true);
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     * @param verify verifies the input text value equals to an expected text if true
     */
    protected void fillInputElement(WebElement inputElement, String inputText, boolean verify){
        fillInputElement(inputElement, inputText, null, verify);
    }

    /**
     * Clears and fills a given input
     *
     * @param inputElement target input element
     * @param inputText input text
     * @param verify verifies the input text value equals to an expected text if true
     * @param scroller  The {@code ScrollFunction} to be used for scrolling. If {@code null}, the default scrolling behavior is applied.
     */
    protected void fillInputElement(WebElement inputElement, String inputText, ScrollFunction scroller, boolean verify){
        // This method clears the input field before filling it
        elementIs(inputElement, ElementState.displayed);
        inputText = contextCheck(inputText);
        if (scroller != null) scroller.scroll(inputElement).sendKeys(inputText);
        else inputElement.sendKeys(inputText);
        assert !verify || inputText.equals(inputElement.getAttribute("value"));
    }

    /**
     * Verifies a given element is in expected state
     *
     * @param element target element
     * @param state expected state
     * @return returns the element if its in expected state
     */
    protected WebElement verifyElementState(WebElement element, ElementState state){
        if (!elementIs(element, state)) throw new PickleibException("Element is not in " + state.name() + " state!");
        log.success("Element state is verified to be: " + state.name());
        return element;
    }

    /**
     * Waits until a given element is in expected state
     *
     * @param element target element
     * @param state expected state
     * @return returns true if an element is in the expected state
     */ //TODO: elementIs should use iterativeConditionalInvocation() instead of iterating in itself. (same for other similar methods).
    protected Boolean elementIs(WebElement element, @NotNull ElementState state){
        long initialTime = System.currentTimeMillis();
        String caughtException = null;
        boolean timeout;
        boolean condition = false;
        boolean negativeCheck = false;
        int counter = 0;
        do { //TODO: Replace this with iterativeConditionalInvocation
            if (condition || (counter > 1 && negativeCheck)) return true;
            try {
                driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
                switch (state) {
                    case enabled -> {
                        negativeCheck = false;
                        condition = element.isEnabled();
                    }
                    case displayed -> {
                        negativeCheck = false;
                        condition = element.isDisplayed();
                    }
                    case selected -> {
                        negativeCheck = false;
                        condition = element.isSelected();
                    }
                    case disabled -> {
                        negativeCheck = true;
                        condition = !element.isEnabled();
                    }
                    case unselected -> {
                        negativeCheck = true;
                        condition = !element.isSelected();
                    }
                    case absent -> {
                        negativeCheck = true;
                        condition = !element.isDisplayed();
                    }
                    default -> throw new EnumConstantNotPresentException(ElementState.class, state.name());
                }
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                else if (!webDriverException.getClass().getName().equals(caughtException)){
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                else if (state.equals(absent) && webDriverException.getClass().getName().equals("StaleElementReferenceException"))
                    return true;
                counter++;
            }
            finally {
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(getDriverTimeout()));
            }
        }
        while (!(System.currentTimeMillis() - initialTime > elementTimeout));
        if (counter > 0) log.warning("Iterated " + counter + " time(s)!");
        return false;
    }

    /**
     * Clicks an element acquired by text without scrolling.
     *
     * <p>
     * This method locates an element by its text using {@code getElementByText}.
     * If the element is found, it is clicked without any scrolling.
     * </p>
     *
     * @param buttonText The text of the target element to be clicked.
     */
    protected void clickButtonWithText(String buttonText){
        clickElement(getElementByText(buttonText), null);
    }

    /**
     * Clicks an element acquired by text with optional scrolling.
     *
     * <p>
     * This method locates an element by its text using {@code getElementByText}.
     * If the element is found, it is clicked with optional scrolling using the provided {@code scroller}.
     * </p>
     *
     * @param buttonText The text of the target element to be clicked.
     * @param scroller   The {@code ScrollFunction} for scrolling before clicking. If {@code null}, no scrolling is performed.
     */
    protected void clickButtonWithText(String buttonText, ScrollFunction scroller){
        clickElement(getElementByText(buttonText), scroller);
    }

    /**
     * Clears an input element
     *
     * @param element target element
     */
    protected WebElement clearInputField(@NotNull WebElement element){
        int textLength = element.getAttribute("value").length();
        for(int i = 0; i < textLength; i++){element.sendKeys(Keys.BACK_SPACE);}
        return element;
    }

    /**
     * Acquires an element by its text
     *
     * @param elementText target element text
     */
    protected WebElement getElementByText(String elementText){
        try {
            return driver.findElement(By.xpath("//*[text()='" +elementText+ "']"));
        }
        catch (NoSuchElementException exception){
            throw new NoSuchElementException(GRAY+exception.getMessage()+RESET);
        }
    }

    /**
     * Acquires an element that contains a certain text
     *
     * @param elementText target element text
     */
    protected WebElement getElementContainingText(String elementText){
        try {
            return driver.findElement(By.xpath("//*[contains(text(), '" +elementText+ "')]"));
        }
        catch (NoSuchElementException exception){
            throw new NoSuchElementException(GRAY+exception.getMessage()+RESET);
        }
    }

    /**
     * Drags and drops a given element on top of another element
     *
     * @param element element that drags
     * @param destinationElement target element
     */
    protected void dragDropToAction(WebElement element, WebElement destinationElement){
        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .moveToElement(destinationElement)
                .release()
                .build()
                .perform();
        waitFor(0.5);
    }

    /**
     * Drags a given element to coordinates specified by offsets from the center of the element
     *
     * @param element target element
     * @param xOffset x offset from the center of the element
     * @param yOffset y offset from the center of the element
     */
    //This method performs click, hold, dragAndDropBy action on at a certain offset
    protected void dragDropByAction(WebElement element, int xOffset, int yOffset){
        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .dragAndDropBy(element, xOffset, yOffset)
                .build()
                .perform();
        waitFor(0.5);
    }

    /**
     * Drags a given element to coordinates specified by offsets from the center of the element
     * Uses moveToElement()
     *
     * @param element target element
     * @param xOffset x offset from the center of the element
     * @param yOffset y offset from the center of the element
     */
    protected void dragDropAction(WebElement element, int xOffset, int yOffset){
        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .moveToElement(element, xOffset, yOffset)
                .release()
                .build()
                .perform();
        waitFor(0.5);
    }

    /**
     * Refreshes the current page
     *
     */
    protected void refreshThePage(){
        driver.navigate().refresh();
    }

    /**
     * Click coordinates specified by the given offsets from the center of a given element
     *
     * @param element target element
     * @param xOffset x offset from the center of the element
     * @param yOffset y offset from the center of the element
     */
    @SuppressWarnings("SameParameterValue")
    protected void clickAtAnOffset(WebElement element, int xOffset, int yOffset){
        Actions builder = new org.openqa.selenium.interactions.Actions(driver);
        builder
                .moveToElement(element, xOffset, yOffset)
                .click()
                .build()
                .perform();
    }

    /**
     * Switches to present alert
     *
     * @return returns the alert
     */
    protected Alert getAlert(){return driver.switchTo().alert();}

    /**
     * Uploads a given file
     *
     * @param fileUploadInput upload element
     * @param directory absolute file directory (excluding the file name)
     * @param fileName file name (including a file extension)
     */
    protected void uploadFile(@NotNull WebElement fileUploadInput, String directory, String fileName){fileUploadInput.sendKeys(directory+"/"+fileName);}

    /**
     * Combines the given keys
     *
     * @param keys key inputs
     */
    protected String combineKeys(Keys... keys) {
        return Keys.chord(keys);
    }

    /**
     * Waits for a certain while
     *
     * @param seconds duration as a double
     */
    //This method makes the thread wait for a certain while
    protected void waitFor(double seconds){
        if (seconds > 1) log.info("Waiting for " + markup(BLUE, String.valueOf(seconds)) + " seconds");
        try {Thread.sleep((long) (seconds* 1000L));}
        catch (InterruptedException exception){
            throw new PickleibException(highlighted(GRAY, exception.getLocalizedMessage()));
        }
    }

    /**
     * Waits actively for the page to load up to 10 seconds
     */
    protected void waitUntilLoads(int waitingTime) {
        long startTime = System.currentTimeMillis();
        String url = driver.getCurrentUrl();
        log.info("Waiting for page to be loaded -> " + markup(BLUE, url));

        ExpectedCondition<Boolean> pageLoadCondition = driverLoad ->
        {
            assert driverLoad != null;
            return ((JavascriptExecutor) driverLoad).executeScript("return document.readyState").equals("complete");
        };

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitingTime));
        wait.until(pageLoadCondition);
        long elapsedTime = System.currentTimeMillis() - startTime;
        int elapsedTimeSeconds = (int) ((double) elapsedTime / 1000);
        log.info("The page is loaded in " + elapsedTimeSeconds + " second(s)");
    }

    /**
     * Gets the parent class from a child element using a selector class
     *
     * @param childElement element that generates the parent class
     * @param current empty string (at the beginning)
     * @param parentSelectorClass selector class for selecting the parent elements
     * @return returns the targeted parent element
     */
    protected WebElement getParentByClass(WebElement childElement, String current, String parentSelectorClass) {

        if (current == null) {current = "";}

        String childTag = childElement.getTagName();

        if (childElement.getAttribute("class").contains(parentSelectorClass)) return childElement;

        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));

        int count = 0;
        for (WebElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) count++;
            if (childElement.equals(childrenElement)) {
                return getParentByClass(parentElement, "/" + childTag + "[" + count + "]" + current, parentSelectorClass);
            }
        }
        return null;
    }

    /**
     * Generate a xPath for a given element
     *
     * @param childElement web element gets generated a xPath from
     * @param current empty string (at the beginning)
     * @return returns generated xPath
     */
    protected String generateXPath(@NotNull WebElement childElement, String current) {
        String childTag = childElement.getTagName();
        if (childTag.equals("html")) {return "/html[1]" + current;}
        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
        int count = 0;
        for (WebElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) count++;
            if (childElement.equals(childrenElement)) {
                return generateXPath(parentElement, "/" + childTag + "[" + count + "]" + current);
            }
        }
        return null;
    }

    /**
     * Checks if an event was fired
     * Create a custom script to listen for an event by generating a unique event key and catches this key in the console
     * Ex: "dataLayerObject.listen(eventName, function(){console.warn(eventKey)});"
     *
     * @param eventName event name of the event that is expected to be fired
     * @param listenerScript script for calling the listener, ex: "dataLayerObject.listen( eventName );"
     * @return true if the specified event was fired.
     */
    protected boolean isEventFired(String eventName, String listenerScript){
        log.info("Listening to '" + eventName + "' event");
        String eventKey = generateRandomString(eventName + "#", 6, false, true);
        listenerScript = listenerScript.replace(eventName, "'" + eventName + "', function(){console.warn('" + eventKey +"')}");
        executeScript(listenerScript);
        LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry: logs.getAll())
            if (entry.toString().contains(eventKey)) {
                log.success("'" + eventName + "' event is fired!");
                return true;
            }
        log.warning(eventName + " event is not fired!");
        return false;
    }

    /**
     * Checks if an event was fired
     *
     * @param eventKey key that is meant to be caught from the console in case the event fires
     * @param listenerScript script for calling the listener, ex: "dataLayerObject.listen('page.info', function(){console.warn(eventKey)});"
     * @return true if the specified event was fired.
     */
    protected boolean isEventFiredByScript(String eventKey, String listenerScript){
        log.info("Listening to '" + markup(BLUE, eventKey) + "' event");
        executeScript(listenerScript);
        LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry: logs.getAll()) if (entry.toString().contains(eventKey)) return true;
        return false;
    }

    /**
     * Executes a JS script and returns the responding object
     *
     * @param script script that is to be executed
     * @return object if the scripts yield one
     */
    protected Object executeScript(String script){
        log.info("Executing script: " + highlighted(BLUE, script));
        return ((JavascriptExecutor) driver).executeScript(script);
    }

    /**
     * Gets the name of the method that called the API.
     *
     * @return the name of the method that called the API
     */
    private static String getCallingClassName(){
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Utilities.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste.getClassName();
            }
        }
        return null;
    }

    /**
     *
     * Adds given values to the local storage
     *
     * @param form Map(String, String)
     */
    public void addValuesToLocalStorage(Map<String, String> form){
        for (String valueKey: form.keySet()) {
            RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
            RemoteWebStorage webStorage = new RemoteWebStorage(executeMethod);
            LocalStorage storage = webStorage.getLocalStorage();
            storage.setItem(valueKey, contextCheck(form.get(valueKey)));
        }
    }

    /**
     *
     * Adds given cookies
     *
     * @param cookies Map(String, String)
     */
    public void putCookies(Map<String, String> cookies){
        for (String cookieName: cookies.keySet()) {
            Cookie cookie = new Cookie(cookieName, contextCheck(cookies.get(cookieName)));
            driver.manage().addCookie(cookie);
        }
    }

    /**
     * Deletes all cookies
     */
    public void deleteAllCookies() {driver.manage().deleteAllCookies();}

    /**
     *
     * Acquire attribute {attribute name} from element {element name} on the {page name}
     * (Use 'innerHTML' attributeName to acquire text on an element)
     *
     * @param element target element
     * @param attributeName acquired attribute name
     * @param elementName target element name
     * @param pageName specified page instance name
     */
    public void updateContextByElementAttribute(
            WebElement element,
            String attributeName,
            String elementName,
            String pageName){
        log.info("Acquiring " +
                highlighted(BLUE,attributeName) +
                highlighted(GRAY," attribute of ") +
                highlighted(BLUE, elementName) +
                highlighted(GRAY," on the ") +
                highlighted(BLUE, pageName)
        );
        String attribute = element.getAttribute(attributeName);
        log.info("Attribute -> " + highlighted(BLUE, attributeName) + highlighted(GRAY," : ") + highlighted(BLUE, attribute));
        ContextStore.put(elementName + "-" + attributeName, attribute);
        log.info("Element attribute saved to the ContextStore as -> '" +
                highlighted(BLUE, elementName + "-" + attributeName) +
                highlighted(GRAY, "' : '") +
                highlighted(BLUE, attribute) +
                highlighted(GRAY, "'")
        );
    }

    /**
     *
     * Verify the text of {element name} on the {page name} to be: {expected text}
     *
     * @param element target element
     * @param expectedText expected text
     */
    public void verifyElementText(WebElement element, String expectedText){
        expectedText = contextCheck(expectedText);
        if (!expectedText.equals(element.getText()))
            throw new PickleibException("Element text is not \"" + highlighted(BLUE, expectedText) + "\"!");
        log.success("Text of the element \"" + expectedText + "\" was verified!");
    }

    /**
     *
     * Verify the text of {element name} on the {page name} to be: {expected text}
     *
     * @param element target element
     * @param expectedText expected text
     */
    public void verifyElementContainsText(WebElement element, String expectedText){
        expectedText = contextCheck(expectedText);
        elementIs(element, displayed);
        if (!element.getText().contains(expectedText))
            throw new PickleibException("Element text does not contain \"" + highlighted(BLUE, expectedText) + "\"!");
        log.success("The element text does contain \"" + expectedText + "\" text!");
    }

    /**
     *
     * Verify the text of an element from the list on the {page name}
     *
     * @param pageName specified page instance name
     */
    public void verifyListedElementText(
            List<Bundle<WebElement, String, String>> bundles,
            String pageName){
        for (Bundle<WebElement, String, String> bundle : bundles) {
            String elementName = bundle.beta();
            String expectedText = bundle.theta();
            log.info("Performing text verification for " +
                    highlighted(BLUE, elementName) +
                    highlighted(GRAY," on the ") +
                    highlighted(BLUE, pageName) +
                    highlighted(GRAY, " with the text: ") +
                    highlighted(BLUE, expectedText)
            );
            if (!expectedText.equals(bundle.alpha().getText()))
                throw new PickleibException("The " + bundle.alpha().getText() + " does not contain text '");
            log.success("Text of the element" + bundle.alpha().getText() + " was verified!");
        }
    }

    /**
     *
     * Fill form input on the {page name}
     *
     * @param bundles list of bundles where input element, input name and input texts are stored
     * @param pageName specified page instance name
     */
    public void fillInputForm(List<Bundle<WebElement, String, String>> bundles, String pageName){
        String inputName;
        String input;
        for (Bundle<WebElement, String, String> bundle : bundles) {
            log.info("Filling " +
                    highlighted(BLUE, bundle.theta()) +
                    highlighted(GRAY," on the ") +
                    highlighted(BLUE, pageName) +
                    highlighted(GRAY, " with the text: ") +
                    highlighted(BLUE, bundle.beta())
            );
            pageName = firstLetterDeCapped(pageName);
            clearFillInput(bundle.alpha(), //Input Element
                    bundle.beta(), //Input Text
                    true
            );
        }
    }

    /**
     *
     * Verify that element {element name} on the {page name} has {attribute value} value for its {attribute name} attribute
     *
     * @param element target element
     * @param attributeValue expected attribute value
     * @param attributeName target attribute name
     */
    public boolean elementContainsAttribute(
            WebElement element,
            String attributeName,
            String attributeValue) {

        long initialTime = System.currentTimeMillis();
        String caughtException = null;
        int counter = 0;
        attributeValue = contextCheck(attributeValue);
        do {
            try {
                if (Objects.equals(element.getAttribute(attributeName), attributeValue))
                    return element.getAttribute(attributeName).contains(attributeValue);
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                else if (!webDriverException.getClass().getName().equals(caughtException)){
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                waitFor(0.5);
                counter++;
            }
        }
        while (!(System.currentTimeMillis() - initialTime > elementTimeout));
        if (counter > 0) log.warning("Iterated " + counter + " time(s)!");
        log.warning("Element does not contain " +
                highlighted(BLUE, attributeName) +
                highlighted(GRAY, " -> ") +
                highlighted(BLUE, attributeValue) +
                highlighted(GRAY, " attribute pair.")
        );
        log.warning(caughtException);
        return false;
    }

    /**
     * Verify that an attribute {attribute name} of element {element name} contains a specific {value}.
     *
     * @param elementName   the name of the element to be verified
     * @param attributeName the name of the attribute to be verified
     * @param value         the expected part of value of the attribute
     */
    public boolean elementAttributeContainsValue(
            WebElement elementName,
            String attributeName,
            String value) {

        long initialTime = System.currentTimeMillis();
        String caughtException = null;
        int counter = 0;
        value = contextCheck(value);
        do {
            try {
                return elementName.getAttribute(attributeName).contains(value);
            } catch (WebDriverException webDriverException) {
                if (counter == 0) {
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                } else if (!webDriverException.getClass().getName().equals(caughtException)) {
                    log.warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                waitFor(0.5);
                counter++;
            }
        }
        while (!(System.currentTimeMillis() - initialTime > elementTimeout));
        if (counter > 0) log.warning("Iterated " + counter + " time(s)!");
        log.warning("Element attribute does not contain " +
                highlighted(BLUE, attributeName) +
                highlighted(GRAY, " -> ") +
                highlighted(BLUE, value) +
                highlighted(GRAY, " value.")
        );
        log.warning(caughtException);
        return false;
    }

    public static class InteractionUtilities extends Utilities {
        public ScrollFunction scroller;

        public InteractionUtilities(RemoteWebDriver driver, ScrollFunction scroller){
            super(driver);
            this.scroller = scroller;
        }

        public void click(WebElement element){
            clickElement(element, scroller);
        }

        public void fill(WebElement element, String input){
            clearFillInput(element, input, scroller, true);
        }

        public boolean elementStateIs(WebElement element, ElementState expectedState){
            return elementIs(element, expectedState);
        }

        public WebElement acquireElementFromList(List<WebElement> elements, String selectionText){
            return acquireNamedElementAmongst(elements, selectionText);
        }

        public WebElement acquireElementFromList(List<WebElement> elements, String attributeName, String attributeValue){
            return acquireElementUsingAttributeAmongst(elements, attributeName, attributeValue); //innerHTML for text
        }

        public <Component extends WebElement> Component acquireComponentFromList(List<Component> items, String selectionName){
            return acquireNamedComponentAmongst(items, selectionName);
        }

        public <Component extends WebElement> Component acquireComponentFromList(List<Component> items,
                                                              String attributeName,
                                                              String attributeValue,
                                                              String elementFieldName){
            return acquireComponentByElementAttributeAmongst(items, attributeName, attributeValue, elementFieldName);
        }
    }
}
