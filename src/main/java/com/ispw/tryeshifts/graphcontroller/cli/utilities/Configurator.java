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
        Formatter minimalFormatter = new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                // Stampa solo il messaggio, utile per la CLI
                return logRecord.getMessage();
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
