/*
 * Created on 4/10/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fem.divider.figure;
import java.awt.Graphics2D;

import fem.divider.Messages;
import fem.geometry.Dot;

/**
 * @author gefox
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CZone implements Cloneable {
	public CZone()
	{}
	
	public CZone(Segment segment_)
	{
		setSegment(segment_);
	}
	
	public CZone(CZone czone)
	{
		//geometric properties
		segment=czone.segment;
		int i;
		if(czone.meshNodes!=null)
			for(i=0; i<czone.meshNodes.length; i++)
				meshNodes[i]=czone.meshNodes[i];
		
		offsetMode=czone.offsetMode;
		lengthMode=czone.lengthMode;
	
		offsetVal=czone.offsetVal;
		lengthVal=czone.lengthVal;
	
		name=czone.name;
		groupName=czone.groupName;
		
		//influence properties
		influence_mode=czone.influence_mode;
	
		//for contact
		forbid_x_motion=czone.forbid_x_motion;
		forbid_y_motion=czone.forbid_y_motion;
	
	
		force_zero_direction=czone.force_zero_direction;
		//angle between initial and actual direction of force (in radians)
		force_direction=czone.force_direction;
		force_value=czone.force_value;		
	}
	
	public Object clone()
	{
		return (Object)(new CZone(this));
	}

	
	public Segment getSegment() {return segment;}
	public void setSegment(Segment segment_) {segment=segment_;}
	
	public String getName() {return name;}
	public void setName(String name_) {name=name_;}
	
	public String getGroupName() {return groupName;}
	public void setGroupName(String name_) {groupName=name_;}

	public int getOffsetMode() {return offsetMode;}
	public void setOffsetMode(int val) {offsetMode=val;}
	
	public int getLengthMode() {return lengthMode;}
	public void setLengthMode(int val) {lengthMode=val;}

	public double getOffsetVal() {return offsetVal;}
	public void setOffsetVal(double val) {offsetVal=val;}
	
	/**
	 * Return value, that depends offsetMode and offsetVal
	 */
	public double getRealOffset()
		{
			switch(offsetMode)
			{
				case OFFSET_BEGIN: 
					return 0.0;
				case OFFSET_VAL: 
					return Math.min(offsetVal, segment.getLength());
				default: 
					return segment.getLength();
			}
		}

		/**
		 * Return value, that depends lengthMode and lengthVal
		 */
		public double getRealLength()
			{
				switch(lengthMode)
				{
					case LENGTH_ZERO: 
						return 0.0;
					case LENGTH_VAL: 
						return Math.min(segment.getLength()-getRealOffset(),
									lengthVal);
					default: 
						return segment.getLength()-getRealOffset();
				}
			}

		
	public double getLengthVal() {return lengthVal;}
	public void setLengthVal(double val) {lengthVal=val;}

	/**
	 * Check, if this CZone intersects with zone2, assuming they belong to same segment
	 */
	public boolean intersects(CZone zone2)
	{
		double begin1=getRealOffset(), end1=begin1+getRealLength();
		double begin2=zone2.getRealOffset(), end2=begin2+zone2.getRealLength();
		
		if( (begin1<end2) && (begin2<end1) ) 
			return true;
		else
			return false;
	} 

	/**
	 * Draw 1 influence mark, in place, that is shifted by offset
	 * from the beginning of this contact zone
	 * 
	 * @param graphics
	 * @param panel
	 * @param offset
	 */
	public void drawInfluenceMark(Graphics2D graphics, FigurePanel panel,
			double offset) {
		double force[];
		double x0 = segment.getBegin().getX();
		double y0 = segment.getBegin().getY();
		double xw, yw, xw2, yw2;
		xw = x0 + segment.shift_x(offset);
		yw = y0 + segment.shift_y(offset);
		// DRAW SIGNS SPECIFIC TO CONTACT OR FORCE
		if (this.getInfluenceMode() == CZone.INFLUENCE_CONTACT)
		// CONTACT
		{
			graphics.drawRect(panel.xsi(xw) - 4, panel.ysi(yw) - 4, 8, 8);
			if (this.isForbidXMotion())
				graphics.drawLine(panel.xsi(xw), -8 + panel.ysi(yw), panel
						.xsi(xw), 8 + panel.ysi(yw));
			if (this.isForbidYMotion())
				graphics.drawLine(panel.xsi(xw) - 8, panel.ysi(yw), panel
						.xsi(xw) + 8, panel.ysi(yw));
		}
		if (this.getInfluenceMode() == CZone.INFLUENCE_FORCE
				|| this.getInfluenceMode() == CZone.INFLUENCE_DISTRIBUTED_FORCE) {
			// FORCE, draw an arrow
			force = this.forceDirection(offset);
			// arrow line
			xw2 = xw - force[0] * panel.ww(25);
			yw2 = yw - force[1] * panel.ww(25);
			graphics.drawLine(panel.xsi(xw), panel.ysi(yw), panel.xsi(xw2),
					panel.ysi(yw2));
			// arrow hat
			double force1[] = new double[2];
			force1[0] = force[0];
			force1[1] = force[1];
			Dot.turnVector(force1, 20 * Math.PI / 180);
			double xa1 = xw - force1[0] * panel.ww(10), ya1 = yw
					- force1[1] * panel.ww(10);
			graphics.drawLine(panel.xsi(xa1), panel.ysi(ya1),
					panel.xsi(xw), panel.ysi(yw));
			force1[0] = force[0];
			force1[1] = force[1];
			Dot.turnVector(force1, -20 * Math.PI / 180);
			xa1 = xw - force1[0] * panel.ww(10);
			ya1 = yw - force1[1] * panel.ww(10);
			graphics.drawLine(panel.xsi(xa1), panel.ysi(ya1),
					panel.xsi(xw), panel.ysi(yw));
			if (this.getInfluenceMode() == CZone.INFLUENCE_FORCE) {
				graphics.drawOval(panel.xsi(xw2) - 2, panel.ysi(yw2) - 2,
						4, 4);
			}
		}
		// end of DRAW SIGNS SPECIFIC TO CONTACT OR FORCE		
	}

	//Influence-related properties
	public int getInfluenceMode() {return influence_mode;}
	public void setInfluenceMode(int val) {influence_mode=val;}
	
	//for contact 
	public boolean isForbidXMotion() {return forbid_x_motion;}
	public void setForbidXMotion(boolean val) {forbid_x_motion=val;}
	public boolean isForbidYMotion() {return forbid_y_motion;}
	public void setForbidYMotion(boolean val) {forbid_y_motion=val;}

	//for force
	public int getForceZeroDirection() {return force_zero_direction;}
	public void setForceZeroDirection(int val) {force_zero_direction=val;}

	public double  getForceDirection() {return force_direction;}
	public void setForceDirection(double val) {force_direction=val;}
	public double  getForceDirectionDeg() //same in degres 
			{return force_direction*180/Math.PI;}
	public void setForceDirectionDeg(double val) 
			{force_direction=val*Math.PI/180;}
	public double  getForceValue() {return force_value;}
	public void setForceValue(double val) {force_value=val;}

	/**
	 * @return x and y components of force direction vector
	 */
	public double[] forceDirection(double segmentOffset)
	{
		double vec[]=new double[2];
		vec[0]=1.0;  vec[1]=0.0;
		if(force_zero_direction==FORCE_ZERO_DIRECTION_NORMALE)
		{
			double x, y;
			vec=segment.direction(segmentOffset);
			if(segment.getBegin().contour.isClockwise())//need to change sign if contour is clockwise
					{x=-vec[0]; y=-vec[1];}
				else
					{x=vec[0]; y=vec[1];}
			vec[0]=-y; vec[1]=x;
		}
		fem.geometry.Dot.turnVector(vec, force_direction);
		return vec;
	}//end forceDirection(...)
	
	//geometric properties
	private Segment segment;
	private fem.divider.mesh.Node meshNodes[];
		
	private int offsetMode=OFFSET_BEGIN;
	private int lengthMode=LENGTH_TO_END;
	
	private double offsetVal=0.0;
	private double lengthVal=0.0;   
	
	private String name="noname"; //$NON-NLS-1$
	private String groupName="noname"; //$NON-NLS-1$
	
	public static final int OFFSET_BEGIN=0;
	public static final int OFFSET_VAL=1;
	public static final int OFFSET_END=2;
	
	public static final String OFFSET_MODE_DESCRIPTION[]=
		{Messages.getString("CZone.From_begin_3"), Messages.getString("CZone.Give_value_4"), Messages.getString("CZone.From_end_5")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
	public static final int LENGTH_ZERO=0;
	public static final int LENGTH_VAL=1;
	public static final int LENGTH_TO_END=2;

	public static final String LENGTH_MODE_DESCRIPTION[]=
		{Messages.getString("CZone.Zero_length_6"), Messages.getString("CZone.Give_value_7"), Messages.getString("CZone.To_end_8")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


	//influence properties
	public static final int INFLUENCE_NONE=0;
	public static final int INFLUENCE_CONTACT=1;
	public static final int INFLUENCE_FORCE=2;
	public static final int INFLUENCE_DISTRIBUTED_FORCE=3;
	public static final String INFLUENCE_MODE_DESCRIPTION[]=
		{Messages.getString("CZone.None_9"), Messages.getString("CZone.Contact_10"), Messages.getString("CZone.Force_11"), "Dsitributed Force"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private int influence_mode=INFLUENCE_NONE;
	
	//for contact
	private boolean forbid_x_motion=true;
	private boolean forbid_y_motion=true;
	
	//for force
	private static final int FORCE_ZERO_DIRECTION_NORMALE=0;
	private static final int FORCE_ZERO_DIRECTION_X=1;
	public static final String FORCE_ZERO_DIRECTION_DESCRIPTION[]=
		{Messages.getString("CZone.Normale_12"), Messages.getString("CZone.X-axis_13")}; //$NON-NLS-1$ //$NON-NLS-2$
	
	private int force_zero_direction=FORCE_ZERO_DIRECTION_NORMALE;
	//angle between initial and actual direction of force (in radians)
	private double force_direction=0.0;
	private double force_value=0.0;
}
