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

public class ReflectUtil {
	
	private ReflectUtil() {
		throw new AssertionError("ReflectUtil is a static utility class that cannot be instantiated");
	}
	
	public static String parametersAsString(Method method) {
		return parametersAsString(method, false);
	}

	public static String getSignature(Method method, boolean longTypeNames) {
		return method.getName() + "(" + parametersAsString(method, longTypeNames) + ")";
	}

	public static String parametersAsString(Method method, boolean longTypeNames) {
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

	public static String getSignature(Method method) {
		return getSignature(method, false);
	}

}
