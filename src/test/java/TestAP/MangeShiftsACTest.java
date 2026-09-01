package TestAP;

import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MangeShiftsACTest {

    private static final String WEEK_ID = "2030_10";
    private static final String USER_EMAIL = "worker@test.com";
    private static final String WORKPLACE_A = "WorkplaceA";
    private static final String WORKPLACE_B = "WorkplaceB";

    private ManageShiftsAC controller;
    private UserBean worker;
    private WorkplaceBean workplaceA;
    private WorkplaceBean workplaceB;

    @BeforeEach
    void setup() throws Exception {
        AppConfig.getInstance().setTestMode(true, false);
        clearInMemoryData();

        controller = new ManageShiftsAC();
        worker = new UserBean(USER_EMAIL, "Mario", "Rossi");

        workplaceA = new WorkplaceBean(WORKPLACE_A, "Via Roma 1", List.of(), List.of(), "bossA@test.com");
        workplaceB = new WorkplaceBean(WORKPLACE_B, "Via Milano 2", List.of(), List.of(), "bossB@test.com");

        AppConfig.getInstance().getWorkplaceRepository().saveWorkplace(
                new Workplace(WORKPLACE_A, "Via Roma 1", null, null, "bossA@test.com")
        );
        AppConfig.getInstance().getWorkplaceRepository().saveWorkplace(
                new Workplace(WORKPLACE_B, "Via Milano 2", null, null, "bossB@test.com")
        );

        AppConfig.getInstance().getWorkplaceRepository().updateWeekStatus(WORKPLACE_A, WEEK_ID, "OPEN");
        AppConfig.getInstance().getWorkplaceRepository().updateWeekStatus(WORKPLACE_B, WEEK_ID, "OPEN");
    }

    @Test
    void testCalcoloWeekIdCorrect() {
        String weekId = controller.calculateWeekId(0);

        assertNotNull(weekId);
        assertTrue(weekId.matches("\\d{4}_\\d{2}"));
    }

    @Test
    void testSaveAvailabilitiesWithNullList() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> controller.saveAvailabilities(null, null, null, null));

        assertEquals("Bean passed null", exception.getMessage());
    }

    @Test
    void testSavingAvailabilitiesReplacesPreviousOnes() throws Exception {
        List<AvailabilityBean> firstSelection = List.of(
                availabilityFor(WORKPLACE_A, "Mon", "08:00", "12:00"),
                availabilityFor(WORKPLACE_A, "Tue", "09:00", "13:00")
        );

        controller.saveAvailabilities(firstSelection, worker, workplaceA, WEEK_ID);

        assertEquals(2, AppConfig.getInstance().getAvailabilityRepository()
                        .getAvailabilitiesByUser(USER_EMAIL, WORKPLACE_A, WEEK_ID)
                        .size()
        );

        List<AvailabilityBean> newSelection = List.of(availabilityFor(WORKPLACE_A, "Wed", "14:00", "18:00"));

        controller.saveAvailabilities(newSelection, worker, workplaceA, WEEK_ID);

        List<Availability> saved =
                AppConfig.getInstance().getAvailabilityRepository()
                        .getAvailabilitiesByUser(USER_EMAIL, WORKPLACE_A, WEEK_ID);

        assertEquals(1, saved.size());
        assertEquals("Wed", saved.getFirst().getDay());
        assertEquals("14:00", saved.getFirst().getStartShift());
        assertEquals("18:00", saved.getFirst().getEndShift());
    }

    @Test
    void testOverlappingAvailabilityInAnotherWorkplaceIsRejected() throws Exception {
        controller.saveAvailabilities(
                List.of(availabilityFor(WORKPLACE_A, "Mon", "09:00", "13:00")),
                worker,
                workplaceA,
                WEEK_ID
        );

        ValidationException exception = assertThrows(
                ValidationException.class, () -> controller.saveAvailabilities(
                        List.of(availabilityFor(WORKPLACE_B, "Mon", "10:00", "12:00")),
                        worker,
                        workplaceB,
                        WEEK_ID
                )
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void testConsecutiveAvailabilityInAnotherWorkplaceIsAccepted() throws Exception {
        controller.saveAvailabilities(
                List.of(availabilityFor(WORKPLACE_A, "Mon", "09:00", "13:00")),
                worker,
                workplaceA,
                WEEK_ID
        );

        assertDoesNotThrow(() -> controller.saveAvailabilities(List.of(availabilityFor(WORKPLACE_B, "Mon", "13:00", "17:00")),
                        worker,
                        workplaceB,
                        WEEK_ID
                )
        );
    }

    private AvailabilityBean availabilityFor(
            String workplaceName,
            String day,
            String start,
            String end
    ) {
        return new AvailabilityBean(
                USER_EMAIL,
                workplaceName,
                day,
                start,
                end,
                WEEK_ID
        );
    }

    private void clearInMemoryData() {
        InMemory memory = InMemory.getInstance();
        memory.getUsers().clear();
        memory.getWorkplaces().clear();
        memory.getMemberships().clear();
        memory.getAvailabilities().clear();
        memory.getWeekStatusDbDemo().clear();
        memory.getPublishedShifts().clear();
    }
}
