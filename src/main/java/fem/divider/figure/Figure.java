/*
 * Figure.java
 *
 * Created on Saturday, 19, March 2005, 12:34
 */

package fem.divider.figure;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fem.divider.RectangleArea;
import fem.divider.figure.command.CommandStack;

/** Basic 
 * @author gefox
 * @author Nikolay Konovalow ('contours' access methods)
 */
public class Figure {

		/** Creates new Figure */
    public Figure() {
				meshSettings = new fem.divider.mesh.MeshSettings();
    }

		public void draw(Graphics2D graphics)
		{
				for(Iterator<Contour> i=contours.iterator(); i.hasNext(); )
				{
						((Contour)i.next()).draw(graphics);
				}
		}
		
		public void setPanel(FigurePanel panel_)
		{
				panel = panel_;
		}
		
		public Segment findSegment(double x_, double y_, double handle_)
		{
				Segment segment;
				for(Iterator<Contour> i=contours.iterator(); i.hasNext(); )
				{
						segment = ((Contour)i.next()).findSegment(x_, y_, handle_);
						if(segment!=null) return segment;
				}
				return null;
		}
		
		//=========== Public access methods for field 'contours' ===========
		/** Examine if  contour is incorporated in this figure*/
      public boolean haveContour(Contour contour) {
         return contours.indexOf( contour )== -1;
      }
		/** Examine if figure is empty (have no contours) */
      public boolean isNonEmpty() {
         return !contours.isEmpty();
      } 
		/** Delete  contour from this figure */
		public void deleteContour(Contour contour) {
		   contours.remove(contour);
      }
		/** @return List of contours, that compose this figure */
		public List<Contour> getContours() {
         return contours;
      }
      public void setContours(List<Contour> contours_) {
         contours = contours_;
         sortContours();
      }
      public void addContour(Contour contour) {
         contours.add(contour);
         sortContours();
      }
		public int contoursCount() {
		   return contours.size();
		}
		public Contour getContourByIndex(int index) {
		   return contours.get(index);
		}
		//=========================== end ===============================
   
		/**
		 * Sort Contours in order of increasing area
		 * If outer (bigger) contour meshing first - it will overlap smaller
		 */
		public void sortContours(){
			int N = contoursCount();
			double[] areas = new double[ N ];
			int pointer = 0;
			for( Contour contour : contours ){
				RectangleArea ra = contour.calculateBounds();
				double approxArea = ra.getWidth() * ra.getHeight(); //TODO: will not work properly on star-like figures
				areas[pointer++] = approxArea;
			}
			
			if( N == 2 && areas[0] > areas[1] ){
				Contour tmp = contours.get(1);
				contours.set( 1, contours.get(0) );
				contours.set( 0, tmp );
			} else {
				boolean need_sort = false;
				double prev_area = Double.NEGATIVE_INFINITY;
				for( double area : areas ) {
					if( prev_area > area ){
						need_sort = true;
						break;
					} else {
						prev_area = area;
					}
				}
				if( !need_sort ){
					return;
				}
						
				// sorting
				int[] indexes = new int[ N ];
				int pos = -1;
				double min_val = Double.POSITIVE_INFINITY;
				for( int i = 0; i < N-1; i++ ){
					for( int j = i; j < N; j++ ){
						if( areas[j] < min_val ){
							min_val = areas[j];
							pos = j;
						}
					}
					double tmp = areas[pos];
					areas[pos] = areas[i];
					areas[i] = tmp;			
					indexes[i] = pos;
				}
				List<Contour> new_contours = new ArrayList<Contour>(N);
				int increase = 0;
				for( int i : indexes ){
					new_contours.add( increase++, contours.get(i) );
				}
				contours = new_contours;
			}
		}
		
		public fem.divider.RectangleArea calculateBounds()
		{
				if(contoursCount()==0) return null; //if there's no contours yet
				fem.divider.RectangleArea area = ((Contour)contours.get(0)).calculateBounds();
				for(int i=1;i<contoursCount();i++)
				{
						area.include( ((Contour)contours.get(i)).calculateBounds() );
				}
				return area;
		}
		
		public fem.divider.mesh.MeshSettings getMeshSettings() {
		      return meshSettings;
		}	
		public void setMeshSettings(fem.divider.mesh.MeshSettings settings) {
				meshSettings = settings;
		}
		/** Redraw figure on panel */
		public void redraw()	{
				panel.redraw();
		}
		
		/**Redraw mesh that corresponds to this figure */
		public void redrawMesh() {
			if(mesh!=null) mesh.redraw();
		}		
		public void setMesh(fem.divider.mesh.Mesh _mesh) {
			mesh=_mesh;
		}
		public fem.divider.mesh.Mesh getMesh()	{
			return mesh;
		}
		
		/**
		 * @return -CZones, that belong to @groupName group, no more than @maxNum. 
		 * <br>- First one it finds <i>(if groupName=null and maxNum=1)</i> 
		 * 
		 * @param maxNum if <b>0</b>, quantity not limited
		 * @param groupName if <b>null</b>, group name not checked 
		 */
		public ArrayList<CZone> getCZones(String groupName, int maxNum)
		{
			int cont_i, segm_i, cz_i, cont_n, segm_n, cz_n,  num;
			Contour cont;
			Segment segm;
			Node node;
			ArrayList<CZone> czones;
			ArrayList<CZone> result = new ArrayList<CZone>(1);
			CZone czone;
			
			cont_n= contoursCount();
			num=0;
			contours_loop: for(cont_i=0; cont_i<cont_n; cont_i++) //for each contour
			{
				cont=(Contour)contours.get(cont_i);
				
				segm_n= cont.nodes.size();
				for(segm_i=0; segm_i<segm_n; segm_i++) //for each segment of contour;
				{
					//System.out.println("Segmtnt #"+segm_i);
					node = (Node)(cont.nodes.get(segm_i));
					segm = node.getNextSegment();
					czones= segm.getCZones(); 
					if( czones.isEmpty() ) continue; //if empty, move to next segment
					
					if(groupName==null&&maxNum==0) //if no conditions, add alll
					{
						result.addAll(czones);
					}
					else //there are some conditions, we need to choose what to add
					{
						cz_n = czones.size();
						for(cz_i=0; cz_i<cz_n; cz_i++) //for each CZone of segment
						{
							czone = (CZone)czones.get(cz_i);
							if( groupName==null||czone.getGroupName().equals(groupName) )
							{
								num++; //advance added czones counter								
							}
							if( num==maxNum) break contours_loop;
						}//end of CZones loop
					}
				}//end of segments loop
			}//end of contours loop
			return result;
		}//end of method getCZones
		
		public CommandStack getCommandStack() {
			return commandStack;
		}

		public boolean isEditable() {
			return editable;
		}
	
	
		public void setEditable(boolean b) {
			editable = b;
		}
			
		/**
		 * Numerate segments in the order in which they situated in the contour
		 */
		public void updateSegmentsIndexes() {
			int segmentIndex = 0;
			for(Contour contour: getContours()) {
				for(Node node: contour.getNodes()) {
					Segment seg = node.getNextSegment();
					seg.setSegmentIndex(segmentIndex++);
				}
			}
		}//end method updateSegmentsIndexes
		
		private fem.divider.mesh.Mesh mesh;
		public FigurePanel panel;
		private List<Contour> contours = new ArrayList<Contour>(5);

		//here are stored all commands of figure editing 
		CommandStack commandStack = new CommandStack(); 
		
		private fem.divider.mesh.MeshSettings meshSettings;
		private boolean editable=true;
}
