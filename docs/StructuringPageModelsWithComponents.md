# Introduction

By applying the page-model pattern, you can produce a cohesive, behavior-based API that centralizes the nuts and bolts of interacting with the target application in your page classes. When the application under test changes (and it will), your tests will typically be unaffected; all of the updates will be isolated to the page classes.

However, modeling an application based solely on its pages produces a very flat model. It's quite common for a web application page to contain groups of elements that are logically associated (e.g. - billing address on an order information page). It's also common to encounter pages with multiple occurrences of an element grouping (e.g. - item tiles on a search results page). Factoring these grouping out into **page components** can greatly enrich your models, presenting a conceptual framework that automation developers will recognize.

If your target application uses frames to structure its content, you will be amazed at the ease with which your models interact with them. With automatic driver targeting, **Selenium Foundation** entirely removes explicit context switching from your implementation, allowing you to focus on functionality instead. More on this later.

###### Page Component and Frame Map from  [ExamplePage.java](example/ExamplePage.md)
```java
...
private TableComponent table;
private Map<Object, FrameComponent> frameMap;
...
public TableComponent getTable() {
	if (table == null) {
		table = new TableComponent(Using.TABLE.locator, this);
	}
	return table;
}
...
public Map<Object, FrameComponent> getFrameMap() {
	if (frameMap == null) {
		frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
	}
	return frameMap;
}
...
```

In the preceding sample code, extracted from the **Selenium Foundation** unit tests, we see that subsets of page functionality have been factored out into two components - [TableComponent](example/TableComponent.md) and [FrameComponent](example/FrameComponent.md). The web application content modeled by this code contains one table and three frames, which are represented by the model as a table component and a mapped collection of frame components.

# Page Component Search Contexts

In the **Selenium WebDriver** API, a search context is defined by the range of elements that will be examined when searching for a specified locator. For a page object, the search context is the entire page. For a page component, the search context is all of the elements within the bounds of the component's container element. For a frame, the search context is all of the elements within the bounds of the frame element.

The search context of a page component encompasses a subset of the elements of the context(s) in which it is contained. In other words, elements within the bounds of the component can be found by searches in encompassing contexts. However, the same cannot be said for frames, because...

## Frames define distinct search contexts

The preceding descriptions of search contexts omits one important detail - frames define distinct search contexts. Elements within the bounds of a frame **cannot** be found by searches in encompassing contexts. In the browser, frames are handled as separate documents. From a conceptual standpoint, a frame **IS-A** page, and the <span style="color:blue">Frame</span> class of **Selenium Foundation** models this concept by extending the <span style="color:blue">Page</span> class.

The search context of a frame is completely isolated, and the driver target needs to be switched to this context to interact with it. Without **Selenium Foundation**, the task of frame driver targeting can be frustrating and confusing. Once you've modeled a frame as a <span style="color:blue">Frame</span> object, **Selenium Foundation** handles driver targeting for you automatically. More on this below.

## A word about XPath locators

XPath locators differ from all other locator types in that they can traverse outside the scope of the target search context. For page and frame containers, this is purely academic, as their search contexts automatically encompass the entire document. For page component, however, you need to make sure you write your XPath expressions so they only evaluate element within the bounds of the page component search context.

Here are some examples of XPath expressions that select elements _within_ the bounds of the page component search context:

* `para` selects **para** element children of the context node
* `*` selects all element children of the context node
* `para[1]` selects the first **para** child of the context node
* `para[last()]` selects the last **para** child of the context node
* `*/para` selects all **para** grandchildren of the context node
* `chapter//para` selects the **para** element descendants of the **chapter** element children of the context node
* `.` selects the context node
* `.//para` selects the **para** element descendants of the context node

This last expression demonstrates the form that most context-relative XPath locators should use.

Because of their ability to traverse the element hierarchy vertically and horizontally, a subset of location-related XPath tokens and operators will produce expressions that can exceed the search context. Here are the tokens and operators to avoid or use cautiously:

* Expressions that begin with `/`, `//`, or `..`
* Ancestor or sibling axes: `parent`, `following-sibling`, `preceding-sibling`, `following`, `predecing`, `ancestor`, `ancestor-or-self`

Here are some examples of XPath expressions that select elements _outside_ the bounds of the page component search context:

* `//para` selects all **para** elements in the document that contains the context node
* `../para` selects **para** element children of the parent of the context node
* `parent::*` selects the parent of the context node
* `ancestor::div` selects all ancestor **div** elements of the context node
* `following-sibling::*` selects all siblings after the context node
* `previous::chapter` selects all **chapter** elements that appear before the context node in the document, except ancestors

Avoid expressions that start with either `/` or `//`, as these always traverse the entire document. There are legitimate applications for the other tokens, but you must exercise great care to avoid traversing outside the bounds of the page component search context.

# Driver Focus with Frame-Based Components

In traditional **Selenium WebDriver** automation, the task of working with frames is often difficult and confusing. You're forced to include ubiquitous boilerplate code to switch driver focus between the frames you need to interact with and the main page that contains them.

With **Selenium Foundation**, the task of managing driver focus is handled for you automatically. The boilerplate code is entirely eliminated, allowing you to focus on modeling the behaviors of your application instead of the plumbing that connects your code to the browser.

The following example demonstrates automatic driver targeting. Note that neither the test nor the frame-based component includes any code to switch the driver context to the frame. **Selenium Foundation** automatically switches the driver's focus to the frame context when the **`getPageContent()`** method is called.

###### Driver Targeting Demonstration from [ExampleTest.java](example/ExampleTest.md)
```java
...
private static final String FRAME_A = "Frame A";

@Test
public void testFrameByElement() {
	ExamplePage page = getPage();
	FrameComponent component = page.getFrameByElement();
	assertEquals(component.getPageContent(), FRAME_A);
}
...
```

# Component Nesting and Aggregation

When modeling a web application, it's often useful to represent groups of associated elements as **page components**. It's quite common for a page component to be composed of one or more sub-components (e.g. - shipping address and delivery method in a shipping information section). You'll also encounter pages that contain multiple instances of particular component (e.g. - item tiles on a search results page).

You're also likely to encounter web applications that use **frames** to aggregate multiple documents into a single page. With **Selenium Foundation**, interactions with frame-based components are essentially identical to interactions with more conventional element-based components.

Each component retains a hierarchical association with the component that created it - the parent container. This hierarchy defines a sequence of nested search contexts, each represented by a page component, with the parent page as the outermost search context.

For scenarios with collections of the same component, **Selenium Foundation** provides the ability to aggregate these as either ordered lists or keyed maps. **Selenium Foundation** component collections employ a lazy-initialization strategy, allocating slots for the items in the collection, but deferring instantiation of the components themselves until they're accessed. More on this later.

The following example demonstrates a [table component](example/TableComponent.md) that includes a [table row component](example/TableRowComponent.md) for the headings and a list of [table row component](example/TableRowComponent.md)s for the data rows.

###### Nested Component and List Aggregation from [TableComponent.java](example/TableComponent.md)
```java
...
private TableRowComponent tableHdr;
private List<TableRowComponent> tableRows;

private TableRowComponent getTableHdr() {
	if (tableHdr == null) {
		tableHdr = new TableRowComponent(Using.HDR_ROW.locator, this);
	}
	return tableHdr;
}

private List<TableRowComponent> getTableRows() {
	if (tableRows == null) {
		tableRows = new ComponentList<>(this, TableRowComponent.class, Using.TBL_ROW.locator);
	}
	return tableRows;
}
...
```

The following example demonstrates a [page](example/ExamplePage.md) that includes a keyed map of [frame component](example/FrameComponent.md)s. The keys are supplied by a static **`getKey()`** method declared by the component itself. More details of this method can be found in the next section. 

###### Frame Map Aggregation from [ExamplePage.java](example/ExamplePage.md)
```java
...
private Map<Object, FrameComponent> frameMap;

public Map<Object, FrameComponent> getFrameMap() {
	if (frameMap == null) {
		frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
	}
	return frameMap;
}
...
```

# Component Collections (Lists and Maps)

As shown in the previous section, **Selenium Foundation** provides the ability to collect groups of page components into either ordered lists or keyed maps. The collection types provided by **Selenium Foundation** implement the standard [List](https://docs.oracle.com/javase/8/docs/api/java/util/List.html) and [Map](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html) interfaces, making them suitable for any scenario that takes a list or a map as input.

#### Required constructor for collectible components

To be grouped into a component collection (either list or map), page components and frames must declare a constructor with signature:

```java
public <component-classname>(RobustWebElement element, ComponentContainer parent)
```

This constructor is required to enable lazy initialization of the items in the collection. More details on this in the next section.

#### Required method for mappable components

In addition, to be grouped as a component map, page components and frames must declare a method with signature:

```java
public static Object getKey(SearchContext context)
```

This method is required to supply the keys that uniquely identify each item in the map.

Full examples of both of these required elements can be seen in [TableComponent.java](example/TableComponent.md), [TableRowComponent.java](example/TableRowComponent.md), and [FrameComponent.java](example/FrameComponent.md).

#### Search context for frame map item keys

Switching the driver focus to a frame context is an expensive process, so **Selenium Foundation** doesn't do this automatically when creating the skeleton of a frame map. If you can derive a unique key from each frame's container element, this is strongly recommended. If you need to access the content of the frame to generate its key, your code needs to switch to the frame's context, then back to the parent frame:

###### Producing map keys with frame content (from [FrameComponent.java](example/FrameComponent.md))
```java
...
	public static Object getKey(SearchContext context) {
		RobustWebElement element = (RobustWebElement) context;
		WebDriver driver = element.getWrappedDriver().switchTo().frame(element);
		Object key = driver.findElement(Using.HEADING.selector).getText();
		driver.switchTo().parentFrame();
		return key;
	}
...
```

Note that the search context passed into the **`getKey()`** method is, in fact, a <span style="color:blue">RobustWebElement</span> object. This example implementation switches the driver to the frame context, extracts unique text from a heading element, and switches the driver back to the parent frame. This last step is **_critical_**, because leaving the driver focused on the frame context is likely to cause downstream failures.

## Lazy initialization of Component Collections

As indicated previously, **Selenium Foundation** component collections employ a lazy-initialization strategy, allocating slots for the items in the collection, but deferring instantiation of the components themselves until they're accessed. This strategy provides provides an enormous performance benefit by eliminating unnecessary interactions with the browser.

When a component collection is initially created, the only details **Selenium Foundation** captures about the actual content behind each item in the collection is the component's search context - its container element. When an item is accessed for the first time, **Selenium Foundation** uses the container element and parent search context to create the corresponding instance of the component, which is why collectible components are required to declare the specific constructor described in the previous section.

## Immutability of Component Collections

Component lists and maps are immutable. These collections are derived from the content of the web application page they represent. Allowing the composition of these collections to be altered by adding, removing, or replacing items would break the one-to-one relationship between the actual page content and the model evinced by the collection.