package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.CreateWorkplaceAC;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.exception.ValidationException;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class NewWorkplaceCLI {
    private  final Logger logger = Logger.getLogger(NewWorkplaceCLI.class.getName());
    private  String msg;

    public void start(){
            CLIReader.println("--- WORKPLACE CREATION ---");

            // 1. Dati base
            String name = CLIReader.readString("Name of the workplace: ");
            String address = CLIReader.readString("Address of the workplace: ");

            // 2. Selezione Giorni (Multi-selezione)
            List<String> selectedDays = selectOperatingDays();

            // 3. Definizione Fasce Orarie
            List<String> slots = defineTimeSlots();

            // 4. Invio all'App Controller
            try {
                WorkplaceBean newWp = new WorkplaceBean(name,address,selectedDays,slots,SessionContext.getInstance().getLoggeduser().getEmail());
                // Chiamata all'AC (usa il metodo che hai già per JavaFX)
                new CreateWorkplaceAC().createWorkplace(newWp);
                msg = "✅ Workplace '" + name + "' succesfully created!";
                CLIReader.println(msg);
            } catch (DuplicateEntityException e) {
                logger.severe(" Workplace already existing " + e.getMessage() + "\n");
            } catch(DataFetchException e){
                logger.severe(" Error fetching workplaces: " + e.getMessage() + "\n");
            } catch (BaseException e) {
                logger.severe("Generic error: " + e.getMessage() + "\n");
            }
        }


    public  List<String> selectOperatingDays() {
        List<String> selected = new ArrayList<>();
        String[] allDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        CLIReader.println("Select when the workplace is opened by numbers and separated by ',' example: 1,2,3):");
        for (int i = 0; i < allDays.length; i++) {
            msg = (i + 1) + ". " + allDays[i] + "";
            CLIReader.println(msg);
        }

        String input = CLIReader.readString("> ");
        String[] parts = input.split(",");
        for (String p : parts) {
            try {
                int index = Integer.parseInt(p.trim()) - 1;
                if (index >= 0 && index < 7) selected.add(allDays[index]);
            } catch (NumberFormatException e) {
                logger.warning("day not valid: " + p+"\n");
            }
        }
        return selected;

    }
    public  List<String> defineTimeSlots() {
        List<String> slots = new ArrayList<>();
        boolean adding = true;

        CLIReader.println("Select the shifts of the workplace with the following format : HH:mm - HH:mm.");
        while (adding) {
            String start = CLIReader.readString("Start Hour (HH:mm): ");
            String end = CLIReader.readString("End Hour (HH:mm): ");
            try{
                String[] startParts = start.trim().split(":");
                String[] endParts = end.trim().split(":");
                if(startParts.length != 2 || endParts.length != 2){
                    logger.warning("Invalid time format. Be sure to use HH:mm (es: 08:30)\n");
                    continue;
                }
                String startH = startParts[0];
                String startM = startParts[1];
                String endH = endParts[0];
                String endM = endParts[1];

                String formattedShift = new ManageShiftsAC().addShiftstoWorkaplce(startM,startH, endM, endH,slots);
                slots.add(formattedShift);
                msg = "✅ correctly added shift: "+formattedShift;
                CLIReader.println(msg);
            }catch(IllegalArgumentException e){
                logger.warning("Hour error: " +e.getMessage()+"\n");
            }catch(BaseException e) {
                logger.warning("Shit error: " + e.getMessage()+"\n");
            }
            // Formattiamo noi la stringa per essere sicuri del separatore " - "
            String cont = CLIReader.readString("Do you want to add more shifts? (y/n): ");
            if (!cont.equalsIgnoreCase("y")) adding = false;
        }
        Collections.sort(slots);
        return slots;
    }
}
