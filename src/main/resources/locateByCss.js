var context = (arguments[0].length) ? arguments[0][0] : document;
if (arguments[2] > 0) {
	return context.querySelectorAll(arguments[1])[arguments[2]];
} else {
	return context.querySelector(arguments[1]);
}
