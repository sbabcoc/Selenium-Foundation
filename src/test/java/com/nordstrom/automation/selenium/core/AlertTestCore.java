package com.nordstrom.automation.selenium.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.ExamplePage.ModalType;

@InitialPage(ExamplePage.class)
public class AlertTestCore {

    public static void testAlertModal(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        page.openAlertModal();
        assertEquals(ModalType.ALERT, page.getModalType());
        page = (ExamplePage) page.acceptModal();
        assertNull(page.getModalType(), "Modal should be gone");
        String result = page.getModalResult();
        assertNotNull(result, "Modal result should be shown");
        assertEquals("You successfully clicked an alert", result);
    }

    public static void testConfirmModal(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        page.openConfirmModal();
        assertEquals(ModalType.CONFIRM, page.getModalType());
        page = (ExamplePage) page.acceptModal();
        assertNull(page.getModalType(), "Modal should be gone");
        String result = page.getModalResult();
        assertNotNull(result, "Modal result should be shown");
        assertEquals("You clicked: Ok", result);
    }

    public static void testDismissModal(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        page.openConfirmModal();
        assertEquals(ModalType.CONFIRM, page.getModalType());
        page = (ExamplePage) page.dismissModal();
        assertNull(page.getModalType(), "Modal should be gone");
        String result = page.getModalResult();
        assertNotNull(result, "Modal result should be shown");
        assertEquals("You clicked: Cancel", result);
    }

    public static void testSubmitPromptModal(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        page.openPromptModal();
        assertEquals(ModalType.PROMPT, page.getModalType());
        page = (ExamplePage) page.sendKeysAndAccept("keys");
        assertNull(page.getModalType(), "Modal should be gone");
        String result = page.getModalResult();
        assertNotNull(result, "Modal result should be shown");
        assertEquals("You entered: keys", result);
    }

    public static void testDismissPromptModal(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        page.openPromptModal();
        assertEquals(ModalType.PROMPT, page.getModalType());
        page = (ExamplePage) page.dismissModal();
        assertNull(page.getModalType(), "Modal should be gone");
        String result = page.getModalResult();
        assertNotNull(result, "Modal result should be shown");
        assertEquals("You entered: null", result);
    }
}
