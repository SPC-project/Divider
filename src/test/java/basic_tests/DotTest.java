package basic_tests;

import static org.junit.Assert.*;

import org.junit.Test;

import fem.geometry.Dot;
import fem.geometry.DotMaterial;

public class DotTest {	
	@Test
	public void testConstructors(){
		Dot dot = new Dot( 25.0, 11.0 );
		assertEquals("Dot's 'double-double' constructor failed on x-test", dot.getX(), 25.0, DELTA);
		assertEquals("Dot's 'double-double' constructor failed on y-test", dot.getY(), 11.0, DELTA);
		assertEquals("Dot's 'double-double' constructor failed on material-test", dot.getMaterial().getValue(), DotMaterial.AIR.getValue());
	
		dot = new Dot( 25.0, 11.0, DotMaterial.FIGURE );
		assertEquals("Dot's 'double-double-material' constructor failed on x-test", dot.getX(), 25.0, DELTA);
		assertEquals("Dot's 'double-double-material' constructor failed on y-test", dot.getY(), 11.0, DELTA);
		assertEquals("Dot's 'double-double-material' constructor failed on material-test", dot.getMaterial().getValue(), DotMaterial.FIGURE.getValue());
		
		Dot dot2 = new Dot(dot);
		assertEquals("Dot's 'dot' constructor failed on x-test", dot2.getX(), 25.0, DELTA);
		assertEquals("Dot's 'dot' constructor failed on y-test", dot2.getY(), 11.0, DELTA);
		assertEquals("Dot's 'dot' constructor failed on material-test", dot2.getMaterial().getValue(), DotMaterial.FIGURE.getValue());
	}
	
	private static final double DELTA = 1e-15;
}
