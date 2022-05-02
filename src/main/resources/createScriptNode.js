var head = document.getElementsByTagName('head')[0];
var script = document.createElement('script');
script.textContent = arguments[0];
head.appendChild(script);
