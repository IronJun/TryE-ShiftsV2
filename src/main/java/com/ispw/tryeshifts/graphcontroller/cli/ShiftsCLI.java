package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;

import com.ispw.tryeshifts.utils.KeyGenerator;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ShiftsCLI {
    private  final Logger logger = Logger.getLogger(ShiftsCLI.class.getName());
    private  String msg;
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    private static final String LOCKED_STATUS = "LOCKED";
    private static final String OPEN_STATUS = "OPEN";
    private static final String SELECTED_STATUS = "SELECTED";
    private  int weekOffset ;
    private  String currentWeekId;
    private static final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    static String SpaceSlot = "%-15s";
    private final ManageShiftsAC ac = new ManageShiftsAC();
    private final PublishShiftsAC pubAc = new PublishShiftsAC();



    public  void shiftsDashboard(WorkplaceBean wp) {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        boolean back = false;
        weekOffset = 0;

        while (!back) {
            try {
                currentWeekId = ac.calculateWeekId(weekOffset);
                String status = ac.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId);

                // 1. Visualizzazione
                printUI(wp, status);

                // 2. Gestione Input
                String choice = CLIReader.readString("Seleziona: ").toUpperCase();
                if (choice.equals("0")) {
                    back = true;
                } else {
                    handleAction(choice, wp, status, user);
                }
            } catch (BaseException e) {
                logger.severe("Errore: " + e.getMessage());
            }
        }
    }

    private  void printUI(WorkplaceBean wp, String status) {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        boolean isLocked = status.equals(LOCKED_STATUS) || status.equals(PUBLISHED_STATUS);

        logger.info("\n--- GESTIONE TURNI: " + wp.getWorkplaceName() + " ---\n");
        printWorkerTable(wp);

        logger.info("\nAZIONI DISPONIBILI:\n");
        if (user.getEmail().equals(wp.getOwnerEmail())) {
            printOwnerMenu(status);
        } else {
            printWorkerMenu(isLocked);
        }

        logger.info("N. Next Week \nP. Previous Week \n0. Torna alla Home\n");

    }

    private  void printOwnerMenu(String status) {
        if (status.equals(OPEN_STATUS)) {
            logger.info("1. Blocca disponibilità (Chiudi prenotazioni)\n");
        } else if (status.equals(LOCKED_STATUS)) {
            logger.info("1. Pubblica Turni Definitivi\n");
        }
        logger.info("2. Modifica Turni manualmente\n");
    }

    private  void printWorkerMenu(boolean isLocked) {
        if (!isLocked) {
            logger.info("1. Inserisci/Modifica le tue disponibilità\n");
        } else {
            logger.info("[SETTIMANA BLOCCATA - Disponibilità non modificabili]\n");
        }
    }
    private  void handleAction(String choice, WorkplaceBean wp, String status, UserBean user) {
        switch (choice) {
            case "1":
                executePrimaryAction(wp, status, user);
                break;
            case "2":
                if (user.getEmail().equals(wp.getOwnerEmail())) modifyShifts();
                break;
            case "N":
                weekOffset++;
                break;
            case "P":
                weekOffset--;
                break;
            default:
                logger.warning("Opzione non valida!");
        }
    }

    private  void executePrimaryAction(WorkplaceBean wp, String status, UserBean user) {
        boolean isOwner = user.getEmail().equals(wp.getOwnerEmail());

        if (!isOwner) {
            giveAvailability();
            return;
        }

        // Logica Owner
        if (status.equals(OPEN_STATUS)) lockshifts(wp);
        else if (status.equals(LOCKED_STATUS)) publishShifts(wp);
        else if (status.equals(PUBLISHED_STATUS)) {
            logger.info("I turni sono già stati pubblicati.");
        }
    }


    private  void giveAvailability() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        UserBean user = SessionContext.getInstance().getLoggeduser();

        if(wp == null || user == null) {
            logger.severe("User or Workplace is null!\n");
            return;
        }

        logger.info("\n--- Availability Insertion ---");

        // 1. Selezione Giorno
        String selectedDay = promptDaySelection(wp.getSelectedDays());
        if (selectedDay == null) return;

        // 2. Selezione Fascia Oraria
        String fullSlot = promptSlotSelection(wp.getShiftsBean());
        if (fullSlot == null) return;

        // 3. Elaborazione e Salvataggio
        try {
            processAndSave(user, wp, selectedDay, fullSlot);
            logger.info("Sincronizzazione completata!\n");
        } catch (BaseException e) {
            logger.severe("Errore: " + e.getMessage());
        }
    }

    private  String promptDaySelection(List<String> activeDays) {
        logger.info("Select the days (1-7) and 0 to annul and exit.\n: ");
        int dayChoice = CLIReader.readInt("> ");
        if (dayChoice <= 0 || dayChoice > 7) return null;

        String selectedDay = days[dayChoice - 1];
        if (!activeDays.contains(selectedDay)) {
            msg = "ATTENZIONE: Il locale è chiuso di " + selectedDay + ". Scegli un altro giorno.\n";
            logger.warning(msg);
            return null;
        }
        return selectedDay;
    }
    private  String promptSlotSelection(List<String> slots) {
            for (int i = 0; i < slots.size(); i++) {
                msg = (i + 1) + ". " + slots.get(i) + "\n";
                logger.info(msg);
            }
            int slotChoice = CLIReader.readInt("Seleziona la fascia oraria: ");
            if (slotChoice <= 0 || slotChoice > slots.size()) return null;
            return slots.get(slotChoice - 1);
        }
    private  void processAndSave(UserBean user, WorkplaceBean wp,
                                       String selectedDay, String fullSlot) throws BaseException {
        Map<String, List<String>> currentData = ac.getShiftData(user, wp,currentWeekId);

        String searchKey = KeyGenerator.buildShiftKey(currentWeekId, selectedDay, fullSlot);

        List<AvailabilityBean> beansToSave = convertMapToBeans(currentData, user, wp, searchKey);

        // Gestione Toggle (Aggiunta se non presente)
        boolean alreadySelected = currentData.getOrDefault(searchKey, new ArrayList<>()).contains(SELECTED_STATUS);
        if (!alreadySelected) {
            String[] parts = fullSlot.split("-");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            beansToSave.add(new AvailabilityBean(user.getEmail(), wp.getWorkplaceName(),
                    selectedDay, parts[0], parts[1], currentWeekId));
            logger.info("Aggiunta disponibilità...");
        } else {
            logger.info("Rimozione disponibilità...");
        }

        ac.saveAvailabilities(beansToSave);
    }

    private  List<AvailabilityBean> convertMapToBeans(Map<String, List<String>> currentData,
                                                            UserBean user, WorkplaceBean wp,
                                                            String currentSelectionKey) {
        List<AvailabilityBean> beans = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : currentData.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().contains(SELECTED_STATUS) && !key.equals(currentSelectionKey)) {
                String cleanKey = key.replace(currentWeekId+ "_","");

                String[]dayAndSlot = cleanKey.split("_");
                String day = dayAndSlot[0];
                String[] timeParts = dayAndSlot[1].split("-");

                beans.add(new AvailabilityBean(
                        user.getEmail(),
                        wp.getWorkplaceName(),
                        day,
                        timeParts[0].trim(),
                        timeParts[1].trim(),
                        currentWeekId));
            }
        }
        return beans;
    }
    private  void lockshifts(WorkplaceBean wp){
        try {
        // Cambiamo lo stato da OPEN a LOCKED
        ac.updateWeekStatusShifts(wp.getWorkplaceName(), currentWeekId, LOCKED_STATUS);
        logger.info("✅ Settimana bloccata con successo! I lavoratori non possono più inserire dati.");
    } catch (BaseException e) {
        logger.severe("Errore durante il blocco: " + e.getMessage());
    }}
    private  void printWorkerTable(WorkplaceBean wp) {
        try {
            UserBean loggedUser = SessionContext.getInstance().getLoggeduser();

            // 1. Recupero Dati
            Map<String, List<String>> assignments = pubAc.getAssignmentsForWeek(wp, currentWeekId);
            Map<String, List<String>> shifts = ac.getShiftData(loggedUser, wp,currentWeekId);
            String status = ac.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId);
            boolean isOwner = loggedUser.getEmail().equals(wp.getOwnerEmail());

            // 2. Stampa Intestazione
            printDashboardHeader(status);

            // 3. Stampa Griglia
            printGrid(wp, status, isOwner, shifts, assignments, loggedUser);

        } catch (BaseException e) {
            logger.warning("Errore tecnico: Impossibile recuperare i turni - " + e.getMessage());
        }
    }

    public record TableContext(
            String status,
            boolean isOwner,
            Map<String, List<String>> shifts,
            Map<String, List<String>> assignments,
            UserBean user
    ) {}
    private  String buildCellContent(String day, String slot, List<String> activeDays, TableContext ctx) {

        // Verifichiamo se il giorno è attivo (usando stream per ridurre i cicli for)
        boolean isDayActive = activeDays.stream().anyMatch(d -> d.equalsIgnoreCase(day));

        if (!isDayActive) {
            return "  CLOSED  ";
        }

        String searchKey = KeyGenerator.buildShiftKey(currentWeekId, day, slot);
        return getCellText(ctx.status, ctx.isOwner, searchKey, ctx.shifts, ctx.assignments, ctx.user);
    }
    private  void printGrid(WorkplaceBean wp, String status, boolean isOwner,
                                  Map<String, List<String>> shifts,
                                  Map<String, List<String>> assignments, UserBean user) {

        List<String> timeSlots = wp.getShiftsBean();
        List<String> activeDays = wp.getSelectedDays();
        TableContext ctx = new TableContext(status, isOwner, shifts, assignments, user);

        for (String slot : timeSlots) {
            // 1. Recuperiamo le liste di nomi per ogni giorno della settimana in questa fascia oraria
            Map<String, List<String>> cellData = new HashMap<>();
            int maxRowsInSlot = 1;

            for (String day : days) {
                String content = buildCellContent(day, slot, activeDays, ctx);
                // Split dei nomi se separati da virgola
                List<String> names = parseCellNames(content);
                cellData.put(day, names);
                maxRowsInSlot = Math.max(maxRowsInSlot, names.size());
            }
            printSlotRows(slot,maxRowsInSlot,cellData);
            // 2. Stampiamo tante righe fisiche quante sono i nomi (maxRowsInSlot)

        }
    }
    private  List<String> parseCellNames(String content){
        if(content.equals("-") || content.equals("  CLOSED  ")){
            return Collections.singletonList(content);
        }
        return Arrays.asList(content.split(","));
    }
    private  void printSlotRows(String slot, int maxRowsInSlot, Map<String, List<String>> cellData) {
        for (int r = 0; r < maxRowsInSlot; r++) {
            StringBuilder line = new StringBuilder();

            // Solo sulla prima riga del blocco stampiamo l'orario, altrimenti spazi vuoti
            if (r == 0) {
                line.append(String.format(SpaceSlot, slot));
            } else {
                line.append(String.format(SpaceSlot, ""));
            }

            for (String day : days) {
                List<String> namesInCell = cellData.get(day);
                String nameToPrint = (r < namesInCell.size()) ? namesInCell.get(r) : "";
                line.append(String.format("| %-20s", nameToPrint));
            }
            msg =line + "\n";
            logger.info(msg);
        }
        msg = "-".repeat(15 + (days.length * 22)) + "\n";
        // Una linea di separazione opzionale tra una fascia oraria e l'altra
        logger.info(msg);
    }
    private  void printDashboardHeader(String status) {
        // Creiamo la riga di stato
        String statusLine = String.format("STATUS: %s | WEEK: %s",
                status, ac.getWeekRangeString(weekOffset));
        msg = statusLine+"\n";
        logger.info(msg);

        StringBuilder header = new StringBuilder(String.format(SpaceSlot, "ORA"));
        for (String day : days) {
            header.append(String.format("| %-20s", day.toUpperCase())); // <--- Portato a 20
        }
        msg = header.toString()+"\n";
        logger.info(msg);
        msg = "-".repeat(header.length())+"\n";
        logger.info(msg);
    }
    private  void publishShifts(WorkplaceBean wp){
        NotificationAC nc = new NotificationAC();
        try {
            logger.info("\n--- PUBBLICAZIONE TURNI DEFINITIVI ---");
            logger.info("\nStai per rendere i turni visibili a tutti i lavoratori.");
            String conferma = CLIReader.readString("Confermi la pubblicazione per la settimana " + currentWeekId + "? (y/n): ");

            if (conferma.equalsIgnoreCase("y")) {

                // Chiamata al tuo Applicativo
                pubAc.publish(wp, currentWeekId);

                logger.info("\n✅ Turni pubblicati con successo! La settimana è ora in sola lettura.");
                nc.sendActiveWorkerNotifAsync(wp.getWorkplaceName(),"Turni per: "+wp.getWorkplaceName()+" pubblicati","SHIFTS");
            } else {
                logger.info("\nOperazione annullata.");
            }
        } catch (BaseException e) {
            logger.severe("❌ Errore durante la pubblicazione: " + e.getMessage());
        }
    }
    private  void modifyShifts() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp == null){
            logger.info("error uploading the current workplace");
            return;
        }
        UserBean user = SessionContext.getInstance().getLoggeduser();
        if(user == null){
            logger.info("error uploading the current user");
        }

        logger.info("\n--- RIMOZIONE MANUALE LAVORATORE ---");

        // 1. Chiediamo al Boss il Giorno (sfruttando il tuo metodo esistente)
        String selectedDay = promptDaySelection(wp.getSelectedDays());
        if (selectedDay == null) return;

        // 2. Chiediamo al Boss la Fascia Oraria
        String fullSlot = promptSlotSelection(wp.getShiftsBean());
        if (fullSlot == null) return;

        try {
            // 3. Recuperiamo la mappa dei turni per capire chi è prenotato in quella cella
            Map<String, List<String>> currentData = ac.getShiftData(user, wp, currentWeekId);
            String searchKey = KeyGenerator.buildShiftKey(currentWeekId, selectedDay, fullSlot);

            // Estraiamo la lista delle email prenotate in quello slot
            List<String> candidates = currentData.getOrDefault(searchKey, new ArrayList<>());

            if (candidates.isEmpty()) {
                logger.info("Nessun lavoratore prenotato per questo turno.\n");
                return;
            }

            // 4. Mostriamo l'elenco numerato dei candidati
            logger.info(()-> "\nLavoratori prenotati per " + selectedDay + " alle " + fullSlot + ":");
            for (int i = 0; i < candidates.size(); i++) {
                final int index = i;
                logger.info(()->"[" + (index + 1) + "] " + candidates.get(index));
            }
            logger.info("[0] Annulla");

            // 5. Acquisiamo la scelta del Boss
            int workerChoice = CLIReader.readInt("Seleziona il lavoratore da rimuovere: ");
            if (workerChoice <= 0 || workerChoice > candidates.size()) {
                logger.info("Operazione annullata.\n");
                return;
            }

            // 6. Troviamo l'email esatta e invochiamo il Controller Applicativo
            String workerEmail = candidates.get(workerChoice - 1);

            // Usiamo LO STESSO IDENTICO METODO creato per JavaFX!
            ac.removeWorkerFromShift(workerEmail, wp.getWorkplaceName(), currentWeekId, selectedDay, fullSlot);

            logger.info(()->"✅ Worker '" + workerEmail + "' removed succesfully!\n");

        } catch (BaseException e) {
            logger.severe("Errore during the remtion: " + e.getMessage());
        }
    }
    private  String getCellText(String status, boolean isOwner, String key,
                                      Map<String, List<String>> shifts,
                                      Map<String, List<String>> assignments, UserBean user) {
        List<String> rawList;
        if (status.equals(PUBLISHED_STATUS)) {
            rawList = assignments.getOrDefault(key, Collections.emptyList());
        } else {
            rawList = shifts.getOrDefault(key, Collections.emptyList());
            if (!isOwner) {
                return rawList.contains(SELECTED_STATUS) ? user.getName() : "-";
            }
        }

        if (rawList.isEmpty()) return "-";

        // Restituiamo i nomi brevi separati da virgola, senza troncamento finale
        return rawList.stream()
                .map(email -> email.split("@")[0])
                .collect(Collectors.joining(","));
    }
}
