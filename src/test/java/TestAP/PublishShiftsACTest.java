package TestAP;

import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PublishShiftsACTest {

    private static final String WEEK_ID = "2030_10";
    private static final String WORKPLACE_NAME = "LocaleTest";
    private static final String WORKER_EMAIL = "worker@test.com";

    @BeforeEach
    void setup() throws Exception {
        AppConfig.getInstance().setTestMode(true, false);

        clearInMemoryData();

        UserInfo worker = new UserInfo(WORKER_EMAIL, "Mario", "Rossi");
        AppConfig.getInstance().getUserRepository().save(worker);

        Workplace workplace = new Workplace(
                WORKPLACE_NAME,
                "Via Roma 1",
                null,
                null,
                "boss@test.com"
        );
        AppConfig.getInstance().getWorkplaceRepository().saveWorkplace(workplace);

        Membership membership = new Membership(worker, workplace, "WORKER", true);
        AppConfig.getInstance().getMembershipRepository().saveMembership(membership);

        AppConfig.getInstance().getAvailabilityRepository().saveAvailability(
                new Availability(WORKER_EMAIL, WORKPLACE_NAME, "Mon", "08:00", "12:00", WEEK_ID));
        // Il test deve partire sempre da una settimana OPEN.
        AppConfig.getInstance().getWorkplaceRepository()
                .updateWeekStatus(WORKPLACE_NAME, WEEK_ID, "OPEN");
    }

    @Test
    void testPublishNullParameter() {
        PublishShiftsAC controller = new PublishShiftsAC();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> controller.handlePublishAction(null, WEEK_ID));

        assertEquals("Workplace or weekId passed null", exception.getMessage());
    }

    @Test
    void testWeekWithoutAvailability() {
        PublishShiftsAC controller = new PublishShiftsAC();
        WorkplaceBean workplace = createWorkplaceBean();

        ValidationException exception = assertThrows(ValidationException.class, () -> controller.handlePublishAction(workplace, "2099_99"));

        assertTrue(exception.getMessage().contains("2099_99"));
    }

    @Test
    void testPublicationLocksThenPublishesAssignments() throws Exception {
        PublishShiftsAC controller = new PublishShiftsAC();
        WorkplaceBean workplace = createWorkplaceBean();
        String lockResult = controller.handlePublishAction(workplace, WEEK_ID);

        assertEquals("Shifts have Been locked", lockResult);
        assertEquals("LOCKED", AppConfig.getInstance().getWorkplaceRepository().getWeekStatus(WORKPLACE_NAME, WEEK_ID));
        assertTrue(AppConfig.getInstance().getWorkplaceRepository().getUserPublishedShiftsByWeek(WORKER_EMAIL, WEEK_ID).isEmpty());

        String publishResult = controller.handlePublishAction(workplace, WEEK_ID);

        assertEquals("Shifts of " + WORKPLACE_NAME + " has been successfully published.", publishResult);
        assertEquals("PUBLISHED", AppConfig.getInstance().getWorkplaceRepository().getWeekStatus(WORKPLACE_NAME, WEEK_ID)
        );

        Map<String, String> workerAssignments = AppConfig.getInstance().getWorkplaceRepository().getUserPublishedShiftsByWeek(WORKER_EMAIL, WEEK_ID);

        assertEquals(WORKPLACE_NAME, workerAssignments.get("Mon_08:00-12:00"));
    }

    private WorkplaceBean createWorkplaceBean() {
        return new WorkplaceBean(WORKPLACE_NAME, "Via Roma 1", new ArrayList<>(), new ArrayList<>(), "boss@test.com");
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
