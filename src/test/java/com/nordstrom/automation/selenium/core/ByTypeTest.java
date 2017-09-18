package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.model.ComponentContainer.ByEnum;

public class ByTypeTest {
    
    private enum Using implements ByEnum {
        TAG_NAME(By.tagName("div")),
        ID(By.id("element-id")),
        NAME(By.name("name")),
        CLASS_NAME(By.className("class-name")),
        LINK_TEXT(By.linkText("link text")),
        PARTIAL_LINK(By.partialLinkText("nk te")),
        CSS_SELECTOR(By.cssSelector("iframe[id^='frame-']")),
        XPATH(By.xpath(".//iframe[contains(@id,'frame-')]"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    @Test
    public void testCssLocatorFor() {
        assertEquals(ByType.cssLocatorFor(Using.TAG_NAME), "div");
        assertEquals(ByType.cssLocatorFor(Using.ID), "#element-id");
        assertEquals(ByType.cssLocatorFor(Using.NAME), "[name=name]");
        assertEquals(ByType.cssLocatorFor(Using.CLASS_NAME), ".class-name");
        assertNull(ByType.cssLocatorFor(Using.LINK_TEXT));
        assertNull(ByType.cssLocatorFor(Using.PARTIAL_LINK));
        assertEquals(ByType.cssLocatorFor(Using.CSS_SELECTOR), "iframe[id^='frame-']");
        assertNull(ByType.cssLocatorFor(Using.XPATH));
    }
    
    @Test
    public void testXpathLocatorFor() {
        assertEquals(ByType.xpathLocatorFor(Using.TAG_NAME), ".//div");
        assertEquals(ByType.xpathLocatorFor(Using.ID), ".//*[@id='element-id']");
        assertEquals(ByType.xpathLocatorFor(Using.NAME), ".//*[@name='name']");
        assertEquals(ByType.xpathLocatorFor(Using.CLASS_NAME), ".//*[contains(concat(' ',@class,' '),' class-name ')]");
        assertEquals(ByType.xpathLocatorFor(Using.LINK_TEXT), ".//a[.='link text']");
        assertEquals(ByType.xpathLocatorFor(Using.PARTIAL_LINK), ".//a[text()[contains(.,'nk te')]]");
        assertNull(ByType.xpathLocatorFor(Using.CSS_SELECTOR));
        assertEquals(ByType.xpathLocatorFor(Using.XPATH), ".//iframe[contains(@id,'frame-')]");
    }

}
