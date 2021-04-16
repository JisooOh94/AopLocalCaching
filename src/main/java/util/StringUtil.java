package util;

import org.slf4j.helpers.MessageFormatter;

public class StringUtil {
	private StringUtil() {
		throw new AssertionError();
	}

	public static String format(String message, Object... args) {
		return MessageFormatter.arrayFormat(message, args).getMessage();
	}
}
