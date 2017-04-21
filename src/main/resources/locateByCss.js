if (arguments[2] > 0) {
	return arguments[0].querySelectorAll(arguments[1])[arguments[2]];
} else {
	return arguments[0].querySelector(arguments[1]);
}
