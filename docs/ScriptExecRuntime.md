
The `scriptExecRuntime` wrapper is a  **self-contained JavaScript execution runtime**  designed to sit inside a browser (or WebView) and provide  **structured, Selenium/Appium-friendly execution semantics**. Its features line up closely with the needs of  `executeScript`  /  `executeAsyncScript`  when Java is on the other side.

Below is a structured description of what it provides and  _why_  each piece exists.

----------

## 1. Idempotent, safe runtime injection

**Feature**

-   Guards against multiple installations:
    
    ```js
    if (root.__wdRuntime) return;
    ```

**Why it matters**

-   Selenium/Appium often injects helper scripts repeatedly.
-   This ensures:
    -   No redefinition warnings
    -   No loss of state
    -   Deterministic behavior across executions

----------

## 2. Namespaced global API (non-polluting)

**Feature**

-   Minimal global footprint:
    -   `__wdRuntime.runSync`
    -   `__wdRuntime.runAsync`
    -   `__wd.fail`

**Implementation**

-   Custom  `namespace()`  helper safely creates nested objects.
-   Functions are registered by name rather than assigned anonymously.

**Why it matters**

-   Avoids collisions with page JavaScript
-   Keeps all Selenium infrastructure under a predictable prefix
-   Compatible with older browsers (no  `Object.assign`, no modules)

----------

## 3. Unified execution model (sync + async)

### Synchronous execution

```js
__wdRuntime.runSync(fn, ...args)
```

**Behavior**

-   Executes user code immediately
-   Returns a structured envelope:
    
    ```json
    { "status": "ok", "value": ... }
    ```
    
    or
    
    ```json
    { "status": "error", "exception": {...} }
    ```

**Why**

-   Mirrors  `executeScript`
-   Avoids uncaught JS exceptions leaking into WebDriver internals

----------

### Asynchronous execution

```js
__wdRuntime.runAsync(fn, callback, timeoutMs, ...args)
```

**Behavior**

-   Supports:
    -   Promise-returning functions
    -   Explicit async logic
-   Enforces  **single completion**
-   Enforces  **timeout**

**Why**

-   Matches Selenium’s  `executeAsyncScript`
-   Prevents:
    -   Double-callback bugs
    -   Hung scripts
    -   Silent promise rejections

----------

## 4. Completion controller with timeout enforcement

**Feature**

-   `createCompletion()`  guarantees:
    -   Callback invoked once
    -   Timeout auto-failure

```js
Script did not complete within 30000 ms
```

**Why**

-   Browsers do not guarantee async script completion
-   Selenium requires  _exactly one_  callback
-   Prevents grid/node deadlocks

----------

## 5. Promise-aware execution

**Feature**

-   Detects Promises via duck typing:
    
    ```js
    value && typeof value.then === 'function'
    ```
    

**Behavior**

-   `then → ok`
-   `catch → error envelope`

**Why**

-   Enables modern async JavaScript without requiring callbacks
-   Works across:
    -   Native Promises
    -   WebKit / Safari
    -   Legacy Selenium nodes

----------

## 6. Structured error envelopes (Java-compatible)

**Feature**

-   All failures are normalized into a Java-style exception structure:
    
    ```json
    {
      "status": "error",
      "exception": {
        "className": "java.lang.RuntimeException",
        "message": "...",
        "stack": "..."
      }
    }
    ```

**Why**

-   Java clients expect:
    -   `className`
    -   `message`
    -   stack traces
-   Enables lossless propagation from JS → Java

----------

## 7. Explicit user-controlled failures (`__wd.fail`)

**Feature**

```js
__wd.fail("java.lang.IllegalStateException", "Something went wrong");
```

**Behavior**

-   Throws a  **tagged internal exception**
-   Caught and translated into a Java exception envelope
-   Preserves JS stack trace

**Why**

-   Allows test authors to:
    -   Fail intentionally
    -   Map JS failures to specific Java exception types
-   Avoids abusing  `throw new Error(...)`

----------

## 8. Internal vs user failure distinction

**Feature**

-   Tagged exception marker:
    
    ```js
    __wdUserFailure: true
    ```

**Behavior**

-   User-triggered failures are handled differently from runtime errors
-   Prevents user errors from being wrapped as generic JS runtime failures

**Why**

-   Enables semantic failures:
    -   Assertion-like behavior
    -   Controlled test aborts
-   Keeps infrastructure errors distinct

----------

## 9. Stack trace capture (portable)

**Feature**

-   Manual stack capture via:
    
    ```js
    throw new Error();
    ```

**Why**

-   Some WebDriver environments:
    -   Drop stack traces
    -   Normalize exceptions poorly
-   This guarantees stack availability when possible

----------

## 10. Defensive, legacy-compatible design

**Notable constraints**

-   No ES6 syntax
-   No modules
-   No arrow functions
-   No  `async/await`

**Why**

-   Runs in:
    -   Safari iOS
    -   Embedded WebViews
    -   Older Selenium Grid 3 nodes
-   Matches the lowest common denominator

----------

## 11. Argument forwarding support

**Feature**

-   Both sync and async variants forward extra arguments:
    
    ```js
    fn.apply(null, args)
    ```

**Why**

-   Aligns with Selenium’s ability to pass arguments into scripts
-   Avoids closure hacks

----------

## Summary

This runtime wrapper provides:

-   ✅ Idempotent injection
-   ✅ Safe global namespacing
-   ✅ Unified sync/async execution
-   ✅ Promise support
-   ✅ Timeout enforcement
-   ✅ Java-compatible error envelopes
-   ✅ Explicit, typed user failures
-   ✅ Stack trace preservation
-   ✅ Selenium 3 / Grid / Safari compatibility

In short, it acts as a  **mini execution kernel**  that bridges JavaScript and Java cleanly, predictably, and defensively—exactly what Selenium/Appium scripting needs in hostile browser environments.

> Written with [StackEdit](https://stackedit.io/).
