package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.CreateWorkplaceAC;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class NewWorkplaceCLI {
    private static final Logger LOGGER = Logger.getLogger(NewWorkplaceCLI.class.getName());
    private static String msg;
    private NewWorkplaceCLI(){
        throw new IllegalStateException("Utility class");
    }
    public static void start(){
            LOGGER.info("\n--- CREAZIONE NUOVO WORKPLACE ---\n");

            // 1. Dati base
            String name = CLIReader.readString("Nome del Workplace: ");
            String address = CLIReader.readString("Indirizzo: ");

            // 2. Selezione Giorni (Multi-selezione)
            List<String> selectedDays = selectOperatingDays();

            // 3. Definizione Fasce Orarie
            List<String> slots = defineTimeSlots();

            // 4. Invio all'App Controller
            try {
                WorkplaceBean newWp = new WorkplaceBean(name,address,selectedDays,slots,SessionContext.getInstance().getLoggeduser().getEmail());
                // Chiamata all'AC (usa il metodo che hai già per JavaFX)
                CreateWorkplaceAC.createWorkplace(newWp);
                msg = "\n✅ Workplace '" + name + "' creato con successo!\n";
                LOGGER.info(msg);
            } catch (DuplicateEntityException e) {
                LOGGER.severe("\n Workplace already existing " + e.getMessage() + "\n");
            } catch(DataFetchException e){
                LOGGER.severe("\n Error fetching workplaces: " + e.getMessage() + "\n");
            } catch (BaseException e) {
                LOGGER.severe("Generic error: " + e.getMessage() + "\n");
            }
        }


    public static List<String> selectOperatingDays() {
        List<String> selected = new ArrayList<>();
        String[] allDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        LOGGER.info("Select when the workplace is opened by numbers and separated by ',' example: 1,2,3):");
        for (int i = 0; i < allDays.length; i++) {
            msg = (i + 1) + ". " + allDays[i] + "\n";
            LOGGER.info(msg);
        }

        String input = CLIReader.readString("> ");
        String[] parts = input.split(",");
        for (String p : parts) {
            int index = Integer.parseInt(p.trim()) - 1;
            if (index >= 0 && index < 7) selected.add(allDays[index]);
        }
        return selected;
    }
    public static List<String> defineTimeSlots() {
        List<String> slots = new ArrayList<>();
        boolean adding = true;

        LOGGER.info("\nSelect the shifts of the workplace with the following format : HH:mm - HH:mm.");
        while (adding) {
            String start = CLIReader.readString("\nOra inizio (HH:mm): ");
            String end = CLIReader.readString("\nOra fine (HH:mm): ");
            try{
                String[] startParts = start.trim().split(":");
                String[] endParts = end.trim().split(":");
                if(startParts.length != 2 || endParts.length != 2){
                    LOGGER.warning("\nInvalid time format. Be sure to use HH:mm (es: 08:30)");
                    continue;
                }
                String startH = startParts[0];
                String startM = startParts[1];
                String endH = endParts[0];
                String endM = endParts[1];

                String formattedShift = ManageShiftsAC.addShiftstoWorkaplce(startM,startH, endM, endH,slots);
                slots.add(formattedShift);
                msg = "✅ correctly added shift: "+formattedShift;
                LOGGER.info(msg);
            }catch(IllegalArgumentException e){
                LOGGER.warning("\nHour error: " +e.getMessage());
            }catch(BaseException e){
                LOGGER.warning("\nShit error: " +e.getMessage());
            }catch(Exception _){
                LOGGER.warning("\nError fetching, retry");
            }
            // Formattiamo noi la stringa per essere sicuri del separatore " - "
            String cont = CLIReader.readString("\nDo you want to add more shifts? (y/n): ");
            if (!cont.equalsIgnoreCase("y")) adding = false;
        }
        Collections.sort(slots);
        return slots;
    }
}
