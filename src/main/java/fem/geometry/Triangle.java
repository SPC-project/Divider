package fem.geometry;

import fem.common.IFemSettings;

public class Triangle {

	private Dot corners[] = createCornersArray(); //new Dot[3];
	
        /**
         * Return newly created 3-elements array. 
         * Element type should be subclassed from Dot.
         * 
         */
        protected Dot[] createCornersArray() {
            return new Dot[3];
        }
	
	public Dot[] getCorners() {
		return corners;
	}


	public void setCorners(Dot[] corners) {
		this.corners = corners;
	}


	public boolean isInside(fem.geometry.Dot dot) {
			Line l1 = new Line(corners[0], corners[1]);
			Line l2 = new Line(corners[1], corners[2]);
			Line l3 = new Line(corners[2], corners[0]);
			double d1 = l1.signedDistance(dot);
			double d2 = l2.signedDistance(dot);
			double d3 = l3.signedDistance(dot);
			
			if(d1>-IFemSettings.GENERAL_ACCURACY&&
					d2>-IFemSettings.GENERAL_ACCURACY&&
					d3>-IFemSettings.GENERAL_ACCURACY)
					return true;
			else
					return false; 				
	}


	/**
	 * Get most distant node from given dot
	 * @param dot
	 * @return
	 */
	public int getMostDistantCornerIndex(Dot dot) {
			double d0 = dot.distance(getCorners()[0]);
			double d1 = dot.distance(getCorners()[1]);
			double d2 = dot.distance(getCorners()[2]);
			
			if(d0>d1&&d0>d2) return 0;
			if(d1>d0&&d1>d2) return 1;
			return 2;
	}


	/**
	 * Get index of first other corner
	 * 
	 * @param i
	 * @return
	 */
	public static int getOtherCorner1Index(int i) {
			if(i==0) return 1;
			if(i==1) return 2;
			return 0; //i==2
	}


	/**
	 * Get index of second other corner
	 * 
	 * @param i
	 * @return
	 */
	public static int getOtherCorner2Index(int i) {
			if(i==0) return 2;
			if(i==1) return 0;
			return 1; //i==1
	}

        
        /**
         * Are i1 and i2 situated in ascendign order in sequence 1-2-3-1
         * For example: i1=0, i2=1 => true, i1=0, i2=2 => false
         * 
         * @param i1
         * @param i2
         * @return
         */
        public static boolean isAscendingOrder(int i1, int i2) {
            return i1 < i2 || (i1==2 && i2==0);
        }
                
        
        /**
         * Get earlier index from looped sequence 0-1-2-1
         * For example: i1=0, i2=1 => 0, i1=0, i2=2 => 2
         * 
         */
        public static int getEarlierIndex(int i1, int i2)
        {
            if(isAscendingOrder(i1, i2))
                return i1;
            else
                return i2;
        }

        /**
         * Get later index from looped sequence 0-1-2-1
         * For example: i1=0, i2=1 => 1, i1=0, i2=2 => 0
         * 
         */
        public static int getLaterIndex(int i1, int i2)
        {
            if(isAscendingOrder(i1, i2))
                return i2;
            else
                return i1;
        }        
        
        /**
         * Get previous index from sequence 0-1-2-0
         * 
         * @param i
         * @return
         */
        public static int getPreviousIndex(int i) {
            if(i==0)
                return 2;
            else
                return i-1;
        }
        
        /**
         * Get next index from sequence 0-1-2-0
         * @param i
         * @return
         */
        public static int getNextIndex(int i) {
            if(i == 2)
                return 0;
            else
                return i+1;
        }
        
        
	/**
	 * Returns center of circle, drawn around this element
	 */
	public Dot circleCenter() {		
			double A1, A2, B1, B2, C1, C2;
			//line 1 coefficients
			A1=getCorners()[1].x-getCorners()[0].x;
			B1=getCorners()[1].y-getCorners()[0].y;
			C1=A1*(getCorners()[1].x+getCorners()[0].x)/2+B1*(getCorners()[1].y+getCorners()[0].y)/2;
			//line 2 coefficients
			A2=getCorners()[2].x-getCorners()[0].x;
			B2=getCorners()[2].y-getCorners()[0].y;
			C2=A2*(getCorners()[2].x+getCorners()[0].x)/2+B2*(getCorners()[2].y+getCorners()[0].y)/2;
			
			//determinants
			double d = A1*B2-B1*A2;
			double dx = C1*B2-B1*C2;
			double dy = A1*C2-C1*A2;
			
			double x = dx/d;
			double y = dy/d;
			
			return new Dot(x,y);
	}


	/**
	 * Determines, whether is dot inside the circle, drawn around the element, or not
	 */
	public boolean isInsideCircle(Dot dot) {
			Dot center = circleCenter();
			if( center.distance(dot) < center.distance(getCorners()[0]) )
					return true;
			else
					return false;
	}


	/**
	 * Get dot that is center of triangle
	 * 
	 * @return
	 */
	public Dot getCentralDot() {
			return new Dot( (getCorners()[0].x+getCorners()[1].x+getCorners()[2].x)/3,
											(getCorners()[0].y+getCorners()[1].y+getCorners()[2].y)/3);
	}


	public double getArea() {
					double s = -0.5*(
					getCorners()[0].x*(getCorners()[1].y-getCorners()[2].y)+
					getCorners()[1].x*(getCorners()[2].y-getCorners()[0].y)+
					getCorners()[2].x*(getCorners()[0].y-getCorners()[1].y)
					);
					return Math.abs(s);
	//				return s;
			}


	public double getAngleValue(int i) {
			if(i<0||i>2) return 0;
			int s1, s2, c;
			c =i;
			if(i==0) s1=2; else s1=i-1;
			if(i==2) s2=0; else s2=i+1;
			return getCorners()[c].angle(getCorners()[s1], getCorners()[s2]);
	}


	/**
	 * Returns number of minimal angle
	 */
	public int getMinAngleIndex() {
			if( getAngleValue(0) < getAngleValue(1) && getAngleValue(0) < getAngleValue(2) )
					return 0;
			if( getAngleValue(1) < getAngleValue(0) && getAngleValue(1) < getAngleValue(2) )
					return 1;
			return 2;
	}


	/**
	 * Returns index of maximal angle
	 */
	public int getMaxAngleIndex() {
			if( getAngleValue(0) > getAngleValue(1) && getAngleValue(0) > getAngleValue(2) )
					return 0;
			if( getAngleValue(1) > getAngleValue(0) && getAngleValue(1) > getAngleValue(2) )
					return 1;
			return 2;
	}
        
        
        /**
         * Sort nodes of element in counter-clockwise order.
         * Currently not done.
         * 
         */
        public void sortNodes() {
            //TODO
        }

}
