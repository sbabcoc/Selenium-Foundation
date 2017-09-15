var found = document.getElementsByTagName("meta");
for (var i = 0; i < found.length; i++) {
    if (found[i].getAttribute("name") == arguments[0]) return found[i];
}
throwNew('org.openqa.selenium.NoSuchElementException', 'No meta element found with name: ' + arguments[0]);
