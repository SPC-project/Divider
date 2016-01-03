package fem.divider.figure;

import fem.common.IFemSettings;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fem.divider.RectangleArea;
import fem.geometry.Dot;

public class ArcSegmentGeometry extends AbstractSegmentGeometry {

	private double radius;
	private int centerSide;
	private int arcSide;

	public static final int LEFT_SIDE = 1;
	public static final int RIGHT_SIDE = 2;

	public static final String PARAM_RADIUS = "radius";
	public static final String PARAM_CENTER_SIDE = "centerSide";
	public static final String PARAM_ARC_SIDE = "arcSide";

	public static final String GEOMETRY_TYPE = "arc";

//	private LineSegmentGeometry fallbackGeometry = new LineSegmentGeometry();

	public final double getRadius() {
		return radius;
	}

	public final void setRadius(double radius) {
		this.radius = radius;
	}

	public final int getCenterSide() {
		return centerSide;
	}

	public final void setCenterSide(int centerSide) {
		this.centerSide = centerSide;
	}

	public final int getArcSide() {
		return arcSide;
	}

	public final void setArcSide(int arcSide) {
		this.arcSide = arcSide;
	}

	public double getActualRadius(Segment segment) {
		double dist = segment.getBegin().distance(segment.getEnd());
		double actualRadius = Math.max(radius, dist/2);
		return actualRadius;
	}
	
	public Map<String, String> getParameters() {
		Map<String, String> params = new HashMap<String, String>();

		params.put(PARAM_GEOMETRY_TYPE, GEOMETRY_TYPE);
		params.put(PARAM_RADIUS, Double.toString(radius));
		params.put(PARAM_CENTER_SIDE, Integer.toString(centerSide));
		params.put(PARAM_ARC_SIDE, Integer.toString(arcSide));

		return params;
	}//end getParameters

	public void setParameters(Map<String, String> parameters) {
		String radiusStr = parameters.get(PARAM_RADIUS);
		String centerSideStr = parameters.get(PARAM_CENTER_SIDE);
		String arcSideStr = parameters.get(PARAM_ARC_SIDE);

		// temporary variables
		double radiusNum;
		int centerSideNum;
		int arcSideNum;

		// try to parse. If can't parse one parameter, all parameters
		// not applied

		try {
			radiusNum = Double.parseDouble(radiusStr);
		} catch (NumberFormatException e) {
			System.err.println("Failed to parse floating-point number: "
					+ radiusStr);
			return;
		}

		try {
			centerSideNum = Integer.parseInt(centerSideStr);
		} catch (NumberFormatException e) {
			System.err.println("Failed to parse integer number: "
					+ centerSideStr);
			return;
		}

		try {
			arcSideNum = Integer.parseInt(arcSideStr);
		} catch (NumberFormatException e) {
			System.err.println("Failed to parse integer number: " + arcSideStr);
			return;
		}

		// parse ok, apply parameters
		radius = radiusNum;
		centerSide = centerSideNum;
		arcSide = arcSideNum;
	}//end setParameters

	private Dot getCenter(Segment segment) {
		if(segment.getBegin().distance(segment.getEnd()) < 
				IFemSettings.GENERAL_ACCURACY){
			double offset = centerSide == RIGHT_SIDE ? radius :-radius;			
			return new Dot(segment.getBegin().getX()+offset, segment.getEnd().getY());
		}
		// coordinates of center in relative (to begin->end vector) system
		double actualRadius = getActualRadius(segment);
		double x = segment.getBegin().distance(segment.getEnd()) / 2;
		double y2 = actualRadius * actualRadius - x * x;
		double y = y2>0 ? Math.sqrt(y2) : 0.0;
		if(centerSide == RIGHT_SIDE) y=-y;
		Dot center = new Dot(x, y);
		// translate coordinates to global system
		Dot.restoreCoordinates(segment.getBegin(), segment.getEnd(), center);
		return center;
	}//end getCenter

	/**
	 * Get angle between X direction and vector center->dot
	 * Result may be [0, 2*Pi]
	 * 
	 * @param segment
	 * @param center
	 * @param dot
	 * @return
	 */
	private double getAngle(Dot center, Dot dot) {
		Dot xAxisDot = new Dot(center.getX()+100.0, center.getY());
		return Dot.unifyAngle(Dot.signedAngle(xAxisDot, center, dot));	
	}
	
	private double getBeginAngle(Segment segment, Dot center) {
//		Dot xAxisDot = new Dot(center.getX()+100.0, center.getY());
//		return Dot.signedAngle(xAxisDot, center, segment.getBegin());
		return getAngle(center, segment.getBegin());
	}
	
	private double getEndAngle(Segment segment, Dot center) {
//		Dot xAxisDot = new Dot(center.getX()+100.0, center.getY());
//		return Dot.signedAngle(xAxisDot, center, segment.getEnd());
		return getAngle(center, segment.getEnd());
	}
	
	/**
	 * Get aagle of arc by length of arc
	 * @param segment
	 * @param len
	 * @return
	 */
	private double angleByLength(Segment segment, double len) {
		return len/getActualRadius(segment);
	}
	
	/**
	 * Get length of arc by angle of arc
	 * @param segment
	 * @param angle
	 * @return
	 */
	private double lengthByAngle(Segment segment, double angle) {
		return getActualRadius(segment)*angle;
	}
	
	/**
	 * Get angle of arc, may be from -Pi to +Pi
	 * 
	 * @param segment
	 * @param center
	 * @param beginAngle
	 * @param endAngle
	 * @return angle in radians
	 */
	private double getArcAngle(Segment segment, 
			Dot center, double beginAngle, double endAngle) {
		double arcAngle = endAngle - beginAngle;
		if(arcSide == RIGHT_SIDE && arcAngle<0) {
			arcAngle = Math.PI*2+arcAngle;
		}
		if(arcSide == LEFT_SIDE && arcAngle>0) {
			arcAngle = arcAngle - Math.PI*2;
		}
		if(Math.abs(arcAngle) < IFemSettings.GENERAL_ACCURACY &&
				arcSide == centerSide && centerSide == RIGHT_SIDE) {
			arcAngle = 2*Math.PI;
		}
		if(Math.abs(arcAngle) < IFemSettings.GENERAL_ACCURACY &&
				arcSide == centerSide && centerSide == LEFT_SIDE) {
			arcAngle = -2*Math.PI;
		}
		return arcAngle;
	}
	public void draw(Segment segment, Graphics2D graphics) {
		double actualRadius = getActualRadius(segment);
		FigurePanel panel = segment.getBegin().contour.figure.panel;
		if (panel == null) {
			System.err.println("Can't draw segment --- panel is null"); //$NON-NLS-1$
			return;
		}

		Dot center = getCenter(segment);
		//draw cross at center
		graphics.drawLine(panel.xsi(center.getX()) - 3, 
				panel.ysi(center.getY()), 
				panel.xsi(center.getX()) + 3, 
				panel.ysi(center.getY()));
		graphics.drawLine(panel.xsi(center.getX()), 
				panel.ysi(center.getY()) - 3, 
				panel.xsi(center.getX()), 
				panel.ysi(center.getY()) +3 );		
		
		double beginAngle = getBeginAngle(segment, center);
		double endAngle = getEndAngle(segment, center);
		double arcAngle = getArcAngle(segment, center, beginAngle, endAngle);
		
		graphics.drawArc(panel.xsi(center.getX()) - panel.wsi(actualRadius), 
				panel.ysi(center.getY()) - panel.hsi(actualRadius), 
				panel.wsi(2 * actualRadius), 
				panel.hsi(2 * actualRadius),
				(int)Math.toDegrees(beginAngle),
				(int)Math.toDegrees(arcAngle));

		//Draw czones
		for (Iterator<CZone> i = segment.czones.iterator(); i.hasNext();) {
			CZone czone = i.next();
			drawCzone(segment, czone, graphics, panel);
		}
	}//end draw

	
	public void drawCzone(Segment segment, CZone czone, Graphics2D graphics, FigurePanel panel) {
		Stroke stroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(5.0F));
		double x1 = segment.getBegin().getX();
		double y1 = segment.getBegin().getY();
		double x2 = x1;
		double y2 = y1;

		double offset = czone.getRealOffset();
		double length = czone.getRealLength();
		x1 += segment.shift_x(offset);
		y1 += segment.shift_y(offset);
		x2 += segment.shift_x(offset+length);
		y2 += segment.shift_y(offset+length);

		double actualRadius = getActualRadius(segment);
		Dot center = getCenter(segment);
		double beginAngle = getBeginAngle(segment, center);
		double endAngle = getEndAngle(segment, center);
		double czoneBeginAngle = beginAngle;
		double czoneArcAngle;
		
		if(getArcAngle(segment, center, beginAngle, endAngle) > 0) {
			czoneBeginAngle = beginAngle + angleByLength(segment, offset);
			czoneArcAngle = angleByLength(segment, length);
		} else {
			czoneBeginAngle = beginAngle - angleByLength(segment, offset);
			czoneArcAngle = - angleByLength(segment, length);			
		}

		//draw zone itself
		graphics.drawArc(panel.xsi(center.getX()) - panel.wsi(actualRadius), 
				panel.ysi(center.getY()) - panel.hsi(actualRadius), 
				panel.wsi(2 * actualRadius), 
				panel.hsi(2 * actualRadius),
				(int)Math.toDegrees(czoneBeginAngle),
				(int)Math.toDegrees(czoneArcAngle));
		
		//draw points on zone ends
		graphics.drawOval(panel.xsi(x1) - 2, panel.ysi(y1) - 2, 5, 5);
		graphics.drawOval(panel.xsi(x2) - 2, panel.ysi(y2) - 2, 5, 5);

		graphics.setStroke(stroke);
		drawInfluence(segment, czone, graphics, panel);
	}// end of drawCzone

	public double getLength(Segment segment) {
//		return segment.getBegin().distance(segment.getEnd());
		Dot center = getCenter(segment);
		double beginAngle = getBeginAngle(segment, center);
		double endAngle = getEndAngle(segment, center);
		return lengthByAngle(segment, 
				Math.abs(getArcAngle(segment, center, beginAngle, endAngle)));
	}
	
	public double distance(Segment segment, Dot dot) {
		Dot center = getCenter(segment);
		double beginAngle = getBeginAngle(segment, center);
		double endAngle = getEndAngle(segment, center);
		double pointedAngle = getAngle(center, dot);
		double distance;
		boolean between;
		if(arcSide == RIGHT_SIDE)
			between = Dot.isAngleBetween(beginAngle, endAngle, pointedAngle);
		else
			between = Dot.isAngleBetween(endAngle, beginAngle, pointedAngle);
		if(between) {
			//count distance from arc line
			distance = Math.abs(dot.distance(center) - getActualRadius(segment));
		} else {
			//count distance from ends of segment
			distance = Math.max(dot.distance(segment.getBegin()), 
					dot.distance(segment.getEnd()));
		}
		return distance;
	}//end distance

	public Dot shiftDot(Segment segment, double offset) {
		double actualRadius = getActualRadius(segment);
		Dot center = getCenter(segment);
		double beginAngle = getBeginAngle(segment, center);
		double shiftAngle = angleByLength(segment, offset);
		double theta;
		if(arcSide == RIGHT_SIDE) {
			theta = beginAngle + shiftAngle;
		} else {
			theta = beginAngle - shiftAngle;			
		}
		
		double x = center.getX() + actualRadius*Math.cos(theta) - segment.getBegin().getX();
		double y = center.getY() + actualRadius*Math.sin(theta) - segment.getBegin().getY();
		
		return new Dot(x, y);
	}

	public Dot getDirection(Segment segment, double offset) {
		Dot dot = shiftDot(segment, offset);
		dot.setX( dot.getX() + segment.getBegin().getX() );
		dot.setY( dot.getY() + segment.getBegin().getY() );
		Dot center = getCenter(segment);
		double theta = getAngle(center, dot);
		
		//normale
		double xn = Math.cos(theta);
		double yn = Math.sin(theta);
		//tangent
		double x, y;
		if(arcSide == RIGHT_SIDE) {
			x = -yn;
			y = xn;
		} else {
			x = yn;
			y = -xn;			
		}
		return new Dot(x, y);
	}

	public RectangleArea calculateBounds(Segment segment) {
		Dot center = getCenter(segment);
		double actualRadius = getActualRadius(segment);
		Dot leftTop = new Dot(center.getX()-actualRadius, center.getY()+actualRadius);
		Dot rightBottom = new Dot(center.getX()+actualRadius, center.getY()-actualRadius);
		
		return new RectangleArea(leftTop, rightBottom);	
	}	

	
	private boolean isIntersectionGood(double xrel, double actualRadius, Dot dotRel, Segment segment, Dot center) {
		if(dotRel.getX()>xrel)
			return false;
		double yrel = dotRel.getY();
		Dot intRel = new Dot(xrel, yrel);
		Dot O = new Dot(0.0, 0.0);
		Dot X = new Dot(10.0, 0.0);
		double theta = Dot.unifyAngle(Dot.signedAngle(X, O, intRel));
		double beginAngle = getBeginAngle(segment, center);
		double endAngle = getEndAngle(segment, center);
		boolean between;
		if(arcSide == RIGHT_SIDE)
			between = Dot.isAngleBetween(beginAngle, endAngle, theta);
		else
			between = Dot.isAngleBetween(endAngle, beginAngle, theta);
		return between;
	}
	
	public int countRightXRayIntersections(Segment segment, 
			Dot dot) {
		double actualRadius = getActualRadius(segment);
		Dot center = getCenter(segment);
		Dot dotRel = new Dot(dot.getX()-center.getX(), dot.getY()-center.getY());
		double xrel2 = actualRadius*actualRadius - dotRel.getY()*dotRel.getY();
		//if no intersections
		if(xrel2<0)
			return 0;
		int nInts = 0;
		if(isIntersectionGood(-Math.sqrt(xrel2), actualRadius, dotRel,
				segment, center))
			nInts++;
		if(isIntersectionGood(Math.sqrt(xrel2), actualRadius, dotRel,
				segment, center))
			nInts++;
		return nInts;
	}//end countRightXRayIntersections
	
}//end class ArcSegmentGeometry
