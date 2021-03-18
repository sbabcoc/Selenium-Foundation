(function(root) {
	function CustomError(className, message) {
	    Object.defineProperty(this, 'className', {
	        enumerable: true,
	        writable: true,
	        value: className
	    });
	
	    Object.defineProperty(this, 'message', {
	        enumerable: true,
	        writable: true,
	        value: message
	    });
	
	    if (Error.hasOwnProperty('captureStackTrace')) { // V8
	        Error.captureStackTrace(this, this.constructor);
	    } else {
	        Object.defineProperty(this, 'stack', {
	            enumerable: true,
	            writable: false,
	            value: (new Error()).stack
	        });
	    }
	}
	
	if (typeof Object.setPrototypeOf == 'function') {
	    Object.setPrototypeOf(CustomError.prototype, Error.prototype);
	} else {
	    CustomError.prototype = Object.create(Error.prototype);
	}
	
	function Throwable(className, message) {
	    CustomError.call(this, className, message);
	}
	
	if (typeof Object.setPrototypeOf == 'function') {
	    Object.setPrototypeOf(Throwable.prototype, CustomError.prototype);
	} else {
	    Throwable.prototype = Object.create(CustomError.prototype);
	}
	
	function throwNew(className, message) {
		createErrorFor(className);
		var throwable = eval("new " + className + "('" + message + "');")
		throw JSON.stringify(throwable);
	}
	
	function createErrorFor(className) {
		if (isObject(className)) return;
		var i = className.lastIndexOf('.');
		var pkg = className.substr(0, i);
		var shortName = className.substr(i + 1);
		var script = createErrorFor.template.replace('[pkg]', pkg).replace(/\[short\]/g, shortName).replace(/\[class\]/g, className);
		eval(script);
	}
	
	createErrorFor.template = "(function() { " +
		"var space = namespace('[pkg]', [short]); " +
		"function [short](message) { " +
			"Throwable.call(this, '[class]', message); " +
		"} " +
		"if (typeof Object.setPrototypeOf == 'function') { " +
			"Object.setPrototypeOf([class].prototype, Throwable.prototype); " +
		"} else { " +
			"[class].prototype = Object.create(Throwable.prototype); " +
		"} " +
	"}());";
	
	// Creates namespaced functions within a closure.
	//
	// http://github.com/jweir/namespace
	// Copyright (c) 2011 John Weir john@famedriver.com
	//
	// License: Public Domain
	//
	// Usage (within a closure):
	//
	//   namepace(moduleName, function1, function2, ...);
	//
	// Example:
	//
	//   (function(){
	//     namespace("space", publicFn, anotherFn);
	//
	//     function privateFn(n) { return [n, "private"].join(" and "); }
	//     function publicFn(n)  { return privateFn(n); }
	//     function anotherFn(n) { return "another result"; }
	//   }());
	//
	//   space.publicFn("good"); /* => "good and private" */
	//
	// Accepts nested namespaces, even if the parent does not yet exist.
	//
	//   namespaces("foo.bar", func);
	//
	// If no string is given as the first argument, the functions will be scoped to the root.

	function namespace(moduleNamespace, functions) {
		var space, i;

		if (typeof arguments[0] != "string") {
			space = root;
			i = 0;
		} else {
			space = namespaceFor(arguments[0].split("."));
			i = 1;
		}

		for (var l = arguments.length; i < l; i++) {
			space[functionName(arguments[i])] = arguments[i];
		}
	}

	function functionName(fn) {
		return fn.name || fn.toString().match(/^\s*function\s+([^\s\(]+)/)[1];
	}

	function namespaceFor(parts) {
		var space = root;

		for (var i = 0; i < parts.length; i++) {
			space = space[parts[i]] = space[parts[i]] || {};
		}
		return space;
	}
	
	function isObject(object) {
		try {
			if (typeof object == 'string') object = eval(object);
			return ((object != null) && (typeof object == 'object'));
		} catch (e) {
			if ((e instanceof TypeError) || (e instanceof ReferenceError)) return false;
			throw e;
		}
	}

	namespace(namespace, isObject, CustomError, Throwable, throwNew, createErrorFor);
}(this)); // <- change this if you want to change the scope of namespace
