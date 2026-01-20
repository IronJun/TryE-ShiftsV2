package com.ispw.tryeshifts.graphcontroller.utilities;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftDistributor {
    public Map<String, String> distribute(Map<String, List<String>> availabilities) {
        Map<String, String> assignments = new HashMap<>();
        // Teniamo il conto di quanti turni assegniamo a ogni persona per equità
        Map<String, Integer> workerLoad = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : availabilities.entrySet()) {
            String cellKey = entry.getKey();
            List<String> candidates = entry.getValue();

            if (candidates == null || candidates.isEmpty()) {
                assignments.put(cellKey, "NON ASSEGNATO");
                continue;
            }

            // LOGICA FAIR: Scegliamo il candidato con meno turni già assegnati
            String winner = selectBestCandidate(candidates, workerLoad);

            assignments.put(cellKey, winner);
            workerLoad.put(winner, workerLoad.getOrDefault(winner, 0) + 1);
        }
        return assignments;
    }

    private String selectBestCandidate(List<String> candidates, Map<String, Integer> workerLoad) {
        // Ordiniamo i candidati per carico di lavoro crescente
        candidates.sort(Comparator.comparingInt(email -> workerLoad.getOrDefault(email, 0)));

        // Se i primi hanno lo stesso carico, ne prendiamo uno a caso tra i "minimi" per varietà
        return candidates.get(0);
    }
}
