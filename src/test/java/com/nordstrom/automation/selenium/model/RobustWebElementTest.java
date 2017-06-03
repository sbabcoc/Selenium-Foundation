package com.nordstrom.automation.selenium.model;

import java.net.URISyntaxException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

@Listeners({ListenerChain.class})
public class RobustWebElementTest implements ListenerChainable {
	
	private static final String DOC_NAME = "ExamplePage.html";
	
	@BeforeMethod
	public void getPageDoc() throws URISyntaxException {
		WebDriver driver = DriverManager.getDriver();
		driver.get(getClass().getClassLoader().getResource(DOC_NAME).toString());
		DriverManager.setInitialPage(new ExamplePage(driver));
	}
	
	@Test
	public void testRefresh() {
		ExamplePage page = getPage();
		TableComponent component = page.getTable();
		List<WebElement> elements = component.findElements(By.cssSelector("th"));
		page.getDriver().navigate().refresh();
		elements.get(0).getTagName();
		elements.get(1).getTagName();
	}

	private ExamplePage getPage() {
		return (ExamplePage) DriverManager.getInitialPage();
	}
	
	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
	
}
