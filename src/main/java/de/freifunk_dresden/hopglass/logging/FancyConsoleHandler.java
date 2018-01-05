package de.freifunk_dresden.hopglass.logging;

import java.util.logging.ConsoleHandler;

public class FancyConsoleHandler extends ConsoleHandler {
    
    private static final String CONSOLE_DATE = "HH:mm:ss";

    public FancyConsoleHandler() {
        setFormatter(new DateOutputFormatter(CONSOLE_DATE));
        setOutputStream(System.out);
    }
}
