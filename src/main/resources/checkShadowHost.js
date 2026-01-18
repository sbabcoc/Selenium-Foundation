var shadow = arguments[0].shadowRoot;
if (shadow == null) {
  __wd.fail('com.nordstrom.automation.selenium.exceptions.ShadowRootContextException', 'Invalid shadow host: ' + arguments[0]);
}
