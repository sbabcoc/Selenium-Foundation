(function (root) {
  /* ============================================================
   * Namespace helper
   * ============================================================ */
  function namespace(moduleNamespace /* functions... */) {
    var space, i;

    if (typeof moduleNamespace !== 'string') {
      space = root;
      i = 0;
    } else {
      space = namespaceFor(moduleNamespace.split('.'));
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

  /* ============================================================
   * Idempotent injection guard
   * ============================================================ */
  if (root.__wdRuntime) return;

  var DEFAULT_TIMEOUT = 30000;

  /* ============================================================
   * Utilities
   * ============================================================ */
  function captureStack() {
    try {
      throw new Error();
    } catch (e) {
      return e && e.stack ? String(e.stack) : null;
    }
  }

  function isPromise(value) {
    return value && typeof value.then === 'function';
  }

  function normalizeJavaError(spec) {
    if (!spec || typeof spec !== 'object') {
      return {
        className: 'java.lang.RuntimeException',
        message: String(spec)
      };
    }

    return {
      className: String(spec.className || 'java.lang.RuntimeException'),
      message: spec.message != null ? String(spec.message) : null,
      details: spec.details != null ? spec.details : null,
      cause: spec.cause ? normalizeJavaError(spec.cause) : null
    };
  }

  function jsFailure(e) {
    return {
      status: 'error',
      exception: {
        className: 'java.lang.RuntimeException',
        message: e && e.message ? String(e.message) : String(e),
        stack: e && e.stack ? String(e.stack) : null
      }
    };
  }

  /* ============================================================
   * Completion controller (async only)
   * ============================================================ */
  function createCompletion(callback, timeoutMs) {
    var completed = false;
    var timeoutId = null;

    function complete(payload) {
      if (completed) return;
      completed = true;
      if (timeoutId) clearTimeout(timeoutId);
      callback(payload);
    }

    if (timeoutMs > 0) {
      timeoutId = setTimeout(function () {
        complete(jsFailure({ message: 'Script did not complete within ' + timeoutMs + ' ms' }));
      }, timeoutMs);
    }

    return complete;
  }

  /* ============================================================
   * Runtime API exposed to user scripts
   * ============================================================ */

  function fail(spec, message) {
      var errorSpec =
          typeof spec === 'string'
              ? { javaType: spec, message: message }
              : spec;

      var exceptionEnvelope = {
          status: "error",
          exception: {
              className: errorSpec.javaType || "java.lang.RuntimeException",
			  message: errorSpec.message == null
			      ? null
			      : String(errorSpec.message),
              stack: captureStack()
          }
      };

      throw { __wdUserFailure: true, payload: exceptionEnvelope };
  }

  /* ============================================================
   * Execution core
   * ============================================================ */

  function executeUserCode(fn, complete) {
      try {
          var result = fn();

          if (isPromise(result)) {
              result.then(
                  function (value) {
                      complete({ status: 'ok', value: value });
                  },
                  function (err) {
                      complete(jsFailureEnvelope(err));
                  }
              );
          } else {
              complete({ status: 'ok', value: result });
          }
      } catch (e) {
          if (e && e.__wdUserFailure) {
              complete(userFailureEnvelope(e.payload));
          } else {
              complete(jsFailureEnvelope(e));
          }
      }
  }

  function jsFailureEnvelope(e) {
      return {
          status: 'error',
          exception: {
              className: 'java.lang.RuntimeException',
              message: (e && e.message != null) ? String(e.message) : String(e),
              stack: (e && e.stack != null) ? String(e.stack) : null
          }
      };
  }

  function userFailureEnvelope(payload) {
      var javaType = payload && payload.error && payload.error.java && payload.error.java.javaType;
      return {
          status: 'error',
          exception: {
              className: javaType != null ? String(javaType) : "java.lang.RuntimeException",
              message: payload && payload.error && payload.error.java
                          && payload.error.java.message != null
                       ? String(payload.error.java.message)
                       : null,
              stack: payload && payload.error ? String(payload.error.jsStack) : null
          }
      };
  }

  /* ============================================================
   * Public runtime
   * ============================================================ */

  function runSync(fn /*, ...args */) {
      var args = Array.prototype.slice.call(arguments, 1); // grab extra args

      try {
          return { ok: true, value: fn.apply(null, args) }; // forward arguments
      } catch (e) {
          if (e && e.__wdUserFailure) return e.payload;
          return jsFailure(e);
      }
  }

  function runAsync(fn, callback, timeoutMs /*, ...args */) {
      var args = Array.prototype.slice.call(arguments, 3); // grab args after timeout
      var complete = createCompletion(
          callback,
          typeof timeoutMs === 'number' ? timeoutMs : DEFAULT_TIMEOUT
      );

      executeUserCode(function() {
          return fn.apply(null, args); // forward arguments to user function
      }, complete);
  }

  /* ============================================================
   * Namespace registration
   * ============================================================ */
  namespace('__wdRuntime', runSync, runAsync);
  namespace('__wd', fail);

}(this));
