chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "openUrl" && message.url) {
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      if (tabs.length > 0) {
        chrome.tabs.update(tabs[0].id, { url: message.url });
        sendResponse({ status: "success" });
      } else {
        sendResponse({ status: "error", message: "No active tab found" });
      }
    });
    // Return true to indicate asynchronous response
    return true;
  }
});
