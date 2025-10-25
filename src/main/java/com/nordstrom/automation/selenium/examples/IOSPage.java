package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the target view of the sample application used by the Appium iOS unit test.
 */
public class IOSPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public IOSPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        INTEGER_A(By.id("IntegerA")),
        INTEGER_B(By.id("IntegerB")),
        COMPUTE_SUM(By.id("ComputeSumButton")),
        ANSWER(By.id("Answer"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public int computeSum(int a, int b) {
    	updateIntegerA(a);
    	updateIntegerB(b);
    	return computeSum();
    }
    
    public void updateIntegerA(int a) {
    	findElement(Using.INTEGER_A).sendKeys(Integer.toString(a));
    }
    
    public void updateIntegerB(int b) {
    	findElement(Using.INTEGER_B).sendKeys(Integer.toString(b));
    }
    
    public int computeSum() {
    	findElement(Using.COMPUTE_SUM).click();
    	return getAnswerAsInt();
    }
    
    public int getAnswerAsInt() {
    	try {
    		return Integer.parseInt(getAnswerAsString());
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }
    
    public String getAnswerAsString() {
    	return findElement(Using.ANSWER).getText();
    }
    
}
