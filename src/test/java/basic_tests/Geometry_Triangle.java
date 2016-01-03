package basic_tests;

import static org.junit.Assert.*;
import org.junit.Test;

import fem.geometry.Triangle;
import fem.geometry.Dot;

public class Geometry_Triangle {   
   @Test
   public void test_getArea() {
      Triangle t;
      
      t = new Triangle( new Dot(0.0, 0.0), new Dot(0.0, 5.0), new Dot(5.0, 0.0) );
      assertEquals( "Wrong calculation of regular triangle's (catheti: 5,5)", t.getArea(), 25.0/2.0, DELTA);
      
      t = new Triangle( new Dot(0.0, 0.0), new Dot(0.0, 5.0), new Dot(3.0, 0.0) );
      assertEquals( "Wrong calculation of regular triangle's (catheti: 5,3)", t.getArea(), 15.0/2.0, DELTA);
      
      t = new Triangle( new Dot(0.0, 0.0), new Dot(3.0, 3.0), new Dot(0.0, 3.0) );
      assertEquals( "Wrong calculation of area of " + t, t.getArea(), 4.5, DELTA);
   }
   
   @Test
   public void test_getMaxAngleIndex(){
      Triangle t = new Triangle( new Dot(0.0, 0.0), new Dot(0.0, 5.0), new Dot(5.0, 0.0) );
      assertEquals( t.getMaxAngleIndex(), 0, DELTA );
   }

   private static final double DELTA = 1e-15;
}
