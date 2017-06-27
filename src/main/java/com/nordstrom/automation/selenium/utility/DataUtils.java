package com.nordstrom.automation.selenium.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * This static utility class contains methods related to representational transformation.
 */
public class DataUtils {
	
	private DataUtils() {
		throw new AssertionError("DataUtils is a static utility class that cannot be instantiated");
	}
	
	/**
	 * Transform the specified JSON string into the corresponding {@link JsonObject}.
	 * 
	 * @param json JSON object string
	 * @return de-serialized {@link JsonObject} 
	 */
	public static JsonObject deserializeObject(String json) {
		try {
			return new Gson().fromJson(json, JsonObject.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

}
