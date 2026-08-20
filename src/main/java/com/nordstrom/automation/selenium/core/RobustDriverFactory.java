package com.nordstrom.automation.selenium.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.RobustElementFactory;

/**
 * This class builds drivers that automatically wrap every {@link WebElement} found in the result of
 * {@code executeScript}/{@code executeAsyncScript} - however deeply nested inside {@link List}s and
 * {@link Map}s - in a reference-refreshing shell, without requiring the caller to opt in.
 * <p>
 * Deliberately scoped to only these two methods. {@code findElement}/{@code findElements} - on the driver,
 * or on any element found through it, however many levels deep - pass straight through untouched and
 * continue to use the existing, separate, locator-based wrapping mechanism (see
 * {@code RobustWebElement#findElement}/{@code RobustElementFactory#getElement}). That separation isn't
 * incidental: the existing mechanism already ties a refreshed reference back to the specific
 * {@code WrapsContext} Java object that found it, which a wrapper operating below the Java object model
 * (at the wire-command level) has no way to reconstruct. Blending the two would silently discard the
 * correct, already-working locator-based refresh in favor of a script-replay-based one, since
 * {@code RobustElementWrapper}'s constructor collapses onto whichever wrap was applied first when it
 * receives an element that's already wrapped.
 * <p>
 * Implemented as a {@link Proxy} across every interface the real driver implements, rather than as a
 * subclass of the driver's concrete class. Driver constructors are commonly side-effecting - launching a
 * browser process, negotiating a session - so reconstructing one via a generated subclass (the same
 * approach {@code RobustElementFactory} uses for elements, which are cheap and side-effect-free to
 * reconstruct) risks spawning a second, unwanted session. A {@link Proxy} wraps the already-live driver
 * object without invoking its constructor at all.
 * <p>
 * The tradeoff: {@code instanceof SomeConcreteDriverClass} checks against a wrapped driver will fail, since
 * a {@link Proxy} only ever implements interfaces, never extends a concrete class. Code that needs to reach
 * a vendor-specific concrete type isn't supported by this class as written; if that turns out to matter in
 * practice, a subclassing-based approach becomes viable wherever the driver exposes a side-effect-free
 * reattachment constructor (e.g. {@code RemoteWebDriver(CommandExecutor, Capabilities)}, which binds to an
 * already-running session rather than starting a new one).
 */
public final class RobustDriverFactory {

    private RobustDriverFactory() {
        throw new AssertionError("RobustDriverFactory is a static utility class that cannot be instantiated");
    }

    /**
     * Wrap the specified driver so that every {@code executeScript}/{@code executeAsyncScript} result comes
     * back with its embedded elements automatically wrapped in a reference-refreshing shell.
     * <p>
     * Idempotent - wrapping an already-wrapped driver returns it unchanged, rather than nesting proxies.
     * 
     * @param driver driver to wrap
     * @return wrapped driver
     */
    public static WebDriver wrapDriver(final WebDriver driver) {
        if (isWrapped(driver)) {
            return driver;
        }

        Class<?> driverClass = driver.getClass();
        Class<?>[] interfaces = getAllInterfaces(driverClass).toArray(new Class<?>[0]);

        return (WebDriver) Proxy.newProxyInstance(
                driverClass.getClassLoader(), interfaces, new RobustDriverInvocationHandler(driver));
    }

    /**
     * Get the real driver underlying the specified driver, unwrapping it if it's a proxy built by
     * {@link #wrapDriver}.
     * <p>
     * For code that genuinely needs the concrete driver instance - e.g. an {@code instanceof
     * RemoteWebDriver} check or cast, or anything reaching into driver-specific behavior a {@link Proxy}
     * can't expose (see the class javadoc's note on {@code instanceof} checks) - rather than one that just
     * wants to invoke ordinary {@link WebDriver}/{@link org.openqa.selenium.JavascriptExecutor} methods,
     * for which the wrapped driver works fine and is almost always what should be used instead, to get the
     * automatic wrapping this class exists to provide.
     * 
     * @param driver driver to unwrap - may or may not actually be wrapped
     * @return the real driver, if {@code driver} was wrapped; {@code driver} itself otherwise
     */
    public static WebDriver unwrap(final WebDriver driver) {
        if (isWrapped(driver)) {
            return ((RobustDriverInvocationHandler) Proxy.getInvocationHandler(driver)).driver;
        }
        return driver;
    }

    /**
     * Determine whether the specified driver is a proxy built by {@link #wrapDriver}.
     * 
     * @param driver driver to check
     * @return {@code true} if {@code driver} is a proxy built by this class
     */
    private static boolean isWrapped(final WebDriver driver) {
        return Proxy.isProxyClass(driver.getClass())
                && Proxy.getInvocationHandler(driver) instanceof RobustDriverInvocationHandler;
    }

    /**
     * Collect every interface implemented by the specified class, including those inherited from
     * superclasses and superinterfaces.
     * 
     * @param clazz class to inspect
     * @return every interface {@code clazz} implements, directly or indirectly
     */
    private static Set<Class<?>> getAllInterfaces(final Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            for (Class<?> iface : current.getInterfaces()) {
                interfaces.add(iface);
                interfaces.addAll(getAllInterfaces(iface));
            }
        }
        return interfaces;
    }

    /**
     * Invocation handler that delegates every call to the real driver, except {@code executeScript}/
     * {@code executeAsyncScript}, whose results are wrapped before being returned to the caller.
     */
    private static final class RobustDriverInvocationHandler implements InvocationHandler {

        private final WebDriver driver;
        private final WrapsContext context;

        private RobustDriverInvocationHandler(final WebDriver driver) {
            this.driver = driver;
            this.context = new DriverContext(driver);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                Object result = method.invoke(driver, args);
                String name = method.getName();
                if (("executeScript".equals(name) || "executeAsyncScript".equals(name))
                        && args != null && args.length > 0 && args[0] instanceof String) {
                    // args[1], if present, is the Object[] backing executeScript's own varargs parameter -
                    // reflection represents a varargs call as one trailing array, not spread individually
                    Object[] scriptArgs = (args.length > 1 && args[1] instanceof Object[])
                            ? (Object[]) args[1] : new Object[0];
                    return wrap(result, (String) args[0], scriptArgs, "", new boolean[1]);
                }
                return result;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        /**
         * Recursively walk the specified value, replacing every {@link WebElement} found with a
         * reference-refreshing wrapper carrying its accessor path, and leaving every other value unchanged.
         * <p>
         * If nothing inside {@code value} needed wrapping - no {@link WebElement} anywhere within it, at
         * any depth - the original {@code value} is returned untouched rather than a reconstructed
         * copy, so a script result that's entirely non-element data (a plain map, a list of strings, a
         * legacy pre-W3C shadow-root JSON blob) always flows back exactly as the underlying driver
         * produced it, with no risk of a different container identity or implementation type mattering to
         * a caller downstream.
         * 
         * @param value value to walk (may be an element, a list, a map, or any other value)
         * @param script the script whose result is being walked - reused verbatim by each wrapped
         *      element's refresh script, with {@code path} appended
         * @param scriptArgs arguments {@code script} was invoked with - replayed verbatim by each wrapped
         *      element's refresh script, not just used for this initial invocation
         * @param path JS accessor path locating {@code value} within {@code script}'s result
         * @param changed single-element output flag - set to {@code true} if {@code value} itself (or
         *      anything nested inside it) was replaced with a wrapped element; left untouched otherwise
         * @return {@code value} with every embedded element wrapped, or {@code value} itself unchanged if
         *      nothing inside it needed wrapping
         */
        private Object wrap(
                final Object value, final String script, final Object[] scriptArgs, final String path,
                final boolean[] changed) {

            if (value instanceof WebElement) {
                changed[0] = true;
                return RobustElementFactory.makeRobustElement(
                        (WebElement) value, context, script, path, scriptArgs);
            }
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                List<Object> out = new ArrayList<>(list.size());
                boolean[] innerChanged = new boolean[1];
                for (int i = 0; i < list.size(); i++) {
                    out.add(wrap(list.get(i), script, scriptArgs, path + "[" + i + "]", innerChanged));
                }
                if (!innerChanged[0]) {
                    return value;
                }
                changed[0] = true;
                return out;
            }
            if (value instanceof Map<?, ?>) {
                Map<Object, Object> out = new LinkedHashMap<>();
                boolean[] innerChanged = new boolean[1];
                ((Map<?, ?>) value).forEach((k, v) -> out.put(
                        k, wrap(v, script, scriptArgs, path + "[" + quoteKey(k) + "]", innerChanged)));
                if (!innerChanged[0]) {
                    return value;
                }
                changed[0] = true;
                return out;
            }
            return value;
        }

        /**
         * Render the specified map key as a single-quoted JavaScript string literal, escaping embedded
         * backslashes and quotes, for use as a bracket-accessor segment in a generated script.
         * 
         * @param key map key to render
         * @return single-quoted JavaScript string literal
         */
        private static String quoteKey(final Object key) {
            return "'" + String.valueOf(key).replace("\\", "\\\\").replace("'", "\\'") + "'";
        }
    }
}
