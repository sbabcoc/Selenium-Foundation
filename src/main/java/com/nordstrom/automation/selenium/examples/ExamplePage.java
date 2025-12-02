package com.nordstrom.automation.selenium.examples;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.AlertHandler;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.model.RobustWebElement;
import com.nordstrom.automation.selenium.servlet.ExamplePageLauncher;
import com.nordstrom.common.uri.UriUtils;

/**
 * This class is the model for the 'Example' page used by Selenium Foundation unit tests.
 */
@PageUrl("/grid/admin/ExamplePageServlet")
public class ExamplePage extends Page implements DetectsLoadCompletion<ExamplePage> {

    /** page title */
    public static final String TITLE = "Example Page";
    /** text content of example paragraphs collection */
    public static final String[] PARAS =
        {"This is paragraph one.", "This is paragraph two.", "This is paragraph three.", /* hidden paragraph */ ""};
    /** text content of example table headers collection */
    public static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
    /** text content of example table rows collection */
    public static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
    /** text content of example frame 'A' */
    public static final String FRAME_A = "Frame A";
    /** text content of example frame 'B' */
    public static final String FRAME_B = "Frame B";
    /** text content of example frame 'C' */
    public static final String FRAME_C = "Frame C";
    /** text content of example frame 'D' */
    public static final String FRAME_D = "Frame D";
    /** example table context element identifier */
    public static final String TABLE_ID = "t1";
    /** text content of example shadow DOM 'A' */
    public static final String SHADOW_DOM_A = "Shadow DOM A";
    /** text content of example shadow DOM 'B' */
    public static final String SHADOW_DOM_B = "Shadow DOM B";
    
    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public ExamplePage(WebDriver driver) {
        super(driver);
        alertHandler = new ExampleAlertHandler(this);
    }
    
    private FrameComponent frameByLocator;
    private FrameComponent frameByElement;
    private FrameComponent frameByIndex;
    private FrameComponent frameById;
    private FormComponent form;
    private TableComponent table;
    private List<TableComponent> tableList;
    private Map<Object, TableComponent> tableMap;
    private List<FrameComponent> frameList;
    private Map<Object, FrameComponent> frameMap;
    private ShadowRootComponent shadowRootByLocator;
    private ShadowRootComponent shadowRootByElement;
    private List<ShadowRootComponent> shadowRootList;
    private Map<Object, ShadowRootComponent> shadowRootMap;
    private final AlertHandler alertHandler;
    private int refreshCount;
    private boolean isLoaded;
    
    /** identifier for frame 'A' */
    protected static final String FRAME_A_ID = "frame-a";
    /** identifier for frame 'B' */
    protected static final String FRAME_B_ID = "frame-b";
    /** identifier for frame 'C' */
    protected static final String FRAME_C_ID = "frame-c";
    /** identifier for frame 'D' */
    protected static final String FRAME_D_ID = "frame-d";

    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** common locator for frame elements */
        FRAME(By.cssSelector("iframe[id^='frame-']")),
        /** frame element 'A' */
        FRAME_A(By.cssSelector("iframe#frame-a")),
        /** frame element 'B' */
        FRAME_B(By.cssSelector("iframe#frame-b")),
        /** frame element 'C' */
        FRAME_C(By.cssSelector("iframe#frame-c")),
        /** frame element 'D' */
        FRAME_D(By.cssSelector("iframe#frame-d")),
        /** common locator for paragraph elements */
        PARA(By.cssSelector("p[id^='para-']")),
        /** table element */
        TABLE(By.cssSelector("table#t1")),
        /** form element */
        FORM(By.tagName("form")),
        /** input field element */
        INPUT(By.cssSelector("input#input-field")),
        /** check box element */
        CHECK(By.cssSelector("input#checkbox")),
        /** common locator shadow root elements */
        SHADOW_ROOT(By.cssSelector("div[id^='shadow-root-']")),
        /** shadow root element 'A' */
        SHADOW_ROOT_A(By.cssSelector("div#shadow-root-a")),
        /** shadow root element 'B' */
        SHADOW_ROOT_B(By.cssSelector("div#shadow-root-b")),
        /** division element */
        FORM_DIV(By.cssSelector("div#form-div")),
        /** alert button */
        ALERT(By.cssSelector("button#alert")),
        /** confirm button */
        CONFIRM(By.cssSelector("button#confirm")),
        /** prompt button */
        PROMPT(By.cssSelector("button#prompt")),
        /** result paragraph */
        RESULT(By.cssSelector("p#result")),
        /** open tab button */
        OPEN_TAB(By.cssSelector("button#open-tab"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    /**
     * This enumeration defined alert type constants. 
     */
    public enum ModalType {
        /** 'alert' modal */
        ALERT("I am a JS Alert"),
        /** 'confirm' modal */
        CONFIRM("I am a JS Confirm"),
        /** 'prompt' modal */
        PROMPT("I am a JS Prompt");
        
        private String text;

        ModalType(String text) {
            this.text = text;
        }
        
        /**
         * Convert the specified modal text to the corresponding constant.
         * 
         * @param text modal text
         * @return modal type constant
         * @throws IllegalArgumentException if specified text is unrecognized
         */
        public static ModalType fromString(String text) {
            if (text == null) return null;
            for (ModalType type : values()) {
                if (type.text.equals(text)) return type;
            }
            throw new IllegalArgumentException("Unrecognized modal text: " + text);
        }
    }
    
    /**
     * Get the automation component that models frame 'A' via the associated element locator.
     * 
     * @return {@link FrameComponent} model for frame 'A'
     */
    public FrameComponent getFrameByLocator() {
        if (frameByLocator == null) {
            frameByLocator = new FrameComponent(Using.FRAME_A.locator, this);
        }
        return frameByLocator;
    }
    
    /**
     * Get the automation component that models frame 'B' via the associated element reference.
     * 
     * @return {@link FrameComponent} model for frame 'B'
     */
    public FrameComponent getFrameByElement() {
        if (frameByElement == null) {
            RobustWebElement element = (RobustWebElement) findElement(Using.FRAME_B);
            frameByElement = new FrameComponent(element, this);
        }
        return frameByElement;
    }
    
    /**
     * Get the automation component that models frame 'C' via element list index.
     * 
     * @return {@link FrameComponent} model for frame 'C'
     */
    public FrameComponent getFrameByIndex() {
        if (frameByIndex == null) {
            frameByIndex = new FrameComponent(2, this);
        }
        return frameByIndex;
    }
    
    /**
     * Get the automation component that models frame 'D' via element identifier.
     * 
     * @return {@link FrameComponent} model for frame 'D'
     */
    public FrameComponent getFrameById() {
        if (frameById == null) {
            frameById = new FrameComponent(FRAME_D_ID, this);
        }
        return frameById;
    }
    
    /**
     * Get text content of the paragraphs collection.
     * 
     * @return list of paragraph strings
     */
    public List<String> getParagraphs() {
        return findElements(Using.PARA).stream().map(WebElement::getText).collect(Collectors.toList());
    }
    
    /**
     * Get the automation component the models the example form.
     * 
     * @return {@link FormComponent} model
     */
    public FormComponent getForm() {
        if (form == null) {
            form = new FormComponent(Using.FORM_DIV.locator, this);
        }
        return form;
    }
    
    /**
     * Get the automation component that models the first table.
     * 
     * @return {@link TableComponent} model for example table
     */
    public TableComponent getTable() {
        if (table == null) {
            table = new TableComponent(Using.TABLE.locator, this);
        }
        return table;
    }
    
    /**
     * Get a list of automation components that model the tables collection.
     *  
     * @return list of {@link TableComponent} models
     */
    public List<TableComponent> getTableList() {
        if (tableList == null) {
            tableList = newComponentList(TableComponent.class, Using.TABLE.locator);
        }
        return tableList;
    }
    
    /**
     * Get a map of automation components that model the tables collection.
     * 
     * @return map of {@link TableComponent} models keyed by element identifier
     */
    public Map<Object, TableComponent> getTableMap() {
        if (tableMap == null) {
            tableMap = newComponentMap(TableComponent.class, Using.TABLE.locator);
        }
        return tableMap;
    }
    
    /**
     * Get a list of automation components that model the frames collection.
     * 
     * @return list of {@link FrameComponent} models
     */
    public List<FrameComponent> getFrameList() {
        if (frameList == null) {
            frameList = newFrameList(FrameComponent.class, Using.FRAME.locator);
        }
        return frameList;
    }
    
    /**
     * Get a map of automation components that model the frames collection.
     * 
     * @return map of {@link FrameComponent} models keyed by heading text
     */
    public Map<Object, FrameComponent> getFrameMap() {
        if (frameMap == null) {
            frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
        }
        return frameMap;
    }
    
    /**
     * Get the automation component that models shadow root 'A' via the associated element locator.
     * 
     * @return {@link ShadowRootComponent} model for shadow root 'A'
     */
    public ShadowRootComponent getShadowRootByLocator() {
        if (shadowRootByLocator == null) {
            shadowRootByLocator = new ShadowRootComponent(Using.SHADOW_ROOT_A.locator, this);
        }
        return shadowRootByLocator;
    }
    
    /**
     * Get the automation component that models shadow root 'B' via the associated element reference.
     * 
     * @return {@link ShadowRootComponent} model for shadow root 'B'
     */
    public ShadowRootComponent getShadowRootByElement() {
        if (shadowRootByElement == null) {
            RobustWebElement element = (RobustWebElement) findElement(Using.SHADOW_ROOT_B);
            shadowRootByElement = new ShadowRootComponent(element, this);
        }
        return shadowRootByElement;
    }
    
    /**
     * Get a list of automation components that model the shadow root collection.
     *  
     * @return list of {@link ShadowRootComponent} models
     */
    public List<ShadowRootComponent> getShadowRootList() {
        if (shadowRootList == null) {
            shadowRootList = newComponentList(ShadowRootComponent.class, Using.SHADOW_ROOT.locator);
        }
        return shadowRootList;
    }
    
    /**
     * Get a map of automation components that model the shadow root collection.
     * 
     * @return map of {@link ShadowRootComponent} models keyed by heading text
     */
    public Map<Object, ShadowRootComponent> getShadowRootMap() {
        if (shadowRootMap == null) {
            shadowRootMap = newComponentMap(ShadowRootComponent.class, Using.SHADOW_ROOT.locator);
        }
        return shadowRootMap;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(long expiration) {
        refreshCount++;
        return super.refreshContext(expiration);
    }
    
    /**
     * Get page refresh count.
     * 
     * @return page refresh count
     */
    public int getRefreshCount() {
        return refreshCount;
    }
    
    /**
     * Get CSS locator string for the form input field.
     * 
     * @return input field locator string
     */
    public String getInputLocator() {
        return ByType.cssLocatorFor(Using.INPUT);
    }
    
    /**
     * Set value of the form input field to specified string.
     * 
     * @param value input value string
     * @return {@code true} if field value changed; otherwise {@code false}
     */
    public boolean setInputValue(String value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    /**
     * Set value of the form input field to specified boolean.
     * 
     * @param value input value boolean
     * @return {@code true} if field value changed; otherwise {@code false}
     */
    public boolean setInputValue(boolean value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    /**
     * Get current value of the form input field.
     * 
     * @return input field value
     */
    public String getInputValue() {
        return getDomPropertyOfElement(Using.INPUT, "value");
    }
    
    /**
     * Get CSS locator string for the form check box.
     * 
     * @return check box locator string
     */
    public String getCheckLocator() {
        return ByType.cssLocatorFor(Using.CHECK);
    }
    
    /**
     * Determine if the form check box is checked.
     * 
     * @return {@code true} if box is checked; otherwise {@code false}
     */
    public boolean isBoxChecked() {
        return findElement(Using.CHECK).isSelected();
    }
    
    /**
     * Set value of the form check box to specified boolean.
     * 
     * @param value {@code true} to set 'checked' state; {@code false} to clear it
     * @return {@code true} if 'checked' state changed; otherwise {@code false}
     */
    public boolean setCheckValue(boolean value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    /**
     * Set value of the form check box to specified string.
     * 
     * @param value "true" to set 'checked' state (case insensitive); all other values clear it
     * @return {@code true} if 'checked' state changed; otherwise {@code false}
     */
    public boolean setCheckValue(String value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    /**
     * Reset all values in the form.
     */
    public void resetForm() {
        JsUtility.run(driver, "document.getElementById('form').reset()");
    }
    
    /**
     * Determine if the example optional element is found via the associated CSS locator.
     * 
     * @return {@code true} if optional element if found; otherwise {@code false}
     */
    public boolean hasCssOptional() {
        return findOptional(By.cssSelector(ByType.cssLocatorFor(Using.FORM))).hasReference();
    }
    
    /**
     * Determine if the example optional element is found via the associated XPath locator.
     * 
     * @return {@code true} if optional element if found; otherwise {@code false}
     */
    public boolean hasXpathOptional() {
        return findOptional(By.xpath(ByType.xpathLocatorFor(Using.FORM))).hasReference();
    }
    
    /**
     * Determine if an optional element is found via a bogus locator.
     * 
     * @return this method should always return {@code false}
     */
    public boolean hasBogusOptional() {
        return findOptional(By.tagName("BOGUS")).hasReference();
    }
    
    /**
     * Get the type of the browser modal.
     * 
     * @return {@link ModalType} representing the current modal; {@code null} if modal is not shown
     */
    public ModalType getModalType() {
        return ModalType.fromString(alertHandler.getText());
    }
    
    /**
     * Open the {@link ModalType#ALERT ALERT} modal.
     */
    public void openAlertModal() {
        findElement(Using.ALERT).click();
    }

    /**
     * Open the {@link ModalType#CONFIRM CONFIRM} modal.
     */
    public void openConfirmModal() {
        findElement(Using.CONFIRM).click();
    }

    /**
     * Open the {@link ModalType#PROMPT PROMPT} modal.
     */
    public void openPromptModal() {
        findElement(Using.PROMPT).click();
    }
    
    /**
     * Accept the browser modal.
     * 
     * @return landing page object
     */
    public Page acceptModal() {
        return alertHandler.accept();
    }
    
    /**
     * Send the specified keys to the browser modal and accept it.
     * 
     * @param keys keys to send
     * @return landing page object
     */
    public Page sendKeysAndAccept(String keys) {
        return alertHandler.sendKeysAndAccept(keys);
    }
    
    /**
     * Dismiss the browser modal.
     * 
     * @return landing page object
     */
    public Page dismissModal() {
        return alertHandler.dismiss();
    }
    
    /**
     * Get the modal result text.
     * 
     * @return modal result text
     */
    public String getModalResult() {
        return findElement(Using.RESULT).getText();
    }
    
    /**
     * Open the example tab.
     * 
     * @return tab page object (either {@link TabPageA} or {@link TabPageB})
     */
    public TabPage openTab() {
        findElement(Using.OPEN_TAB).click();
        return new TabPage(driver).setWindowState(WindowState.WILL_OPEN);
    }
    
    /**
     * Set the active Grid hub as the base URI for all relative loads of test pages.
     * 
     * @return {@link URI} for the active Grid hub
     */
    public static URI setHubAsTarget() {
        URI targetUri = null;
        SeleniumConfig config = SeleniumConfig.getConfig();
        try {
            // if running Selenium 4+
            if (config.getVersion() > 3) {
                // ensure example page servlet is running
                ExamplePageLauncher.getLauncher().start();
                // get URI of example page servlet
                targetUri = ExamplePageLauncher.getLauncher().getUrl().toURI();
            } else {
                // get URL of Selenium Grid hub server
                URL hubUrl = config.getSeleniumGrid().getHubServer().getUrl();
                // get base URI of grid hub server
                targetUri = UriUtils.uriForPath(hubUrl);
            }
            config.setTargetUri(targetUri);
        } catch (URISyntaxException | IOException eaten) {
            // nothing to do here
        }
        return targetUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadComplete() {
        if (!isLoaded) {
            isLoaded = true;
            return false;
        }
        return true;
    }
    
    /**
     * This class models the browser alerts of the example page.
     */
    private static class ExampleAlertHandler extends AlertHandler {
        public ExampleAlertHandler(Page parentPage) {
            super(parentPage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ExamplePage accept() {
            return Optional.ofNullable(waitForAlert())
                    .map(alert -> {
                        alert.accept();
                        return new ExamplePage(driver);
                    })
                    .orElse((ExamplePage) parentPage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ExamplePage sendKeysAndAccept(final String keys) {
            return Optional.ofNullable(waitForAlert())
                    .map(alert -> {
                        alert.sendKeys(keys);
                        alert.accept();
                        return new ExamplePage(driver);
                    })
                    .orElse((ExamplePage) parentPage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ExamplePage dismiss() {
            return Optional.ofNullable(waitForAlert())
                    .map(alert -> {
                        alert.dismiss();
                        return new ExamplePage(driver);
                    })
                    .orElse((ExamplePage) parentPage);
        }
    }
}
