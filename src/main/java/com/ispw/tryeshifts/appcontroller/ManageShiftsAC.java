package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.ShiftPersistenceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageShiftsAC {
    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace) throws DAOException, EntityNotFoundException {
        if(workplace == null||user == null){throw new DAOException("Workplace or User not found");}
        //System.out.println("CHECK RUOLO: Utente Loggato=" + user.getEmail() + " | Owner WP=" + workplace.getOwnerEmail());
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
        return viewMap;
    }

    public void saveAvailabilities(List<AvailabilityBean> beans) throws ShiftPersistenceException {
        System.out.println("LOG AC: Ricevuti " + beans.size() + " bean da salvare");
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
                System.out.println("LOG AC: Salvataggio per " + bean.getUserEmail() + " al " + bean.getDay());
                Availability entity = new Availability(
                        bean.getUserEmail(),
                        bean.getWorkplaceName(),
                        bean.getDay(),
                        bean.getStartShift(),
                        bean.getEndShift()// Assicurati che nel Bean si chiami shift o timeSlot come nella Entity
                );
                repo.saveAvailability(entity);
            }
        }catch(DAOException e){
            throw new ShiftPersistenceException("Errire tecnico durante salvataggio disponibilità");
        }
    }
}
