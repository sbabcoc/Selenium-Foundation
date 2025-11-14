package com.nordstrom.automation.selenium.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This utility class facilitates easy assembly lists of key chords for the {@code macos: keys} script
 * of Appium's <b>Mac2</b> engine.
 */
public final class KeysPayloadBuilder {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private KeysPayloadBuilder() {
		throw new AssertionError("KeysPayloadBuilder is a static utility class that cannot be instantiated");
	}

	/** SHIFT modifier */
	public static final int SHIFT = 1 << 1;
	/** CONTROL modifier */
	public static final int CTRL = 1 << 2;
	/** OPTION modifier */
	public static final int OPT = 1 << 3;
	/** COMMAND modifier */
	public static final int CMD = 1 << 4;

	/**
	 * Return an instance of the <b>Builder</b> class.
	 * 
	 * @return new {@link Builder} object
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * This class implements a builder for lists of key chords for the {@code macos: keys} script.
	 */
	public static final class Builder {
		private final List<Map<String, Object>> keys = new ArrayList<>();

		/**
		 * Add new key chord with modifiers.
		 * 
		 * @param k keystroke character
		 * @param flags modifier flags (may be omitted)
		 * @return this {@link Builder} object
		 * @see KeysPayloadBuilder#SHIFT
		 * @see KeysPayloadBuilder#CTRL
		 * @see KeysPayloadBuilder#OPT
		 * @see KeysPayloadBuilder#CMD
		 */
		public Builder key(String k, int... flags) {
			Map<String, Object> m = new HashMap<>();
			m.put("key", k);
			
		    if (flags != null && flags.length > 0) {
		        int combined = Arrays.stream(flags).reduce(0, (a, b) -> a | b);
		        m.put("modifierFlags", combined);
		    }

			keys.add(m);
			return this;
		}

		/**
		 * Assemble payload for the {@code macos: keys} script.
		 * 
		 * @return unmodifiable list of key chords for the {@code macos: keys} script
		 */
		public Map<String, Object> build() {
			Map<String, Object> map = Collections.unmodifiableMap(
				Collections.singletonMap(
					"keys", 
					keys.stream()
						.map(Collections::unmodifiableMap)
						.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList))));
			keys.clear();
			return map;
		}
	}
}
