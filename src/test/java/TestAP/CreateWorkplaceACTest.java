package TestAP;

import com.ispw.tryeshifts.appcontroller.CreateWorkplaceAC;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CreateWorkplaceACTest {

    @BeforeEach
    void setup() {
        AppConfig.setTestMode(true,false);
        try{
            if(AppConfig.getUserRepository().findByEmail("boss@test.com")==null){
                UserInfo fakeBoss = new UserInfo("boss@test.com","Mario","Rossi");
                AppConfig.getUserRepository().save(fakeBoss);
            }

            if(!AppConfig.getWorkplaceRepository().existsWorkplaceByName("LocaleEsistente")) {
                Workplace wp = new Workplace("LocaleEsistente", "Via Roma 1", null, null, "boss@test.com");
                AppConfig.getWorkplaceRepository().saveWorkplace(wp);
            }
        }catch (Exception e){
            fail("Error during the test's setup: "+e.getMessage());
        }
    }


    @Test
    void testCreateWorkplacNameNull(){
        WorkplaceBean emptyNameBean = new WorkplaceBean("","Via Roma 1", null,null,"boss@test.com");
        CreateWorkplaceAC createWorkplaceAC = new CreateWorkplaceAC();

        NullPointerException ecc = assertThrows(NullPointerException.class, ()->{
            createWorkplaceAC.createWorkplace(emptyNameBean);
        });

        assertEquals("Workplace name cannot be empty", ecc.getMessage());
    }

    @Test
    void testCreateDuplicateWorkplace(){
        WorkplaceBean wp = new WorkplaceBean("LocaleEsistente", "Via Milano 12", null, null, "boss@test.com");
        CreateWorkplaceAC createWorkplaceAC = new CreateWorkplaceAC();

        DuplicateEntityException ecc = assertThrows(DuplicateEntityException.class, ()->{
            createWorkplaceAC.createWorkplace(wp);
        });
        assertTrue(ecc.getMessage().contains("LocaleEsistente"));
    }
}
