/*
Copyright 2011 Karl-Michael Schneider

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.nordstrom.automation.selenium.utility;

import java.lang.reflect.Method;

/**
 * This static utility class contains methods that use reflection to produce method signature strings.
 */
public final class ReflectUtils {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ReflectUtils() {
        throw new AssertionError("ReflectUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get method parameter type string with short type names.
     * 
     * @param method method from which to derive parameter type string
     * @return parameter type string
     */
    public static String parametersAsString(final Method method) {
        return parametersAsString(method, false);
    }
    
    /**
     * Get method signature with either long or short parameter type names.
     * 
     * @param method method from which to derive signature
     * @param longTypeNames 'true' to return long type names; 'false' to return short type names
     * @return method signature
     */
    public static String getSignature(final Method method, final boolean longTypeNames) {
        return method.getName() + "(" + parametersAsString(method, longTypeNames) + ")";
    }
    
    /**
     * Get method parameter type string with either long or short type names.
     * 
     * @param method method from which to derive parameter type string
     * @param longTypeNames 'true' to return long type names; 'false' to return short type names
     * @return parameter type string
     */
    public static String parametersAsString(final Method method, final boolean longTypeNames) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0)
            return "";
        StringBuilder paramString = new StringBuilder();
        paramString.append(longTypeNames ? parameterTypes[0].getName() : parameterTypes[0].getSimpleName());
        for (int i = 1; i < parameterTypes.length; i++) {
            paramString.append(",")
                    .append(longTypeNames ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName());
        }
        return paramString.toString();
    }

    /**
     * Get method signature with short parameter type names.
     * 
     * @param method method from which to derive signature
     * @return method signature
     */
    public static String getSignature(final Method method) {
        return getSignature(method, false);
    }

}
