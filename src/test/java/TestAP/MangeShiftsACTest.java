package TestAP;

import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MangeShiftsACTest {

    @BeforeEach
    void setup() {
        AppConfig.getInstance().setTestMode(true,false);
    }
    @Test
    void testCalcoloWeekIdCorrett(){
        ManageShiftsAC ac = new ManageShiftsAC();
        int offset = 0; //Arrange
        //Act
        String weekId = ac.calculateWeekId(offset);

        //assert
        assertNotNull(weekId,"The week id generated must not be null");
        assertTrue(weekId.matches("\\d{4}_\\d{2}"),"The week id must match the format yyyy-ww");
    }

    @Test
    void testSaveAvailabilities(){
        ManageShiftsAC ac = new ManageShiftsAC();

        IllegalArgumentException ecc = assertThrows(IllegalArgumentException.class, () -> {
            ac.saveAvailabilities(null,null,null,null);
        });
        assertEquals("Bean passed null",ecc.getMessage());
    }

}
