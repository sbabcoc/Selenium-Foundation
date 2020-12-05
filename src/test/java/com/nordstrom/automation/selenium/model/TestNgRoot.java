package com.nordstrom.automation.selenium.model;

import org.testng.annotations.BeforeClass;

import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.support.TestNgBase;

public class TestNgRoot extends TestNgBase {
	
	@BeforeClass
	public static void beforeClass() {
		ModelTestCore.setHubAsTarget();
	}

}
