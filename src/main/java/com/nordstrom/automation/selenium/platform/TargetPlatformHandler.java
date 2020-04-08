package com.nordstrom.automation.selenium.platform;

/** This class contains support methods used by our target platform implementation. */
public class TargetPlatformHandler {
    
    /**
     * Resolve the target platform constant for the associated method.
     * 
     * @param testObject test class object
     * @param targetPlatform {@link TargetPlatform} annotation for the current method (may be 'null')
     * @return target platform constant; 'null' if test class object is not {@link PlatformTargetable}
     */
    @SuppressWarnings("unchecked")
    public static <P extends Enum<?> & PlatformEnum> P resolveTargetPlatform(Object testObject, TargetPlatform targetPlatform) {
        P platform = null;
        
        if (testObject instanceof PlatformTargetable) {
            PlatformTargetable<P> targetable = (PlatformTargetable<P>) testObject;
            
            if (targetPlatform == null) {
                platform = targetable.getDefaultPlatform();
            } else {
                platform = targetable.platformFromString(targetPlatform.value());
            }
        }
        
        return platform;
    }
    
	/**
	 * Determine if the associated method should run on the specified target platform.
	 * <p>
	 * <b>NOTE</b>: The method is runnable if the test class implements {@link PlatformTargetable} and the method
	 * supports the specified target platform. If the method has no {@link TargetPlatform @TargetPlatform} annotation,
	 * it is assumed to support the implementation-defined 'default' platform. If the test class doesn't implement
	 * <b>PlatformTargetable</b>, the method is runnable if the CONTEXT_PLATFORM setting matches the specified target
	 * platform.
	 * 
	 * @param contextPlatform active context platform
	 * @param platformConstant {@link PlatformEnum} constant for the current method
     * @return 'true' if the associated method should run on the specified target platform; otherwise 'false'
	 */
    public static boolean shouldRun(String contextPlatform, PlatformEnum platformConstant) {
        if ((contextPlatform != null) && (platformConstant != null)) {
    		return platformConstant.matches(contextPlatform);
        }
        return true;
    }

}
