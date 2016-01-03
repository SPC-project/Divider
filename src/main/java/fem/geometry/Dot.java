/*
 * Dot.java
 *
 * Created on Tuesday, 22, March 2005, 14:27
 */

package fem.geometry;

import fem.common.IFemSettings;

/**
 * It's a dot on a plane Superclass of Node
 */
public class Dot {
	protected double y;
	protected double x;
	protected DotMaterial material;

	/**
	 * Creates new Dot with specified coordinates
	 */
	public Dot(double x_, double y_) {
		x = x_;
		y = y_;
		material = DotMaterial.AIR;
	}
	public Dot(double x_, double y_, DotMaterial material_) {
		this(x_, y_);
		material = material_;
	}

	/**
	 * Creates new Dot with coordinates taken from _dot
	 */
	public Dot(Dot _dot) {
		this(_dot.x, _dot.y, _dot.material);
	}

	/**
	 * Calculate distance from this dot to given one
	 */
	public double distance(Dot dot_) {
		return Math.sqrt((dot_.x - x) * (dot_.x - x) + (dot_.y - y)
				* (dot_.y - y));
	}

	/**
	 * Calculate distance from this dot to (x_; y_)
	 */
	public double distance(double x_, double y_) {
		return Math.sqrt((x_ - x) * (x_ - x) + (y_ - y) * (y_ - y));
	}

	/**
	 * Change dot coordinates to given values
	 * 
	 * @param x_ -
	 *            x-coordinate
	 * @param y_ -
	 *            y-coordinate
	 */
	public void setCoordinates(double x_, double y_) {
		x = x_;
		y = y_;
	}
	
	public boolean isFigure(){
		return material == DotMaterial.FIGURE;
	}
	/**
	 * Change dot coordinates to those from given value
	 * 
	 * @param _dot -
	 *            coordinates will be taken from this dot
	 */
	public void setCoordinates(Dot _dot) {
		setCoordinates(_dot.x, _dot.y);
	}

	/**
	 * @return angle (in radians), that has corner c and dots in sides s1 and s2
	 *         Returned angle is between 0 and Pi
	 */
	public static double angle(fem.geometry.Dot s1, fem.geometry.Dot c, fem.geometry.Dot s2) {
		double B = c.distance(s1);
		double C = c.distance(s2);
		double A = s1.distance(s2);
		double cos_alpha = (B * B + C * C - A * A) / (2 * B * C);
		return Math.acos(cos_alpha);
	}

	/**
	 * @return angle (in radians), that has corner c and dots in sides s1 and s2
	 *         (angle between vectors c-s1 and s2-c) Returned angle is between
	 *         -Pi and +Pi
	 */
	public static double signedAngle(fem.geometry.Dot s1, fem.geometry.Dot c,
			fem.geometry.Dot s2) {
//		double x1 = c.x - s1.x; // first vector coords
//		double y1 = c.y - s1.y;
		double x1 = s1.x - c.x; // first vector coords
		double y1 = s1.y - c.y;
		double x1_ = -y1; // first vector's left perpendicular coordinates
		double y1_ = x1;
		double x2 = s2.x - c.x; // second vector coords
		double y2 = s2.y - c.y;

		double cos_alpha = (x1 * x2 + y1 * y2)
				/ Math.sqrt((x1 * x1 + y1 * y1) * (x2 * x2 + y2 * y2));
		double aux_mul = x1_ * x2 + y1_ * y2;
		double sign = 1;
		if (aux_mul < 0)
			sign = -1;

		return Math.acos(cos_alpha) * sign;
	}


	/**
	 * Returns angle, that has corner this and dots in sides s1 and s2
	 */
	public double angle(Dot s1, Dot s2) {
		return angle(s1, this, s2);
	}

	/**
	 * Returns angle, between vedtors this-s1 and s2-this
	 */
	public double signedAngle(Dot s1, Dot s2) {
		return signedAngle(s1, this, s2);
	}

	/**
	 * Returns angle from 0 to 2*Pi that is eqal to given 
	 * 
	 * @param angle in radians
	 * @return unified angle
	 */
	public static double unifyAngle(double angle) {
		double circles = Math.floor(Math.abs(angle/(2*Math.PI)));
		if(angle>0) {
			return angle-circles*2*Math.PI;
		} else{
			return angle+(circles+1)*2*Math.PI;
		}
	}
	
	/**
	 * Return true if checkedAngle lies in arc, drawn in counter-clockwise
	 * direction from angle1 to angle2
	 * @param angle1
	 * @param angle2
	 * @param checkedAngle
	 * @return
	 */
	public static boolean isAngleBetween(double angle1, double angle2, 
			double checkedAngle) {
		angle1 = Dot.unifyAngle(angle1);
		angle2 = Dot.unifyAngle(angle2);
		checkedAngle = Dot.unifyAngle(checkedAngle);
		//TODO: write this method
		if(angle2 > angle1) {
			if( angle1<=checkedAngle && checkedAngle<=angle2 ) {
				return true;
			}
		} else {
			if( angle1<=checkedAngle || checkedAngle<=angle2 ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Static method. Turns specified vector counter clockwise
	 * 
	 * @param vec[] --
	 *            x and y coordinate of vector (initial as well as result)
	 * @param angle
	 *            --- vector will be turned by this angle (in radians)
	 */
	public static void turnVector(double vec[], double angle) {
		double x = vec[0], y = vec[1];
		vec[0] = x * Math.cos(angle) - y * Math.sin(angle);
		vec[1] = x * Math.sin(angle) + y * Math.cos(angle);
	}

	/**
	 * Static method Find intersection of two lines, specified by their begin
	 * and end
	 * 
	 * @return intersection place on success, null if they are not intersected
	 *         (parallel or are one line)
	 */
	public static Dot linesIntersection(Dot begin1, Dot end1, Dot begin2,
			Dot end2) {
		double a11, a12, a21, a22, b1, b2, d0, dx, dy;

		a11 = end1.getY() - begin1.getY();
		a12 = -end1.getX() + begin1.getX();
		b1 = begin1.getX() * (end1.getY() - begin1.getY()) + begin1.getY()
				* (-end1.getX() + begin1.getX());

		a21 = end2.getY() - begin2.getY();
		a22 = -end2.getX() + begin2.getX();
		b2 = begin2.getX() * (end2.getY() - begin2.getY()) + begin2.getY()
				* (-end2.getX() + begin2.getX());

		d0 = a11 * a22 - a12 * a21;
		if (Math.abs(d0) < IFemSettings.GENERAL_ACCURACY)
			return null;
		else {
			dx = b1 * a22 - a12 * b2;
			dy = a11 * b2 - b1 * a21;
			return new Dot(dx / d0, dy / d0);
		}
	}// end lineIntersections(...)

	/**
	 * Static method Find out if dot is inside rectangle, specified by 2 corners
	 */
	public static boolean isInsideRectangle(Dot dot, Dot corner1, Dot corner2) {
		double minX = Math.min(corner1.x, corner2.x);
		double maxX = Math.max(corner1.x, corner2.x);
		double minY = Math.min(corner1.y, corner2.y);
		double maxY = Math.max(corner1.y, corner2.y);

		if (minX - IFemSettings.GENERAL_ACCURACY < dot.x
				&& dot.x < maxX + IFemSettings.GENERAL_ACCURACY
				&& minY - IFemSettings.GENERAL_ACCURACY < dot.y
				&& dot.y < maxY + IFemSettings.GENERAL_ACCURACY)
			return true;
		else
			return false;
	}

	/**
	 * Use line begin-end as X-axis of new coordinate system and set coords ot
	 * 'translated' to those from new coordinate system. Notice that it modifies
	 * 'translated' argument
	 */
	public static void translateCoordinates(Dot begin, Dot end, Dot translated) {
//		double a_length = begin.distance(end);
//		double a_x = end.x - begin.x;
//		double a_y = end.y - begin.y;
//
//		double sin_alpha = a_y / a_length;
//		double cos_alpha = a_x / a_length;
//		double x1, y1;
//		translated.x -= begin.x;
//		translated.y -= begin.y;
//		x1 = translated.x * cos_alpha + translated.y * sin_alpha;
//		y1 = -translated.x * sin_alpha + translated.y * cos_alpha;
//		translated.setX(x1);
//		translated.setY(y1);
            
		translated.x -= begin.x;
		translated.y -= begin.y;
            
                translateCoordinatesOnlyRotation(begin, end, translated);
	}// end translateCoordinates(...)


	public static void translateCoordinatesOnlyRotation(Dot begin, Dot end, Dot translated) {
		double a_length = begin.distance(end);
		double a_x = end.x - begin.x;
		double a_y = end.y - begin.y;

		double sin_alpha = a_y / a_length;
		double cos_alpha = a_x / a_length;
		double x1, y1;
                
		x1 = translated.x * cos_alpha + translated.y * sin_alpha;
		y1 = -translated.x * sin_alpha + translated.y * cos_alpha;
		translated.setX(x1);
		translated.setY(y1);
	}        
        
	/**
	 * Inverse to translateCoordinates: takes coordinates of translated dot as
	 * coordinates in local system (begin-->end vector is X axis), and
	 * translates those coordinates to global
	 * 
	 * @param begin
	 * @param end
	 * @param translated
	 */
	public static void restoreCoordinates(Dot begin, Dot end, Dot translated) {
//		double a_length = begin.distance(end);
//		double a_x = end.x - begin.x;
//		double a_y = end.y - begin.y;
//
//		double sin_alpha = a_y / a_length;
//		double cos_alpha = a_x / a_length;
//		double x1, y1;
//		x1 = translated.x * cos_alpha - translated.y * sin_alpha;
//		y1 = translated.x * sin_alpha + translated.y * cos_alpha;
//		translated.setX(x1);
//		translated.setY(y1);
                
            restoreCoordinatesOnlyRotation(begin, end, translated);
            
		translated.x += begin.x;
		translated.y += begin.y;
	}

	public static void restoreCoordinatesOnlyRotation(Dot begin, Dot end, Dot translated) {
		double a_length = begin.distance(end);
		double a_x = end.x - begin.x;
		double a_y = end.y - begin.y;

		double sin_alpha = a_y / a_length;
		double cos_alpha = a_x / a_length;
		double x1, y1;
		x1 = translated.x * cos_alpha - translated.y * sin_alpha;
		y1 = translated.x * sin_alpha + translated.y * cos_alpha;
		translated.setX(x1);
		translated.setY(y1);
	}//end method restoreCoordinatesOnlyRotation
        
        @Override
	public String toString() {
		return "(" + x + ";" + y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return x coordinate of the dot
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return y coordinate of the dot
	 */
	public double getY() {
		return y;
	}

	public DotMaterial getMaterial() {
		return material;
	}
	
	/**
	 * @param d -
	 *            value, that will be x coordinate of the dot
	 */
	public void setX(double d) {
		x = d;
	}

	/**
	 * @param d -
	 *            value, that wil be y coordinate of the dot
	 */
	public void setY(double d) {
		y = d;
	}
	
	/**
	 * Change material of this dot
	 * @param material_ - new material
	 */
	public void setMaterial(DotMaterial material_){
		material = material_;
	}
}
