package com.nordstrom.automation.selenium.model;

import static org.testng.Assert.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

@Listeners({ListenerChain.class})
public class ModelTest implements ListenerChainable {
	
	private static final String DOC_NAME = "ExamplePage.html";
	private static final String TITLE = "Example Page";
	private static final String[] PARAS = {"This is paragraph one.", "This is paragraph two.", "This is paragraph three."};
	private static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
	private static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
	private static final String FRAME_A = "Frame A";
	private static final String FRAME_B = "Frame B";
	private static final String FRAME_C = "Frame C";
	
	@BeforeMethod
	public void getPageDoc() throws URISyntaxException {
		WebDriver driver = DriverManager.getDriver();
		URL url = getClass().getClassLoader().getResource(DOC_NAME);
		driver.get(url.toString());
		ExamplePage page = new ExamplePage(driver);
		Reporter.getCurrentTestResult().setAttribute(DOC_NAME, page);
	}
	
	@Test
	public void testBasicPage() {
		ExamplePage page = getPage();
		assertEquals(page.getTitle(), TITLE);
	}
	
	@Test
	public void testParagraphs() {
		ExamplePage page = getPage();
		List<String> paraList = page.getParagraphs();
		assertEquals(paraList.size(), 3);
		assertEquals(paraList.toArray(), PARAS);
	}
	
	@Test
	public void testTable() {
		ExamplePage page = getPage();
		TableComponent component = page.getTable();
		assertEquals(component.getHeadings().toArray(), HEADINGS);
		List<List<String>> content = component.getContent();
		assertEquals(content.size(), 3);
		assertEquals(content.get(0).toArray(), CONTENT[0]);
		assertEquals(content.get(1).toArray(), CONTENT[1]);
		assertEquals(content.get(2).toArray(), CONTENT[2]);
	}
	
	@Test
	public void testFrameByElement() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByElement();
		assertEquals(component.getPageContent(), FRAME_A);
	}

	@Test
	public void testFrameByIndex() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByIndex();
		assertEquals(component.getPageContent(), FRAME_B);
	}

	@Test
	public void testFrameById() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameById();
		assertEquals(component.getPageContent(), FRAME_C);
	}

	private ExamplePage getPage() {
		return (ExamplePage) Reporter.getCurrentTestResult().getAttribute(DOC_NAME);
	}
	
	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
	
}
