package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.Repository;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.ShiftPersistenceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ManageShiftsAC {
    private static final Logger LOGGER = Logger.getLogger(ManageShiftsAC.class.getName());
    Repository repository = AppConfig.getRepository();

    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace,String weekId ) throws DAOException, EntityNotFoundException {
        if(workplace == null||user == null){throw new DAOException("Workplace or User not found");}
        var repo = AppConfig.getRepository();
        Map<String, List<String>> viewMap = new HashMap<>();

        if(repo.findWorkplaceByName(workplace.getWorkplaceName()) == null){throw new EntityNotFoundException("Workplace not found");}

        boolean isBoss = user.getEmail().equals(workplace.getOwnerEmail());

        List<Availability> list;

        if(isBoss){
            list = repo.getAvailabilitiesByWorkplace(workplace.getWorkplaceName());
        }else{
            list = repo.getAvailabilitiesByUser(user.getEmail(),workplace.getWorkplaceName());
        }

        for(Availability a : list){
            String shiftKey = a.getFullShift().replace(" ","");
            String key = a.getDay() + "_" + shiftKey;
            if(isBoss){
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getUserEmail());
            }else{
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add("SELECTED");
            }
        }
        return repo.getAvailabilitiesByWeek(workplace.getWorkplaceName(),weekId);
    }

    public void saveAvailabilities(List<AvailabilityBean> beans) throws ShiftPersistenceException {
        LOGGER.info("salvataggio turni");
        if(beans == null){throw new ShiftPersistenceException("Bean non valido");}

        try {
            var repo = AppConfig.getRepository();

            // Se la lista è vuota, significa che l'utente ha deselezionato tutto
            // ma dobbiamo comunque sapere CHI e DOVE per pulire il database
            if (beans == null) return;

            // Usiamo il SessionContext per sicurezza o i dati del primo bean
            String userEmail = SessionContext.getInstance().getLoggeduser().getEmail();
            String wpName = SessionContext.getInstance().getLoggedWorkplace().getWorkplaceName();

            // 1. PULIZIA: Rimuoviamo le vecchie disponibilità per evitare duplicati
            // Questo è fondamentale perché se l'utente cambia idea, il DB deve riflettere l'ultima scelta
            repo.deleteAvailabilitiesByUser(userEmail, wpName);

            // 2. SALVATAGGIO: Trasformiamo i Bean in Entity e le salviamo nel Repository
            for (AvailabilityBean bean : beans) {
                LOGGER.info("LOG AC: Salvataggio per " + bean.getUserEmail() + " al " + bean.getDay());
                Availability entity = new Availability(
                        bean.getUserEmail(),
                        bean.getWorkplaceName(),
                        bean.getDay(),
                        bean.getStartShift(),
                        bean.getEndShifts(),
                        bean.getWeekId()// Assicurati che nel Bean si chiami shift o timeSlot come nella Entity
                );
                repo.saveAvailability(entity);
            }
        }catch(DAOException _){
            throw new ShiftPersistenceException("Errire tecnico durante salvataggio disponibilità");
        }
    }

    public String getWeekStatusShifts(String workplaceName, String weekId) {
        // Chiamata al repository (sia esso in memoria o DB)
        String status = repository.getWeekStatus(workplaceName, weekId);

        // Logica di fallback: se il repository restituisce null, la settimana è nuova/aperta
        return (status != null) ? status : "OPEN";
    }

    public void updateWeekStatusShifts(String workplaceName,String weekId, String status){
        repository.updateWeekStatus(workplaceName,weekId,status);
    }
}
