package geb.mobile.android

import geb.Browser
import geb.Page
import geb.error.UndefinedAtCheckerException
import geb.error.UnexpectedPageException
import geb.mobile.AbstractMobileNonEmptyNavigator
import geb.navigator.AbstractNavigator
import geb.navigator.EmptyNavigator
import geb.navigator.Navigator
import geb.textmatching.TextMatcher
import geb.waiting.WaitTimeoutException
import groovy.util.logging.Slf4j
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import java.util.regex.Pattern

import static java.util.Collections.EMPTY_LIST

/**
 * Created by gmueksch on 23.06.14.
 */
@Slf4j
class AndroidUIAutomatorNonEmptyNavigator extends AbstractMobileNonEmptyNavigator<AndroidDriver> {

    AndroidUIAutomatorNonEmptyNavigator(Browser browser, Collection<? extends MobileElement> contextElements) {
        super(browser,contextElements)
    }

    private String getAppPackage() {
        driver.capabilities.getCapability("appPackage")
    }

    @Override
    Navigator find(String selectorString) {
        log.debug "Selector: $selectorString"

        if (selectorString.startsWith("//")) {
            return navigatorFor(driver.findElements(By.xpath(selectorString)))
        }

//        if (selectorString.startsWith("./") ) {
//            //log.debug("Page: ${getBrowser().getPage()} ")
//            return navigatorFor(driver.findElements(By.xpath(selectorString.substring(1))))
//        }

        if (selectorString.startsWith("#")) {
            String value = selectorString.substring(1)
            String resource = "resourceId(\"$appPackage:id/$value\")"
            log.debug " android selector: $resource"
            List<WebElement> elements = driver.findElementsByAndroidUIAutomator(resource)
            if (elements.isEmpty()) {
                // TODO:  this doesn't work yet
                String scrollingResource = "new UIScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().$resource)"
                log.debug " not found, try to scroll and find: $scrollingResource"
                elements = driver.findElementByAndroidUIAutomator(scrollingResource)
            }

            return navigatorFor(elements)
        } else {
            selectorString = selectorString.replaceAll("'", '\"')
            log.debug "Using UIAutomator with: $selectorString"
            navigatorFor(driver.findElementsByAndroidUIAutomator(selectorString))
        }

    }

    @Override
    String tag() {
        _props.tagName ?: firstElement().getAttribute("tagName")
    }

    @Override
    String text() {
        firstElement().text ?: firstElement().getAttribute("name")
    }

    @Override
    Navigator unique() {
        new AndroidUIAutomatorNonEmptyNavigator(browser, contextElements.unique(false))
    }

    protected getInputValue(MobileElement input) {
        def value
        def tagName = tag()

        if (tagName == "android.widget.Spinner") {
            value = input?.findElementByAndroidUIAutomator("new UiSelector().enabled(true)").getText()
        } else if (tagName == "android.widget.CheckBox") {
            value = input.getAttribute("checked")
        } else {
            value = input.getText()
        }
        log.debug("inputValue for $tagName : $value ")
        value
    }

    @Override
    void setInputValue(WebElement input, Object value) {

        def tagName = tag()
        log.debug("setInputValue: $input, $tagName")
        if (tagName == "android.widget.Spinner") {
            if (getInputValue(input) == value) return
            setSpinnerValueWithScrollToExact(input,value)
            if( getInputValue(input) != value ) {
                setSpinnerValueWithScrollTo(input, value)
            }
        } else if (tagName in ["android.widget.CheckBox", "android.widget.RadioButton"]) {
            boolean checked = input.getAttribute("checked")
            if ( !checked && value) {
                input.click()
            } else if (checked && !value ) {
                input.click()
            }
        } else {
            input.clear()
            input.sendKeys value as String
        }
    }

    private void setSpinnerValueWithScrollTo(MobileElement input, value) {
        try {
            input.click()
            driver.scrollTo(value?.toString())?.click()
        } catch (e) {
            log.warn("Could not set $value to $input.tagName : $e.message")
        }
    }

    private void setSpinnerValueWithScrollToExact(MobileElement input, value) {
        try {
            input.click()
            driver.scrollToExact(value?.toString())?.click()
        } catch (e) {
            log.warn("Could not set $value to $input.tagName : $e.message")
        }
    }

    private void setSpinnerValueWithUISelector(MobileElement input, value) {
        try {
            input.click()
            input.findElementByAndroidUIAutomator("new UiSelector().text(\"$value\")")?.click()
            if (getInputValue(input) == value) return
            input.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().className(\"${input.tagName}\")).getChildByText(new UiSelector().enabled(true), \"${value}\")")
        } catch (e) {
            log.warn("Error selecting with UiAutomator: $e.message")
        }

    }



}