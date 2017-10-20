package com.nordstrom.automation.selenium.support;

import com.google.common.base.Function;

import org.openqa.selenium.SearchContext;

/**
 * Models a condition that might reasonably be expected to eventually evaluate to something that is
 * neither null nor false. Examples would include determining if a web page has loaded or that an
 * element is visible.
 * <p>
 * Note that implementations of the Coordinator interface are expected to be idempotent. They will 
 * be called in a loop by {@link SearchContextWait} and any modification of the state of the application
 * under test may have unexpected side-effects.
 * 
 * @param <T> The return type
 */
public interface Coordinator<T> extends Function<SearchContext, T>{}
