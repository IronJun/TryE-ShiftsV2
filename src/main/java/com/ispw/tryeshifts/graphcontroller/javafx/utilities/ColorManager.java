package com.ispw.tryeshifts.graphcontroller.javafx.utilities;

import java.util.HashMap;
import java.util.Map;

public class ColorManager {
    private static final Map<String, String> workerColorMap = new HashMap<>();
    private static final String[] palette = {
            "#E74C3C", "#3498DB", "#9B59B6", "#F1C40F",
            "#E67E22", "#16A085", "#2980B9", "#8E44AD"
    };
    private static int colorIndex = 0;

    public static String getColorForWorker(String workerName) {
        return workerColorMap.computeIfAbsent(workerName, k -> {
            String color = palette[colorIndex % palette.length];
            colorIndex++;
            return color;
        });
    }
}
