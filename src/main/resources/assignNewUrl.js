function fixedEncodeURI(str) {
    // encode URI in conformity with RFC3986
    return encodeURI(str).replace(/%5B/g, ‘[’).replace(/%5D/g, ‘]’);
}
window.location.href = fixedEncodeURI(arguments[0]);