package com.nordstrom.automation.selenium.exceptions;

public class DriverNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 657965846077748022L;

	public DriverNotAvailableException(String message) {
		super(message);
	}
}
