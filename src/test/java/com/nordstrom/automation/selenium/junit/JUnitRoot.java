package com.nordstrom.automation.selenium.junit;

import org.junit.BeforeClass;

import com.nordstrom.automation.selenium.core.ModelTestCore;

public class JUnitRoot extends JUnitBase {

	@BeforeClass
	public static void beforeClass() {
		ModelTestCore.setHubAsTarget();
	}

}
