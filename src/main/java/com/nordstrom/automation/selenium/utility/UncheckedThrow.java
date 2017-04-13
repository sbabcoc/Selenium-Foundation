package com.nordstrom.automation.selenium.utility;

/**
 * This utility class uses type erasure to enable client code to throw checked exceptions as unchecked.
 * This allows methods to throw checked exceptions without requiring clients to handle or declare them.
 * It should be sparingly, as this exempts client code from handling or declaring exceptions created by
 * their own actions. The target use case for this facility is to throw exceptions that were serialized
 * in responses from a remote system. Although the compiler won't require clients of methods using this
 * technique to handle or declare the suppressed exception, the JavaDoc for such methods should include
 * a {@code @throws} declaration for implementers who might want to handle or declare it voluntarily.
 */
public final class UncheckedThrow {
	
	private UncheckedThrow() {
		throw new AssertionError("UncheckedThrow is a static utility class that cannot be instantiated");
	}
	
	/**
	 * This method throws the specified checked exception, using generic type erasure to enable client
	 * methods to propagate checked exceptions without being required to declare them.
	 * 
	 * @param thrown exception to be thrown
	 */
    public static RuntimeException throwUnchecked(final Throwable thrown) {
        UncheckedThrow.<RuntimeException>propagate(thrown);
        // suppress complaint about missing return value
        throw new AssertionError("This is unreachable");
    }
    
    /**
     * Throw the specified checked exception, using generic type erasure to bypass the requirement to
     * declare the exception in the signature of the calling method.
     * 
     * @param thrown exception to be thrown
     * @throws T dummy declaration to satisfy the compiler
     */
    @SuppressWarnings("unchecked")
	private static <T extends Exception> void propagate(Throwable thrown) throws T {
        // Due to generic type erasure, this cast only serves to satisfy the compiler
    	// that the requirement to declare the thrown exception has been met.
        throw (T) thrown;
    }
}