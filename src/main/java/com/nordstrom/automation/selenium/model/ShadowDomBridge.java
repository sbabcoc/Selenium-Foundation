package com.nordstrom.automation.selenium.model;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.utility.SearchContextUtils;
import com.nordstrom.automation.selenium.utility.SearchContextUtils.ContextType;

/**
 * This class implements a search context for pre-W3C shadow DOM element search.
 */
public final class ShadowDomBridge extends PageComponent {
    
    private ShadowDomBridge(final RobustWebElement element, final ComponentContainer parent) {
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
     * <b>NOTE</b>: If the targeted browser requires <b>ShadowDomBridge</b> and the script arguments include
     * <b>ShadowDomBridge</b> objects, this method will revise the specified script and alter the corresponding
     * arguments to retrieve and use references to the associated shadow root nodes. Otherwise, script and
     * arguments are unaltered.
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute
     * @param args The arguments to the script. May be empty
     * @return revised script if arguments include {@link ShadowDomBridge} objects; otherwise, original script
     */
    public static String injectShadowArgs(final WebDriver driver, final String js, final Object... args) {
        // if not using ShadowDomBridge
        if ( ! useShadowDomBridge(driver)) {
            return js; // return without altering anything
        }
        
        String origin = js;
        StringBuilder script = new StringBuilder();
        
        // iterate over arguments
        for (int i = 0; i < args.length; i++) {
            // if this argument is a ShadowDomBridge
            if (args[i] instanceof ShadowDomBridge) {
                // replace argument with shadow host element
                args[i] = ((ShadowDomBridge) args[i]).getWrappedElement();
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
     * @return shadow root context; {@code null} if not using ShadowDomBridge 
     * @throws ShadowRootContextException if unable to acquire shadow root
     */
    static ShadowDomBridge getShadowRoot(final SearchContext context) {
        // if using ShadowDomBridge
        if (useShadowDomBridge(context)) {
            ShadowRoot shadowRoot;
            RobustWebElement element;
            if (context instanceof ShadowRoot) {
                shadowRoot = (ShadowRoot) context;
                element = (RobustWebElement) shadowRoot.getWrappedElement();
            } else if (context instanceof RobustWebElement) {
                element = (RobustWebElement) context;
                ComponentContainer parent = SearchContextUtils.getContainingContext(element);
                shadowRoot = new ShadowRoot(element, parent);
            } else {
                return null;
            }
            // return a ShadowDomBridge object
            return new ShadowDomBridge(element, shadowRoot);
        }
        return null;
    }
    
    private static boolean useShadowDomBridge(final SearchContext context) {
        // if running Firefox on Selenium 3
        if ((SeleniumConfig.getConfig().getVersion() == 3) && "firefox".equals(WebDriverUtils.getBrowserName(context))) {
            return true;
        }
        // if running Safari on iOS
        if (Platform.IOS.equals(WebDriverUtils.getPlatform(context)) && "Safari".equals(WebDriverUtils.getBrowserName(context))) {
            return true;
        }
        return false;
    }

}
