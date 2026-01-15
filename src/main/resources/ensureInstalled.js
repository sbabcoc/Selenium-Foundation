return (function(installSource, installedCheckSource) {
    try {
        var isInstalled = Function('return (' + installedCheckSource + ');');
        if (!!isInstalled()) return true;

        // Attempt DOM injection first
        if (document.head) {
            var script = document.createElement('script');
            script.text = installSource;
            document.head.appendChild(script);
            document.head.removeChild(script);
        } else {
            // Fallback for HtmlUnit or no <head>
            Function(installSource)();
        }

        return !!isInstalled();
    } catch (e) {
        return false;
    }
})(arguments[0], arguments[1]);
