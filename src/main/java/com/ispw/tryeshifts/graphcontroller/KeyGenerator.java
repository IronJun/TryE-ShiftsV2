package com.ispw.tryeshifts.graphcontroller;

public class KeyGenerator {
    private KeyGenerator() {} // Classe utility, non si istanzia

    public static String buildShiftKey(String weekId, String day, String slot) {
        // Uniformiamo tutto: niente spazi e formato standard
        return weekId + "_" + day + "_" + slot.replace(" ", "");
    }
}
