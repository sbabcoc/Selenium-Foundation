package com.nordstrom.automation.selenium.model;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.utility.SearchContextUtils;
import com.nordstrom.automation.selenium.utility.SearchContextUtils.ContextType;

/**
 * This class implements a search context for <b>Firefox</b> shadow DOM element search.
 */
public final class FirefoxShadowRoot extends PageComponent {
    
    private FirefoxShadowRoot(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElements(final By by) {
        String script = SearchContextUtils.buildScriptToLocateElements(ContextType.SHADOW, by);
        List<WebElement> elements = JsUtility.runAndReturn(driver, script, context);
        for (int index = 0; index < elements.size(); index++) {
            script = SearchContextUtils.buildScriptToLocateElement(ContextType.SHADOW, by, index);
            elements.set(index, 
                    RobustElementFactory.makeRobustElement(elements.get(index), (RobustWebElement) context, script));
        }
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElement(final By by) {
        String script = SearchContextUtils.buildScriptToLocateElement(ContextType.SHADOW, by, 0);
        return RobustElementFactory.makeRobustElement(null, (RobustWebElement) context, script);
    }
    
    /**
     * Prepare the specified script and arguments for invocation.
     * <p>
     * <b>NOTE</b>: If the targeted browser is Firefox and the script arguments include <b>FirefoxShadowRoot</b>
     * objects, this method will revise the specified script and alter the corresponding arguments to retrieve
     * and use references to the associated shadow root nodes. Otherwise, script and arguments are unaltered.
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute
     * @param args The arguments to the script. May be empty
     * @return revised script if arguments include {@link FirefoxShadowRoot} objects; otherwise, original script
     */
    public static String injectShadowArgs(final WebDriver driver, final String js, final Object... args) {
        // if browser isn't Firefox, return without altering anything
        if ( ! (WebDriverUtils.getBrowserName(driver).equals("firefox")) ) {
            return js;
        }
        
        String origin = js;
        StringBuilder script = new StringBuilder();
        
        // iterate over arguments
        for (int i = 0; i < args.length; i++) {
            // if this argument is a FirefoxShadowRoot
            if (args[i] instanceof FirefoxShadowRoot) {
                // replace argument with shadow host element
                args[i] = ((FirefoxShadowRoot) args[i]).getWrappedElement();
                // add statement to acquire shadow root reference
                script.append("var shadow").append(i).append(" = arguments[").append(i).append("].shadowRoot;\n");
                // replace references to original argument with shadow root variables
                origin = origin.replaceAll("arguments\\[" + i + "\\]", "shadow" + i);
            }
        }
        
        // return revised script (args altered)
        return script.append(origin).toString();
    }

    /**
     * Get the underlying shadow root for the specified context.
     * 
     * @param context search context
     * @return shadow root context; {@code null} if browser isn't Firefox
     * @throws ShadowRootContextException if unable to acquire shadow root
     */
    static FirefoxShadowRoot getShadowRoot(final SearchContext context) {
        // if browser is Firefox
        if (WebDriverUtils.getBrowserName(context).equals("firefox")) {
            ShadowRoot shadowRoot;
            RobustWebElement element;
            if (context instanceof ShadowRoot) {
                shadowRoot = (ShadowRoot) context;
                element = (RobustWebElement) shadowRoot.getWrappedElement();
            } else if (context instanceof RobustWebElement) {
                element = (RobustWebElement) context;
                ComponentContainer parent = SearchContextUtils.getContainingContext((SearchContext) context);
                shadowRoot = new ShadowRoot(element, parent);
            } else {
                return null;
            }
            // return a FirefoxShadowRoot object
            return new FirefoxShadowRoot(element, shadowRoot);
        }
        return null;
    }

}
