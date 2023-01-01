#!/bin/bash
error_exit()
{
  echo
  echo "$1" 1>&2
  usage
  exit 1
}

usage()
{
  echo
  echo "Usage: $0 [options...]" >&2
  echo
  echo "    -b <browser>        {chrome|edge|espresso|firefox|htmlunit|mac2|opera|phantomjs|safari|uiautomator2|windows|xcuitest}"
  echo "    -h                  run in 'headless' mode (only supported by Chrome, Edge, and Firefox)"
  echo
  echo "The browser-less support tests are executed if no options are specified"
}

. gradle.properties > /dev/null 2>&1
if [[ ! "${version}" =~ -SNAPSHOT$ ]]; then
  error_exit "not snapshot version: ${version}"
fi

revision=${version/-/-s3-}

while getopts :b:h flag
do
  case "${flag}" in
    b)
      browser=${OPTARG}
      ;;
    h)
      headless='.headless'
      ;;

    *)
      error_exit "unsupported option: ${OPTARG}"
      ;;
  esac
done

case "${browser}" in
  htmlunit|opera|phantomjs|safari)
    targetPlatform=web-app
    browserProfile=-P${browser}
    seleniumSettings=("-Dselenium.browser.name=${browser}")
    ;;

  chrome|firefox)
    targetPlatform=web-app
    browserProfile="-P${browser}"
    seleniumSettings=("-Dselenium.browser.name=${browser}${headless}")
    ;;

  edge)
    targetPlatform=web-app
    browserProfile=-Pedge
    seleniumSettings=("-Dselenium.browser.name=MicrosoftEdge${headless}")
    ;;

  espresso)
    targetPlatform=android
    browserProfile=-Pespresso
    seleniumSettings=(-Dselenium.browser.caps='{"platformName":"Android","appium:automationName":"Espresso","appium:forceEspressoRebuild":true,"appium:showGradleLog":true,"appium:app":"https://github.com/appium/appium/raw/master/packages/appium/sample-code/apps/ApiDemos-debug.apk"}' -Dselenium.grid.examples=false)
    ;;

  uiautomator2)
    targetPlatform=android
    browserProfile=-Puiautomator2
    seleniumSettings=(-Dselenium.browser.caps='{"platformName":"Android","appium:automationName":"UiAutomator2","appium:app":"https://github.com/appium/appium/raw/master/packages/appium/sample-code/apps/ApiDemos-debug.apk"}' -Dselenium.grid.examples=false)
    ;;

  xcuitest)
    targetPlatform=ios-app
    browserProfile=-Pxcuitest
    seleniumSettings=(-Dselenium.browser.caps='{"platformName":"iOS","appium:automationName":"XCUITest","appium:app":"https://github.com/appium/appium/raw/master/packages/appium/sample-code/apps/TestApp.app.zip"}' -Dselenium.grid.examples=false)
    ;;

  mac2)
    targetPlatform=mac-app
    browserProfile=-Pmac2
    seleniumSettings=(-Dselenium.browser.caps='{"platformName":"Mac","appium:automationName":"Mac2","appium:bundleId":"com.apple.TextEdit"}' -Dselenium.grid.examples=false)
    ;;

  windows)
    targetPlatform=windows
    browserProfile=-Pwindows
    seleniumSettings=(-Dselenium.browser.caps='{"platformName":"Windows","appium:automationName":"Windows","appium:app":"C:/Windows/system32/notepad.exe"}' -Dselenium.grid.examples=false)
    ;;

  *)
    targetPlatform=support
    ;;
esac

mvn "-Drevision=${revision}" clean install -Pselenium3 ${browserProfile} "-Dselenium.context.platform=${targetPlatform}" "${seleniumSettings[@]}"
