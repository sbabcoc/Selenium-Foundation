package com.nordstrom.automation.selenium.examples;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.model.RobustWebElement;

@PageUrl("/grid/admin/ExamplePageServlet")
public class ExamplePage extends Page {

    public static final String TITLE = "Example Page";
    public static final String[] PARAS = {"This is paragraph one.", "This is paragraph two.", "This is paragraph three."};
    public static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
    public static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
    public static final String FRAME_A = "Frame A";
    public static final String FRAME_B = "Frame B";
    public static final String FRAME_C = "Frame C";
    public static final String FRAME_D = "Frame D";
    public static final String TABLE_ID = "t1";
    public static final String SHADOW_DOM_A = "Shadow DOM A";
    public static final String SHADOW_DOM_B = "Shadow DOM B";
    
    public ExamplePage(WebDriver driver) {
        super(driver);
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
    private int refreshCount;
    
    protected static final String FRAME_A_ID = "frame-a";
    protected static final String FRAME_B_ID = "frame-b";
    protected static final String FRAME_C_ID = "frame-c";
    protected static final String FRAME_D_ID = "frame-d";

    protected enum Using implements ByEnum {
        FRAME(By.cssSelector("iframe[id^='frame-']")),
        FRAME_A(By.cssSelector("iframe#frame-a")),
        FRAME_B(By.cssSelector("iframe#frame-b")),
        FRAME_C(By.cssSelector("iframe#frame-c")),
        FRAME_D(By.cssSelector("iframe#frame-d")),
        PARA(By.cssSelector("p[id^='para-']")),
        TABLE(By.cssSelector("table#t1")),
        FORM(By.tagName("form")),
        INPUT(By.cssSelector("input#input-field")),
        CHECK(By.cssSelector("input#checkbox")),
        SHADOW_ROOT(By.cssSelector("div[id^='shadow-root-']")),
        SHADOW_ROOT_A(By.cssSelector("div#shadow-root-a")),
        SHADOW_ROOT_B(By.cssSelector("div#shadow-root-b")),
        FORM_DIV(By.cssSelector("div#form-div"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public FrameComponent getFrameByLocator() {
        if (frameByLocator == null) {
            frameByLocator = new FrameComponent(Using.FRAME_A.locator, this);
        }
        return frameByLocator;
    }
    
    public FrameComponent getFrameByElement() {
        if (frameByElement == null) {
            RobustWebElement element = (RobustWebElement) findElement(Using.FRAME_B);
            frameByElement = new FrameComponent(element, this);
        }
        return frameByElement;
    }
    
    public FrameComponent getFrameByIndex() {
        if (frameByIndex == null) {
            frameByIndex = new FrameComponent(2, this);
        }
        return frameByIndex;
    }
    
    public FrameComponent getFrameById() {
        if (frameById == null) {
            frameById = new FrameComponent(FRAME_D_ID, this);
        }
        return frameById;
    }
    
    public List<String> getParagraphs() {
        List<WebElement> paraList = findElements(Using.PARA);
        return Arrays.asList(paraList.get(0).getText(), paraList.get(1).getText(), paraList.get(2).getText());
    }
    
    public FormComponent getForm() {
        if (form == null) {
            form = new FormComponent(Using.FORM_DIV.locator, this);
        }
        return form;
    }
    
    public TableComponent getTable() {
        if (table == null) {
            table = new TableComponent(Using.TABLE.locator, this);
        }
        return table;
    }
    
    public List<TableComponent> getTableList() {
        if (tableList == null) {
            tableList = newComponentList(TableComponent.class, Using.TABLE.locator);
        }
        return tableList;
    }
    
    public Map<Object, TableComponent> getTableMap() {
        if (tableMap == null) {
            tableMap = newComponentMap(TableComponent.class, Using.TABLE.locator);
        }
        return tableMap;
    }
    
    public List<FrameComponent> getFrameList() {
        if (frameList == null) {
            frameList = newFrameList(FrameComponent.class, Using.FRAME.locator);
        }
        return frameList;
    }
    
    public Map<Object, FrameComponent> getFrameMap() {
        if (frameMap == null) {
            frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
        }
        return frameMap;
    }
    
    public ShadowRootComponent getShadowRootByLocator() {
        if (shadowRootByLocator == null) {
            shadowRootByLocator = new ShadowRootComponent(Using.SHADOW_ROOT_A.locator, this);
        }
        return shadowRootByLocator;
    }
    
    public ShadowRootComponent getShadowRootByElement() {
        if (shadowRootByElement == null) {
            RobustWebElement element = (RobustWebElement) findElement(Using.SHADOW_ROOT_B);
            shadowRootByElement = new ShadowRootComponent(element, this);
        }
        return shadowRootByElement;
    }
    
    public List<ShadowRootComponent> getShadowRootList() {
        if (shadowRootList == null) {
            shadowRootList = newComponentList(ShadowRootComponent.class, Using.SHADOW_ROOT.locator);
        }
        return shadowRootList;
    }
    
    public Map<Object, ShadowRootComponent> getShadowRootMap() {
        if (shadowRootMap == null) {
            shadowRootMap = newComponentMap(ShadowRootComponent.class, Using.SHADOW_ROOT.locator);
        }
        return shadowRootMap;
    }
    
    @Override
    public SearchContext refreshContext(long expiration) {
        refreshCount++;
        return super.refreshContext(expiration);
    }
    
    public int getRefreshCount() {
        return refreshCount;
    }
    
    public String getInputLocator() {
        return ByType.cssLocatorFor(Using.INPUT);
    }
    
    public boolean setInputValue(String value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    public boolean setInputValue(boolean value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    public String getInputValue() {
        return findElement(Using.INPUT).getAttribute("value");
    }
    
    public String getCheckLocator() {
        return ByType.cssLocatorFor(Using.CHECK);
    }
    
    public boolean isBoxChecked() {
        return findElement(Using.CHECK).isSelected();
    }
    
    public boolean setCheckValue(boolean value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    public boolean setCheckValue(String value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    public void resetForm() {
        JsUtility.run(driver, "document.getElementById('form').reset()");
    }
    
    public boolean hasCssOptional() {
        return findOptional(By.cssSelector(ByType.cssLocatorFor(Using.FORM))).hasReference();
    }
    
    public boolean hasXpathOptional() {
        return findOptional(By.xpath(ByType.xpathLocatorFor(Using.FORM))).hasReference();
    }
    
    public boolean hasBogusOptional() {
        return findOptional(By.tagName("BOGUS")).hasReference();
    }

    public static URI setHubAsTarget() {
        URI targetUri = null;
        SeleniumConfig config = SeleniumConfig.getConfig();
        URL hubUrl = config.getSeleniumGrid().getHubServer().getUrl();
        try {
            targetUri = new URIBuilder()
                    .setScheme(hubUrl.getProtocol())
                    .setHost(hubUrl.getHost())
                    .setPort(hubUrl.getPort())
                    .build().normalize();
            config.setTargetUri(targetUri);
        } catch (URISyntaxException eaten) {
            // nothing to do here
        }
        return targetUri;
    }
    
}
