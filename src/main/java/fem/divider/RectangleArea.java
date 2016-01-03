/*
 * RectangleArea.java
 *
 * Created on Monday, 21, March 2005, 21:22
 */

package fem.divider;

import fem.geometry.Dot;

/**
 *
 * @author  gefox
 * @version
 */
public class RectangleArea extends AbstractArea{
		
		/** Creates new RectangleArea */
		public RectangleArea(double left_, double right_, double bottom_, double top_) {
				left = left_;
				right = right_;
				bottom = bottom_;
				top = top_;
		}
		
		public RectangleArea(Dot corner1, Dot corner2)
		{
				left = Math.min(corner1.getX(), corner2.getX());
				right = Math.max(corner1.getX(), corner2.getX());
				bottom = Math.min(corner1.getY(), corner2.getY());
				top = Math.max(corner1.getY(), corner2.getY());
		}
		
		public RectangleArea(RectangleArea area_)
		{
				this(area_.left, area_.right, area_.bottom, area_.top);
		}
		
		
		public double getLeft() {return left;}
		public double getRight() {return right;}
		public double getTop() {return top;}
		public double getBottom() {return bottom;}
		
		public double getWidth() { return right - left;}
		public double getHeight() { return top - bottom;}
		public double getArea() { return getWidth()*getHeight(); }
		
/*
 public void setLeft(double val) {left = val;}
		public void setRight(double val) {right = val;}
		public void setTop(double val) {top = val;}
		public void setBottom(double val) {bottom = val;}
 */
		
		public void setLeft(double val) {left = val; tellChanged();}
		public void setRight(double val) {right = val; tellChanged();}
		public void setTop(double val) {top = val; tellChanged();}
		public void setBottom(double val) {bottom = val; tellChanged();}
		
		
		public boolean resize(double left_, double right_, double bottom_, double top_) {
				if(left>right || bottom>top) return false;
				left = left_;
				right = right_;
				bottom = bottom_;
				top = top_;
				tellChanged();
				return true;
		}
		
		/**
		 *If necessary, extends area so that dot (x;y) would be included
		 */
		public boolean include(double x_, double y_) {
				boolean result = false;
				
				if(x_<left) {
						result = true;
						left = x_;
				}
				if(x_>right) {
						result = true;
						right = x_;
				}
				if(y_<bottom) {
						result = true;
						bottom = y_;
				}
				if(y_>top) {
						result = true;
						top = y_;
				}
				if(result) tellChanged();
				return result;
		}
		
		public boolean include(Dot dot)
		{
				return include(dot.getX(), dot.getY());
		}
		
		/**
		 *If necessary, extends area so that area_ would be included
		 */
		public boolean include(RectangleArea area_) {
				boolean result=false;
				if(area_.left < left) {
						result = true;
						left = area_.left;
				}
				if(area_.right > right) {
						result = true;
						right = area_.right;
				}
				if(area_.bottom < bottom) {
						result = true;
						bottom = area_.bottom;
				}
				if(area_.top > top) {
						result = true;
						top = area_.top;
				}
				if(result) tellChanged();
				return result;
		}
		
		/**
		 *Determine if the dot is inside the area
		 */
		public boolean isInside(double x, double y) {
				if(left<x&&x<right&& bottom<y&&y<top)
						return true;
				else
						return false;
		}
		
		/**
		 *Find out if area_ is inside the area
		 */
		public boolean isInside(RectangleArea area_) {
				if(left<area_.left&&area_.right<right&& bottom<area_.bottom&&area_.top<top)
						return true;
				else
						return false;
		}
		
		/**
		 *Calculate nearest distance from dot to edge of area
		 */
/*		public double distance(Dot dot_) {
				double x_=dot_.getX(); double y_=dot_.getY();
				double x; double y;
				boolean x_inside = false;
				double d;
				
				if(x_>left)// .   |-----|
						x = left;
				else
						if(x_>right)// |-----|   .
								x = right;
						else		//  |---.---|
						{
								x_inside = true;
						}
				
				if(y_<bottom) {//.=
						if(x_inside)
								return bottom-y_;
						else
								y = bottom;
				}
				else
						if(y_>top)//*=
						{
								if(x_inside)
										return y_-top;
								else
										y = top;
						}
						else//-*_
						{
								d=top-y_;
								d=Math.min(d,  y_-bottom);								
								if(x_inside)
								{
										d=Math.min(d, x-left, 
								}
						}
				
				return 0;
		}
		*/
				
		protected double left;
		protected double right;
		protected double top;
		protected double bottom;
}

