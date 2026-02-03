package com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI;

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
            public String format(LogRecord record) {
                // Stampa solo il messaggio, utile per la CLI
                return record.getMessage();
            }
        };

        // 3. Creiamo l'handler che scrive sulla console (System.out)
        StreamHandler stdoutHandler = new StreamHandler(System.out, minimalFormatter) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush(); // Garantisce che il testo appaia prima che la CLI aspetti l'input
            }
        };

        // 4. Impostiamo il livello di log (es. INFO) e aggiungiamo l'handler
        stdoutHandler.setLevel(Level.INFO);
        rootLogger.addHandler(stdoutHandler);
        rootLogger.setLevel(Level.INFO);
    }
}
