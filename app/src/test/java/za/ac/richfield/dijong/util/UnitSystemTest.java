package za.ac.richfield.dijong.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitSystemTest {

    @Test
    public void metric_leavesAmountsAlone() {
        UnitSystem.Amount amount = UnitSystem.forDisplay(500, "g", false);
        assertEquals(500, amount.quantity, 0.0001);
        assertEquals("g", amount.unit);
    }

    @Test
    public void imperial_convertsGramsToOunces() {
        UnitSystem.Amount amount = UnitSystem.forDisplay(250, "g", true);
        assertEquals(8.8, amount.quantity, 0.0001);
        assertEquals("oz", amount.unit);
    }

    @Test
    public void imperial_usesPoundsFromOnePoundUp() {
        UnitSystem.Amount amount = UnitSystem.forDisplay(1, "kg", true);
        assertEquals(2.2, amount.quantity, 0.0001);
        assertEquals("lb", amount.unit);
    }

    @Test
    public void imperial_convertsMillilitresAndLitresToFluidOunces() {
        assertEquals("fl oz", UnitSystem.forDisplay(250, "ml", true).unit);
        assertEquals(8.5, UnitSystem.forDisplay(250, "ml", true).quantity, 0.0001);
        assertEquals(33.8, UnitSystem.forDisplay(1, "L", true).quantity, 0.0001);
    }

    @Test
    public void imperial_leavesSpoonsCupsAndCountsAlone() {
        assertEquals("tbsp", UnitSystem.forDisplay(2, "tbsp", true).unit);
        assertEquals("cup", UnitSystem.forDisplay(1, "cup", true).unit);
        assertEquals("can", UnitSystem.forDisplay(1, "can", true).unit);
    }

    @Test
    public void metric_showsSmallKilogramsAndLitresInGramsAndMillilitres() {
        UnitSystem.Amount grams = UnitSystem.forDisplay(0.2, "kg", false);
        assertEquals(200, grams.quantity, 0.0001);
        assertEquals("g", grams.unit);

        UnitSystem.Amount millilitres = UnitSystem.forDisplay(0.25, "L", false);
        assertEquals(250, millilitres.quantity, 0.0001);
        assertEquals("ml", millilitres.unit);

        assertEquals("kg", UnitSystem.forDisplay(1.5, "kg", false).unit);
    }
}
