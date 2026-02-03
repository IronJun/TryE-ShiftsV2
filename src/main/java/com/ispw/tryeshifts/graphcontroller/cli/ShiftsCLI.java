package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ShiftsCLI {
    private static final Logger LOGGER = Logger.getLogger(ShiftsCLI.class.getName());
    private static int weekOffset ;
    private static String currentWeekId;
    private static final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private ShiftsCLI(){}
    public static void shiftsDashboard(WorkplaceBean wp) {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        boolean back = false;

        weekOffset = 0;
        while (!back) {
            try {
                currentWeekId = ManageShiftsAC.calculateWeekId(weekOffset);
                ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
                String status = manageShiftsAC.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId);
                boolean isLocked = status.equals("LOCKED") || status.equals("PUBLISHED");
                LOGGER.info("\n--- GESTIONE TURNI: " + wp.getWorkplaceName() + " ---\n");
                printWorkerTable(wp);
                LOGGER.info("\nAZIONI DISPONIBILI:\n");
                if (!user.getEmail().equals(wp.getOwnerEmail())) {
                    if(!isLocked) {
                        LOGGER.info("1. Inserisci/Modifica le tue disponibilità\n");
                    }else{
                        LOGGER.info("[SETTIMANA BLOCCATA - Disponibilità non modificabili]\n");                    }
                } else {
                    if(status.equals("OPEN")) {
                        LOGGER.info("1. Blocca disponibilità (Chiudi prenotazioni)\n");
                    }else if(status.equals("LOCKED")){
                        LOGGER.info("1. Pubblica Turni Definitivi\n");
                    }
                    LOGGER.info("2. Modifica Turni manualmente\n");
                }
                LOGGER.info("N. Next Week (Settimana Prossima)\n");
                LOGGER.info("P. Previous Week (Settimana Precedente)\n");
                LOGGER.info("0. Torna alla Home\n");

                String choice = CLIReader.readString("Seleziona: ").toUpperCase();
                switch (choice) {
                    case "0":
                        back = true;
                        break;
                    case "1":
                        if (!user.getEmail().equals(wp.getOwnerEmail())) GiveAvailability();
                        else{
                            if(status.equals("OPEN")){
                                LockShifts(wp);
                            }else if(status.equals("LOCKED")){
                                publishShifts(wp);
                            }else if(status.equals("PUBLISHED")){
                                LOGGER.info("I turni sono già stati pubblicati per questa settimana.");
                            }
                        }
                        break;
                    case "2":
                        modifyShifts();
                        break;
                    case "N":
                        weekOffset++;
                        break;
                    case "P":
                        weekOffset--;
                        break;
                    default:
                        LOGGER.warning("Invalid option!");
                        break;
                }
            }catch(BaseException e){
                LOGGER.severe("Errore: " + e.getMessage());
            }
        }
    }

    private static void GiveAvailability() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        UserBean user = SessionContext.getInstance().getLoggeduser();
        List<String> activeDays = wp.getSelectedDays(); // Es: ["MON", "TUE"]
        ManageShiftsAC ac = new ManageShiftsAC();

        LOGGER.info("\n--- Availability Insertion ---");
        LOGGER.info("Select the days (1-7) and 0 to annul and exit.\n: ");
        int dayChoice = CLIReader.readInt("> ");
        if (dayChoice <= 0 || dayChoice > 7) return;
        String selectedDay = days[dayChoice - 1];

        if (!activeDays.contains(selectedDay)) {
            LOGGER.warning("ATTENZIONE: Il locale è chiuso di " + selectedDay + ". Scegli un altro giorno.\n");
            return;
        }
        // Recuperiamo le fasce orarie reali dal WorkplaceBean


        // Selezione Fascia Oraria
        List<String> slots = wp.getShiftsBean();
        for (int i = 0; i < slots.size(); i++) {
            LOGGER.info((i + 1) + ". " + slots.get(i) + "\n");
        }
        int slotChoice = CLIReader.readInt("Seleziona la fascia oraria: ");

        if (dayChoice > 0 && dayChoice <= 7 && slotChoice > 0 && slotChoice <= slots.size()) {
            String day = days[dayChoice - 1];
            String fullSlot = slots.get(slotChoice - 1); // Es: "08:00 - 14:00"
            String[] parts = fullSlot.split("\\s*-\\s*");
            try {
                // 1. Recuperiamo le disponibilità attuali dal DB tramite l'AC
                // Passiamo currentWeekId per essere sicuri della settimana
                Map<String, List<String>> currentData = ac.getShiftData(user, wp, currentWeekId);
                String searchKey = selectedDay + "_" + fullSlot.replace(" ", "");

                // 2. Prepariamo la nuova lista di AvailabilityBean da inviare al tuo metodo saveAvailabilities
                // Il tuo AC fa deleteAvailabilitiesByUser, quindi dobbiamo mandargli il SET COMPLETO aggiornato
                List<AvailabilityBean> beansToSave = new ArrayList<>();

                // Convertiamo la mappa esistente (che ha "SELECTED" per i turni del worker) in Bean
                for (Map.Entry<String, List<String>> entry : currentData.entrySet()) {
                    if (entry.getValue().contains("SELECTED")) {
                        String fullKey = entry.getKey(); // Es: "Mon_08:00-14:00"
                        String[] keyParts = fullKey.split("_");
                        if (keyParts.length < 2) continue; // Salta chiavi malformate
                        String timePart = keyParts[1];
                        // Splittiamo in modo robusto: cerca il trattino ignorando spazi
                        String[] timeParts = timePart.split("-");
                        // Se la cella corrente è quella selezionata, la saltiamo (così facciamo il TOGGLE/RIMOZIONE)
                        if (timeParts.length < 2) {
                            // Se non c'è il trattino, forse il separatore è diverso?
                            // Proviamo a vedere cosa contiene davvero
                            LOGGER.warning("Formato orario inatteso nella chiave: " + timePart);
                            continue;
                        }
                        String currentSelectionKey = selectedDay + "_" + fullSlot.replace(" ", "");
                        if (fullKey.equals(currentSelectionKey)) {
                            continue;
                        }
                        beansToSave.add(new AvailabilityBean(user.getEmail(), wp.getWorkplaceName(),
                                keyParts[0], timeParts[0], timeParts[1], currentWeekId));
                    }
                }

                // 3. Se la cella non era presente, la aggiungiamo (Aggiunta)
                boolean alreadySelected = currentData.getOrDefault(searchKey, new ArrayList<>()).contains("SELECTED");
                if (!alreadySelected) {
                    beansToSave.add(new AvailabilityBean(user.getEmail(), wp.getWorkplaceName(),
                            selectedDay, parts[0], parts[1], currentWeekId));
                    LOGGER.info("Aggiunta disponibilità...");
                } else {
                    LOGGER.info("Rimozione disponibilità...");
                }
                LOGGER.info("\n--- RECAP INVIO AL DATABASE ---");
                for (AvailabilityBean b : beansToSave) {
                    LOGGER.info(String.format("Salvo: Giorno: %s | Inizio: %s | Fine: %s | Settimana: %s",
                            b.getDay(), b.getStartShift(), b.getEndShifts(), b.getWeekId()));
                }
                // 4. Salva il nuovo stato completo
                ac.saveAvailabilities(beansToSave);
                LOGGER.info("Sincronizzazione completata!\n");

            } catch (BaseException e) {
                LOGGER.severe("Errore: " + e.getMessage());
            }
        }
    }
    private static void LockShifts(WorkplaceBean wp){
        try {
        ManageShiftsAC ac = new ManageShiftsAC();
        // Cambiamo lo stato da OPEN a LOCKED
        ac.updateWeekStatusShifts(wp.getWorkplaceName(), currentWeekId, "LOCKED");
        LOGGER.info("✅ Settimana bloccata con successo! I lavoratori non possono più inserire dati.");
    } catch (BaseException e) {
        LOGGER.severe("Errore durante il blocco: " + e.getMessage());
    }}
    private static void printWorkerTable(WorkplaceBean wp){
        try {
            ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
            PublishShiftsAC publishAC = new PublishShiftsAC();
            UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
            Map<String, List<String>> assignments = publishAC.getAssignmentsForWeek(wp, currentWeekId);
            Map<String, List<String>> shifts = manageShiftsAC.getShiftData(loggedUser, wp, currentWeekId);
            List<String> activeDays = wp.getSelectedDays();
            String status = manageShiftsAC.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId);
            boolean isOwner = loggedUser.getEmail().equals(wp.getOwnerEmail());
            LOGGER.info("\nSTATUS: " + status + " | WEEK: " + ManageShiftsAC.getWeekRangeString(weekOffset) + "\n");
            StringBuilder header = new StringBuilder(String.format("%-15s", "ORA"));

            for (String day : days) {
                header.append(String.format("| %-15s", day.toUpperCase()));
            }
            LOGGER.info(header.toString() + "\n");
            LOGGER.info("-".repeat(header.length()) + "\n");

            List<String> timeSlots = wp.getShiftsBean();
            for (String slot : timeSlots) {
                StringBuilder row = new StringBuilder(String.format("%-15s", slot));

                for (String day : days) {
                    String timeKey = slot.replace(" ", "");
                    String searchKey = day + "_" + timeKey;
                    boolean isDayActive = false;
                    for (String activeDay : activeDays) {
                        if (activeDay.equalsIgnoreCase(day)) {
                            isDayActive = true;
                            break;
                        }
                    }

                    String content;
                    if (!isDayActive) {
                        content = "  CLOSED  ";
                    } else {
                        content = getCellText(status, isOwner, searchKey, shifts, assignments, loggedUser);
                    }
                    row.append(String.format("| %-15s", content));
                }
                LOGGER.info(row.toString() + "\n");
            }

        }catch(DataFetchException _){
            LOGGER.warning("Errore tecnico Impossibile recuperare i turni\n");
        }catch(EntityNotFoundException _){
            LOGGER.warning("availability not found Impossibile recuperare i turni\n");
        }catch(BaseException _){
            LOGGER.warning("Errore tecnico Impossibile recuperare i turni\n");
        }
    }
    private static void publishShifts(WorkplaceBean wp){
        try {
            LOGGER.info("\n--- PUBBLICAZIONE TURNI DEFINITIVI ---");
            LOGGER.info("Stai per rendere i turni visibili a tutti i lavoratori.");
            String conferma = CLIReader.readString("Confermi la pubblicazione per la settimana " + currentWeekId + "? (y/n): ");

            if (conferma.equalsIgnoreCase("y")) {
                PublishShiftsAC ac = new PublishShiftsAC();

                // Chiamata al tuo Applicativo
                ac.publish(wp, currentWeekId);

                LOGGER.info("\n✅ Turni pubblicati con successo! La settimana è ora in sola lettura.");
            } else {
                LOGGER.info("Operazione annullata.");
            }
        } catch (BaseException e) {
            LOGGER.severe("❌ Errore durante la pubblicazione: " + e.getMessage());
        }
    }
    private static void modifyShifts(){
        LOGGER.info("Modifica dei turni manuale non ancora implementata");
    }

    private static String getCellText(String status, boolean isOwner, String key,
                               Map<String, List<String>> shifts,
                               Map<String, List<String>> assignments, UserBean user) {

        if (status.equals("PUBLISHED")) {
            List<String> assigned = assignments.getOrDefault(key, new ArrayList<>());
            return assigned.isEmpty() ? "-" : String.join(",", assigned);
        }

        List<String> candidates = shifts.get(key);

        if (candidates == null || candidates.isEmpty()) {
            String[] parts = key.split("_"); // [Wed, 00:00-01:00]
            if (parts.length >= 2) {
                String startTime = parts[1].split("-")[0]; // 00:00
                String brokenKey = parts[0] + "_" + startTime + "-" + startTime; // Wed_00:00-00:00
                candidates = shifts.get(brokenKey);
            }
        }

        if (candidates == null) candidates = new ArrayList<>();

        if (!isOwner) {
            // Se l'AC ha messo "SELECTED" (nella chiave corretta o in quella rotta), mostra il nome
            if (candidates.contains("SELECTED")) {
                return user.getName();
            }
            return "-";
        }

        // LOGICA PER L'OWNER
        if (candidates.isEmpty()) return "-";

        List<String> shortNames = new ArrayList<>();
        for (String email : candidates) {
            shortNames.add(email.split("@")[0]);
        }

        String content = String.join(", ", shortNames);
        return content.length() > 15 ? content.substring(0, 12) + "..." : content;
    }
}
