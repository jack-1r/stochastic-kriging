package se.sics.stochastic.helpers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestStatistics {
    
    public TestStatistics() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void testStatistics() {
		Statistics statCal = new Statistics (0);
		statCal.addValue(1);
		statCal.addValue(2);
		statCal.addValue(3);
		System.out.println(statCal.mean());
		System.out.println(statCal.sStdv());
		System.out.println(statCal.getWidth());
		double x = (1/Math.sqrt(3))*2.92;
		System.out.print(x);

	}

}
