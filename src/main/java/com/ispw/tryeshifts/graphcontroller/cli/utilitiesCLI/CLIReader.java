package com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI;

import java.util.Scanner;
import java.util.logging.Logger;

public class CLIReader {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger LOGGER = Logger.getLogger(CLIReader.class.getName());

    private CLIReader(){
        throw new IllegalStateException("Utility class");
    }
    public static String readString(String message){
        LOGGER.info(message);
        return scanner.nextLine();
    }

    public static int readInt(String message){
        LOGGER.info(message);
        while(!scanner.hasNextInt()){
            LOGGER.warning("Invalid input, please try again");
            scanner.nextLine();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }
}
