package com.nordstrom.automation.selenium.model;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.utility.SearchContextUtils;
import com.nordstrom.automation.selenium.utility.SearchContextUtils.ContextType;

/**
 * This class implements a search context for <b>Firefox</b> shadow DOM element search.
 */
class FirefoxShadowRoot extends PageComponent {
    
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
        return JsUtility.runAndReturn(driver, script, context);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return this;
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
                ComponentContainer parent = SearchContextUtils.getContainingContext(element);
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
