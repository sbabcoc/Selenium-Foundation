package com.nordstrom.automation.selenium.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DataUtils {
	
	private DataUtils() {
		throw new AssertionError("DataUtils is a static utility class that cannot be instantiated");
	}
	
	public static JsonObject deserializeObject(String json) {
		try {
			return new Gson().fromJson(json, JsonObject.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

}
