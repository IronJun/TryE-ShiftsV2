package com.ispw.tryeshifts.graphcontroller.cli.utilities;

import java.util.logging.*;

public class Configurator {

    private Configurator() {
        throw new IllegalStateException("Utility class");
    }

    public static void configureLogger() {
        Logger rootLogger = Logger.getLogger("");

        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // 2. Creiamo il tuo formatter personalizzato "minimalista"
        Formatter minimalFormatter = new Formatter() {// Codici per resettare il colore al default del terminale
            private static final String RESET = "\u001B[0m";
            // 1;37m è il Bianco Brillante (Bold White)
            private static final String BRIGHT_WHITE = "\u001B[1;37m";

            @Override
            public String format(LogRecord logRecord) {
                // Applichiamo il bianco brillante, formattiamo il messaggio, resettiamo e andiamo a capo
                return BRIGHT_WHITE + formatMessage(logRecord) + RESET ;
            }
        };

        // 3. Creiamo l'handler che scrive sulla console (System.out)
        ConsoleHandler consoleHandler = new ConsoleHandler() {
            @Override
            public synchronized void publish(LogRecord logRecord) { // Usa logRecord per evitare lo smell del nome
                super.publish(logRecord);
                flush();
            }
        };

        // Imposta il tuo formatter personalizzato
        consoleHandler.setFormatter(minimalFormatter);
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }
}
