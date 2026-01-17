var shadow = arguments[0].shadowRoot;
if (shadow == null) {
  __wd.fail('org.openqa.selenium.WebDriverException', 'Invalid shadow host: ' + arguments[0]);
}
