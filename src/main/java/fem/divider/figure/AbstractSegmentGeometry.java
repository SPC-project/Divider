package fem.divider.figure;

import fem.common.IFemSettings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

import fem.divider.Divider;
import fem.divider.RectangleArea;
import fem.geometry.Dot;

abstract public class AbstractSegmentGeometry {

	public static final String PARAM_GEOMETRY_TYPE = "type";
	
	/**
	 * Get parameters as map of strings.
	 * @return
	 */
	public Map<String, String> getParameters() {
		return null;
	}
	
	/**
	 * Set parameters from map of strings
	 * @param parameters
	 */
	public void setParameters(Map<String, String> parameters) {
	}

	abstract public double getLength(Segment segment);

	abstract public Dot shiftDot(Segment segment, double offset);

	/**
	 * get distance from segment to point
	 */
	abstract public double distance(Segment segment, Dot dot);
	
	/**
	 * Draw segment, its contact zones and influences
	 * @param segment
	 * @param graphics
	 */
	abstract public void draw(Segment segment, Graphics2D graphics); 


	protected void drawInfluence(Segment segment, CZone czone, Graphics2D graphics,
			FigurePanel panel) {
		if (czone.getInfluenceMode() == CZone.INFLUENCE_NONE)
			return;
		double x0 = segment.getBegin().getX();
		double y0 = segment.getBegin().getY();
		double x1 = x0;
		double y1 = y0;
		double x2 = segment.getEnd().getX();
		double y2 = segment.getEnd().getY();
		double czone_offset = czone.getRealOffset();
		double czone_length = czone.getRealLength();
		double czone_final_offset = czone_offset + czone_length;

		x1 = x0 + segment.shift_x(czone_offset);
		y1 = y0 + segment.shift_y(czone_offset);
		x2 = x0 + segment.shift_x(czone_final_offset);
		y2 = y0 + segment.shift_y(czone_final_offset);

		Color color_before = (Color) graphics.getPaint();
		graphics.setPaint(Contour.influenceColor);

		int czone_length_s = panel.wsi(czone_length);
		int n_signs = (czone_length_s / 20);
		if (n_signs == 0)
			n_signs = 1;
		double mark_space = panel.ww(czone_length_s / n_signs); // space between
																// marks
		if (mark_space < IFemSettings.GENERAL_ACCURACY)
			mark_space = 1.0; // provide correct work with zero length zones
		double offset;
		// LOOP: DRAW SIGNS
		for (offset = czone_offset; offset <= czone_final_offset
				+ IFemSettings.GENERAL_ACCURACY; offset += mark_space) {
			czone.drawInfluenceMark(graphics, panel, offset);
		}
		graphics.setPaint(color_before);
	}// end of drawInfluence
	
	/**
	 * Segment direction in specified place.
	 * @param offset offset from the begin of segment
	 */	
	abstract public Dot getDirection(Segment segment, double offset);

	/**
	 * Get rectangular area, that covers the segment
	 * @return
	 */
	abstract public RectangleArea calculateBounds(Segment segment);

	/**
	 * Count number of intersections between given segment and ray, 
	 * starting in given dot, and going in X axis direction
	 * 
	 * @param segment
	 * @param dot
	 * @return
	 */
	abstract public int countRightXRayIntersections(Segment segment, 
			Dot dot);
	
}
