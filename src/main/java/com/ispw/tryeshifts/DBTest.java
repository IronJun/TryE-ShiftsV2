package com.ispw.tryeshifts;

import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBTest {
    public static void main(String[] args) {
        JDBC jdbc = new JDBC();

        // Dati per il test
        String emailDipendente = "test@test.com";
        String emailOwner = "simoneferretti2003@gmail.com";
        String nomeWP = "Officina_Test_" + System.currentTimeMillis();

        try {
            System.out.println("--- INIZIO TEST MEMBERSHIP ---");

            // 1. PREPARAZIONE: Creiamo il Workplace
            Workplace wp = new Workplace(nomeWP, "Via Test 123", new ArrayList<>(), new ArrayList<>(), emailOwner);
            jdbc.saveWorkplace(wp);
            System.out.println("> Workplace creato: " + nomeWP);

            // 2. TEST SAVE: Creiamo una Membership non accettata (is_accepted = false)
            UserInfo dipendente = new UserInfo(emailDipendente, "Mario", "Rossi");
            Membership m = new Membership(dipendente, wp, "Meccanico", false);

            System.out.println("> Invio richiesta di Membership...");
            jdbc.saveMembership(m);

            // 3. TEST PENDING REQUESTS: Il proprietario controlla le richieste
            System.out.println("\n> Test: getPendingRequestsForOwner(" + emailOwner + ")");
            List<Membership> pendenti = jdbc.getPendingRequestsForOwner(emailOwner);
            System.out.println("Richieste pendenti trovate: " + pendenti.size());
            for(Membership p : pendenti) {
                System.out.println(" - Utente: " + p.getUser().getEmail() + " per WP: " + p.getWorkplace().getName());
            }

            // 4. TEST UPDATE: Accettiamo la Membership
            System.out.println("\n> Accettazione Membership in corso...");
            m.setAccepted(true);
            jdbc.updateMembership(m);

            // 5. TEST IS MEMBER: Verifichiamo se l'utente è ora un membro effettivo
            boolean isMember = jdbc.isUserMemberOf(emailDipendente, nomeWP);
            System.out.println("> L'utente è membro ufficiale? " + isMember);

            // 6. TEST FIND ALL MEMBERS: Recuperiamo tutti i membri del workplace
            List<Membership> membri = jdbc.getMembershipsByWorkplace(nomeWP);
            System.out.println("> Numero totale membri nel WP: " + membri.size());

            System.out.println("\n--- TEST MEMBERSHIP COMPLETATO CON SUCCESSO ---");

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE IL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void TestWorkplace(){
        JDBC jdbc = new JDBC();
        String testEmail = "simoneferretti2003@gmail.com"; // Assicurati che esista nel DB
        String nomeWP = "Test Lab " + System.currentTimeMillis(); // Nome univoco per ogni test

        try {
            System.out.println("--- INIZIO TEST COMPLETO WORKPLACE ---");

            // 1. PREPARAZIONE E SALVATAGGIO
            List<String> giorni = Arrays.asList("Lunedì", "Martedì");
            List<String> turni = Arrays.asList("09:00-13:00", "14:00-18:00");
            Workplace wp = new Workplace(nomeWP, "Via delle prove 1", giorni, turni, testEmail);

            System.out.println("> Salvataggio nuovo Workplace...");
            jdbc.saveWorkplace(wp);

            // 2. TEST: findWorkplacesbyEmail
            System.out.println("\n> Test: findWorkplacesbyEmail(" + testEmail + ")");
            List<Workplace> listaPerEmail = jdbc.findWorkplacesbyEmail(testEmail);
            System.out.println("Trovati: " + listaPerEmail.size());
            for (Workplace w : listaPerEmail) {
                System.out.println(" - Nome: " + w.getName() + " | Giorni: " + w.getSelectedDays());
            }

            // 3. TEST: findWorkplacesByName (Ricerca Parziale)
            System.out.println("\n> Test: findWorkplacesByName('Test')");
            List<Workplace> listaRicerca = jdbc.findWorkplacesByName("Test");
            System.out.println("Corrispondenze trovate: " + listaRicerca.size());

            // 4. TEST: findAllWorkplaces
            System.out.println("\n> Test: findAllWorkplaces()");
            List<Workplace> tutti = jdbc.findAllWorkplaces();
            System.out.println("Totale workplace nel sistema: " + tutti.size());

            System.out.println("\n--- TEST COMPLETATO CON SUCCESSO ---");

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE IL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void TestAvailability(){
        JDBC jdbc = new JDBC();

        // Dati necessari per il test (assicurati che esistano nel DB!)
        String emailTest = "simoneferretti2003@gmail.com";
        String nomeWorkplaceTest = "TestLab_" + System.currentTimeMillis(); // Deve esistere nella tabella workplaces

        try {
            System.out.println("--- INIZIO TEST AVAILABILITY ---");

            // 1. TEST SAVE
            // Creiamo una disponibilità: Lunedì dalle 08:00 alle 14:00
            Availability disp = new Availability(emailTest, nomeWorkplaceTest, "Lunedì", "08:00", "14:00");

            System.out.println("> Salvataggio disponibilità...");
            jdbc.saveAvailability(disp);

            // 2. TEST GET BY WORKPLACE
            System.out.println("\n> Test: getAvailabilitiesByWorkplace(" + nomeWorkplaceTest + ")");
            List<Availability> listaWp = jdbc.getAvailabilitiesByWorkplace(nomeWorkplaceTest);
            System.out.println("Disponibilità trovate per questo workplace: " + listaWp.size());
            for (Availability a : listaWp) {
                System.out.println(" - Utente: " + a.getUserEmail() + " | Giorno: " + a.getDay() + " | Orario: " + a.getFullShift());
            }

            // 3. TEST DELETE
            System.out.println("\n> Test: deleteAvailabilitiesByUser per " + emailTest);
            jdbc.deleteAvailabilitiesByUser(emailTest, nomeWorkplaceTest);

            // 4. VERIFICA FINALE
            List<Availability> listaDopoEliminazione = jdbc.getAvailabilitiesByWorkplace(nomeWorkplaceTest);
            System.out.println("Disponibilità dopo eliminazione (dovrebbe essere 0): " + listaDopoEliminazione.size());

            if (listaDopoEliminazione.isEmpty()) {
                System.out.println("\n--- TEST AVAILABILITY COMPLETATO CON SUCCESSO ---");
            } else {
                System.out.println("\n--- TEST FALLITO: La disponibilità non è stata eliminata ---");
            }

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE IL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
