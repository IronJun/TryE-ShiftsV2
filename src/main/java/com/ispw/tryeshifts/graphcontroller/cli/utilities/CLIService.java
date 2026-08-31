package com.ispw.tryeshifts.graphcontroller.cli.utilities;

import java.io.Console;
import java.util.Scanner;
import java.util.logging.Logger;

public class CLIService {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger LOGGER = Logger.getLogger(CLIService.class.getName());
    private static Console console = System.console();

    private CLIService(){
        throw new IllegalStateException("Utility class");
    }


    public static String readString(String message){
        if(console!=null){
            String value = console.readLine("%s",message);
            return value == null ? "" : value;
        }
        print(message);
        return scanner.nextLine();
    }

    public static String readPassword(String message){
        if(console!=null){
            char[] password = console.readPassword("%s",message );
            return password == null?"":new String(password);
        }
        print(message);
        return scanner.nextLine();
    }


    public static int readInt(String message){
        while(true){
            try{
                return Integer.parseInt(readString(message).trim());
            }catch(NumberFormatException _){
                println("Insert valid number");
            }
        }
    }

    public static void print(String message){
        if(console!=null){
            console.printf("%s",message);
        }else{
            LOGGER.info(message);
        }
    }

    public static void println(String message){
        print(message + System.lineSeparator());
    }
}
