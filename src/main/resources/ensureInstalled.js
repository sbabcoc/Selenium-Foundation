return (function (installSource, installedCheckSource) {
    try {
        // Build a callable installed-check function
        var isInstalled = Function(
            'return (' + installedCheckSource + ');'
        );

        // If already installed, succeed
        if (!!isInstalled()) {
            return true;
        }

        // Install runtime
        Function(installSource)();

        // Re-check after installation
        return !!isInstalled();
    } catch (e) {
        return false;
    }
})(arguments[0], arguments[1]);
