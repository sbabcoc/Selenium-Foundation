
# Introduction

Browser modals (a.k.a. - alerts) are a standard feature of web applications, but working with alerts via the standard **Selenium** API is a bit odd. These application entities are not search contexts, and yet the API requires "switching" to an alert to interact with it. Even the process of determining whether an alert is present is clunky. Try to switch to the alert, and **NoAlertPresentException** is thrown if no alert is present. To make interactions with alerts more rational, **Selenium Foundation** provides the **AlertHandler** class.

# Using the AlertHandler Class

To incorporate alert handling into your web application page collection, declare extensions of **AlertHandler** as private nested classes of page models that trigger alerts, providing implementations of the base class abstract methods that return instances of the correct landing page classes. Alert handler instances are not exposed to clients directly but are service objects of the parent page object. Alert-targeted actions are accessed through page-class methods, which provides a solid link between the page object and its associated browser modals.

###### Alert handler
```java
public  class ExamplePage extends Page implements DetectsLoadCompletion {
    
    ...
    
    public ExamplePage(WebDriver driver) {
        super(driver);
        alertHandler = new ExampleAlertHandler(this);
    }
    
    private final AlertHandler alertHandler;
    private boolean isLoaded;
    
    ...
    
    /**
     * Accept the browser modal.
     * <p>
     * <b>NOTE</b>: Providing access to the alert handler 'accept' function through this
     * page object method enables page transition synchronization and page object life
     * cycle management.
     * 
     * @return landing page object
     */
    public Page acceptModal() {
        return alertHandler.accept();
    }
    
    ...
    
    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b>: This method belongs to the {@link DetectsLoadCompletion} interface.
     * Page classes that will be returned by alert-targeted functions should implement
     * this interface, because the DOM cannot be accessed while an alert is shown. This
     * precludes using the default synchronization mechanism that acquires a reference
     * element and waits for it to go stale.
     * <p>
     * The implementation shown here is a contrived example. In a real page class, the
     * implementation would check for verifiable evidence that loading of the page was
     * actually complete (e.g. - dynamic content loading is done).
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
        
        ...
    }
}
```

The preceeding example shows how to incorporate **AlertHandler** into a page class that models a page with associated browser modals. Pay attention to the notes in the code comments, as these provide important information to keep in mind when creating your own implementation. To maintain solid synchronization between your automation and the system under test, it's crucial to implement the **DetectsLoadCompletion** interface in landing page classes, instances of which are returned by alert methods like **`accept`**. The example above only shows a definition for the **`accept`** function, but you'll need to implement all three of the abstract methods declared by **AlertHandler**, even if the page being modeled doesn't require the associated function. For unneeded methods, a simple solution is just to throw **UnsupportedOperationException** with a meaningful message. 

Here are some of the advantages of using **AlertHandler** in your page model classes:
* The **AlertHandler** base class switches the driver to the alert for you, removing this concern from your own implementation.
* Routing calls to alert functions through page object methods maintains control of transition synchronization and page object life cycle.
* Your code never has to deal with **NoAlertPresentException** just to check for the presence of an alert or to wait for one to appear.



> Written with [StackEdit](https://stackedit.io/).
