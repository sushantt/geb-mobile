package geb.mobile

import geb.Browser
import geb.navigator.Navigator
import geb.navigator.factory.AbstractNavigatorFactory
import io.appium.java_client.MobileBy
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.WebElement

/**
 * Created by gmueksch on 23.06.14.
 */
class GebMobileNavigatorFactory extends AbstractNavigatorFactory{

    GebMobileNavigatorFactory(Browser browser) {
        super(browser, new GebMobileInnerNavigatorFactory())
    }

    @Override
    Navigator getBase() {
        List<WebElement> list

        if (browser.driver instanceof AndroidDriver) {
            // We really only want the top level element (if there is one) or top level sibblings, but for now assume
            // only 1 and the first.  The issue with the else was it was grabbing all elements which doesn't play nice
            // when using it for our context.  Tht web based navigators would have grabbed "HTML" element.

            // Another option here may be to set an empty list.  Then in the AndroidUIAutomatorNonEmptyNavigator detect
            // an empty list and send commands against the browser instead of the context.  If we do this we need to
            // override the isEmpty on AndroidUIAutomatorNonEmptyNavigator to always return false
            list = browser.driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().instance(0)")) as List
        } else {
            list = browser.driver.findElementsByXPath("//*") as List
        }

        createFromWebElements(list)
    }

}
