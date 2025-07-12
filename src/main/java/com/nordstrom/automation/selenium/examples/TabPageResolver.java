package com.nordstrom.automation.selenium.examples;

import com.nordstrom.automation.selenium.exceptions.UnresolvedContainerTypeException;
import com.nordstrom.automation.selenium.interfaces.ContainerResolver;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class implements the container resolver for the tab pages opened by the "Open A/B Tab" button on the 'Example'
 * page.
 */
public class TabPageResolver implements ContainerResolver<TabPage> {
    @Override
    public TabPage resolve(TabPage container) {
        RobustWebElement element = container.findOptional(TabPage.Using.HEADING);
        if (element.hasReference()) {
            String pageContent = element.getText();
            if (pageContent.equals(TabPageA.EXPECT_CONTENT)) {
                return new TabPageA(container.getWrappedDriver());
            }
            if (pageContent.equals(TabPageB.EXPECT_CONTENT)) {
                return new TabPageB(container.getWrappedDriver());
            }
           throw new UnresolvedContainerTypeException("Unsupported tab page heading: " + pageContent);
        }
        throw new UnresolvedContainerTypeException("Tab page heading not found");
    }
}
