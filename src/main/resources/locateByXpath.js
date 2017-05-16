var context = (arguments[0].length) ? arguments[0][0] : document;
return document.evaluate(arguments[1], context, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
