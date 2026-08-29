package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;

import com.ispw.tryeshifts.utils.KeyGenerator;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIService;

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
    private static final int TIME_COLUMN_WIDTH =12;
    private static final int DAY_COLUMN_WIDTH= 12;
    private static final String SPACE_SLOT = "%-" + TIME_COLUMN_WIDTH +"s";
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
                String choice = CLIService.readString("Select: ").toUpperCase();
                if (choice.equals("0")) {
                    back = true;
                } else {
                    handleAction(choice, wp, status, user);
                }
            } catch (BaseException e) {
                logger.severe("Error: " + e.getMessage()+"\n");
            }
        }
    }

    private  void printUI(WorkplaceBean wp, String status) {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        boolean isLocked = status.equals(LOCKED_STATUS) || status.equals(PUBLISHED_STATUS);

        CLIService.println("--- SHIFTS DASHBOARD: " + wp.getWorkplaceName() + " ---");
        printWorkerTable(wp);

        CLIService.println("AVAILABLE ACTIONS:");
        if (user.getEmail().equals(wp.getOwnerEmail())) {
            printOwnerMenu(status);
        } else {
            printWorkerMenu(isLocked);
        }

        CLIService.println("N. Next Week P. Previous Week 0. Back to the Home");

    }

    private  void printOwnerMenu(String status) {
        if (status.equals(OPEN_STATUS)) {
            CLIService.println("1. Lock Availability (Worker's will not be able to give other shifts)");
        } else if (status.equals(LOCKED_STATUS)) {
            CLIService.println("1. Publish Shifts");
        }
        CLIService.println("2. Modify Shifts Manually");
    }

    private  void printWorkerMenu(boolean isLocked) {
        if (!isLocked) {
            CLIService.println("1. Insert/modify your availaibility");
        } else {
            CLIService.println("[WEEK LOCKED - Availability not mutable]");
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
                if(weekOffset<2)weekOffset++;
                else logger.warning("You can give shifts for up to 2 weeks\n");
                break;
            case "P":
                if(weekOffset>0){
                    weekOffset--;
                } else{
                    logger.warning("You can't go back to previous weeks\n");
                }
                break;
            default:
                logger.warning("Invalid operation!\n");
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
            CLIService.println("The Shifts have already been published");
        }
    }


    private  void giveAvailability() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        UserBean user = SessionContext.getInstance().getLoggeduser();

        if(wp == null || user == null) {
            logger.severe("User or Workplace is null!\n");
            return;
        }

        CLIService.println("--- Availability Insertion ---");

        // 1. Selezione Giorno
        String selectedDay = promptDaySelection(wp.getSelectedDays());
        if (selectedDay == null) return;

        // 2. Selezione Fascia Oraria
        String fullSlot = promptSlotSelection(wp.getShiftsBean());
        if (fullSlot == null) return;

        // 3. Elaborazione e Salvataggio
        try {
            processAndSave(user, wp, selectedDay, fullSlot);
            CLIService.println("Completed Synchronization!");
        } catch (BaseException e) {
            logger.severe("Error: " + e.getMessage()+"\n");
        }
    }

    private  String promptDaySelection(List<String> activeDays) {
        CLIService.println("Select the days (1-7) and 0 to annul and exit.: ");
        int dayChoice = CLIService.readInt("> ");
        if (dayChoice <= 0 || dayChoice > 7) return null;

        String selectedDay = days[dayChoice - 1];
        if (!activeDays.contains(selectedDay)) {
            msg = "ATTENTION: the workplace is closed on " + selectedDay + ". select another day.\n";
            logger.warning(msg);
            return null;
        }
        return selectedDay;
    }
    private  String promptSlotSelection(List<String> slots) {
            for (int i = 0; i < slots.size(); i++) {
                msg = (i + 1) + ". " + slots.get(i) + "\n";
                CLIService.println(msg);
            }
            int slotChoice = CLIService.readInt("Select hour slot: ");
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
            CLIService.println("Adding availability..");
        } else {
            CLIService.println("Removing Availability...");
        }

        ac.saveAvailabilities(beansToSave,user,wp,currentWeekId);
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
        CLIService.println("✅ Week succesfully locked! Workers can no long insert data.");
    } catch (BaseException e) {
        logger.severe("Error during the Locking: " + e.getMessage()+"\n");
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
            logger.warning("Technical error, could not fetch the shifts - " + e.getMessage()+"\n");
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
                line.append(String.format(SPACE_SLOT, slot));
            } else {
                line.append(String.format(SPACE_SLOT, ""));
            }

            for (String day : days) {
                List<String> namesInCell = cellData.get(day);
                String nameToPrint = (r < namesInCell.size()) ? namesInCell.get(r) : "";
                line.append(String.format("| %-" +DAY_COLUMN_WIDTH +"s",fitCellContent(nameToPrint)));
            }
            msg =line + "";
            CLIService.println(msg);
        }
        msg = "-".repeat(TIME_COLUMN_WIDTH + (days.length * DAY_COLUMN_WIDTH +2 )) ;
        // Una linea di separazione opzionale tra una fascia oraria e l'altra
        CLIService.println(msg);
    }


    private  void printDashboardHeader(String status) {
        // Creiamo la riga di stato
        String statusLine = String.format("STATUS: %s | WEEK: %s",
                status, ac.getWeekRangeString(weekOffset));
        msg = statusLine+"";
        CLIService.println(msg);

        StringBuilder header = new StringBuilder(String.format(SPACE_SLOT, "ORA"));
        for (String day : days) {
            header.append(String.format("| %-"+DAY_COLUMN_WIDTH+"s", day.toUpperCase())); // <--- Portato a 20
        }
        msg = header.toString();
        CLIService.println(msg);
        msg = "-".repeat(header.length());
        CLIService.println(msg);
    }
    private String fitCellContent(String content) {
        if(content == null){
            return "";
        }
        return content.length() <= DAY_COLUMN_WIDTH ? content : content.substring(0, DAY_COLUMN_WIDTH-3)+"...";
    }
    private  void publishShifts(WorkplaceBean wp){
        try {
            CLIService.println("--- Shifts Publication ---");
            CLIService.println("The workers will be able to see the official shifts.");
            String conferma = CLIService.readString("Do you confirm the pubblication of the shifts for the week: " + currentWeekId + "? (y/n): ");

            if (conferma.equalsIgnoreCase("y")) {

                // Chiamata al tuo Applicativo
                pubAc.handlePublishAction(wp, currentWeekId);

                CLIService.println("✅ Shifts Publication successfully, now the week is only read mode");
            } else {
                CLIService.println("Operation annulled.");
            }
        } catch (BaseException e) {
            logger.severe("❌ Error during the publication " + e.getMessage()+"\n");
        }
    }
    private  void modifyShifts() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp == null){
            CLIService.println("error uploading the current workplace");
            return;
        }
        UserBean user = SessionContext.getInstance().getLoggeduser();
        if(user == null){
            CLIService.println("error uploading the current user");
        }

        CLIService.println("---MANUAL REMOVAL OF WORKERS ---");

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
                CLIService.println("No worker has reserved this shift .");
                return;
            }

            // 4. Mostriamo l'elenco numerato dei candidati
            CLIService.println("Workers reserved for " + selectedDay + " at " + fullSlot + ":");
            for (int i = 0; i < candidates.size(); i++) {
                final int index = i;
                CLIService.println("[" + (index + 1) + "] " + candidates.get(index));
            }
            CLIService.println("[0] Cancel");

            // 5. Acquisiamo la scelta del Boss
            int workerChoice = CLIService.readInt("Select the worker to remove: ");
            if (workerChoice <= 0 || workerChoice > candidates.size()) {
                CLIService.println("Operation Canceled.");
                return;
            }

            // 6. Troviamo l'email esatta e invochiamo il Controller Applicativo
            String workerEmail = candidates.get(workerChoice - 1);

            // Usiamo LO STESSO IDENTICO METODO creato per JavaFX!
            ac.removeWorkerFromShift(workerEmail, wp.getWorkplaceName(), currentWeekId, selectedDay, fullSlot);

            CLIService.println("✅ Worker '" + workerEmail + "' removed succesfully!");

        } catch (BaseException e) {
            logger.severe("Error during the remotion: " + e.getMessage()+"\n");
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
