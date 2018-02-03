package de.freifunk_dresden.hopglass.logging;

import java.util.logging.ConsoleHandler;

public class FancyConsoleHandler extends ConsoleHandler {
    
    public FancyConsoleHandler() {
        setFormatter(new DateOutputFormatter());
        setOutputStream(System.out);
    }
}
