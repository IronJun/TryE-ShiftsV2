package TestAP;

import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PublishShiftsACTest {
    private final String validWeek = "2026_10";

    @BeforeEach
    void setup(){
        AppConfig.getInstance().setTestMode(true,false);


        try{
            UserInfo fakeWorker = AppConfig.getInstance().getUserRepository().findByEmail("worker@test.com");
            if(fakeWorker==null){
                fakeWorker = new UserInfo("worker@test.com","Mario","Rossi");
                AppConfig.getInstance().getUserRepository().save(fakeWorker);
            }

            Workplace fakeWp;
            if(!AppConfig.getInstance().getWorkplaceRepository().existsWorkplaceByName("LocaleTest")) {
                fakeWp = new Workplace("LocaleTest", "Via Roma 1", null, null, "boss@test.com");
                AppConfig.getInstance().getWorkplaceRepository().saveWorkplace(fakeWp);
            } else {
                fakeWp = AppConfig.getInstance().getWorkplaceRepository().findWorkplaceByName("LocaleTest");
            }

            if(AppConfig.getInstance().getMembershipRepository().findMembership("worker@test.com","LocaleTest")==null){
                Membership fakeMem = new Membership(fakeWorker,fakeWp,"WORKER",true);
                AppConfig.getInstance().getMembershipRepository().saveMembership(fakeMem);
            }

            // Usa .isEmpty() perché il DAO restituisce una Mappa, non null!
            if(AppConfig.getInstance().getAvailabilityRepository().getAvailabilitiesByWeek("LocaleTest", validWeek).isEmpty()){
                Availability fakeAvail = new Availability("worker@test.com","LocaleTest","Mon","08:00","12:00", validWeek);
                AppConfig.getInstance().getAvailabilityRepository().saveAvailability(fakeAvail);
            }



        }catch(Exception e){
            fail("Error during the memory database setup: "+e.getMessage());
        }
    }

    @Test
    void testPublishNullParameter(){
        PublishShiftsAC controller = new PublishShiftsAC();
        NullPointerException npe = assertThrows(NullPointerException.class, ()->{
            controller.publish(null, validWeek);
        });
        assertEquals("Workplace or weekId passed null",npe.getMessage());
    }

    @Test
    void testWeekWithoutAvailability(){
        String emptyweek = "2099_99";
        PublishShiftsAC controller = new PublishShiftsAC();
        WorkplaceBean validwpBean = new WorkplaceBean("LocaleTest","Via Roma 1",new ArrayList<>(),new ArrayList<>(),"boss@test.com");

        ValidationException ecception = assertThrows(ValidationException.class, ()->{
            controller.publish(validwpBean,emptyweek);
        });
        assertTrue(ecception.getMessage().contains(emptyweek));
    }

    @Test
    void testPublishSuccess(){
        PublishShiftsAC controller = new PublishShiftsAC();
        WorkplaceBean validwpBean = new WorkplaceBean("LocaleTest","Via Roma 1",new ArrayList<>(),new ArrayList<>(),"boss@test.com");


        try {
            AppConfig.getInstance().getAvailabilityRepository().getAvailabilitiesByWeek(validwpBean.getWorkplaceName(), validWeek);
        } catch (Exception e) {
            fail("Error during the uploading of availabilities: "+e.getMessage());
        }
        assertDoesNotThrow(()->{
            controller.publish(validwpBean, validWeek);
        },"The publication should end well with valid data");
    }
}
