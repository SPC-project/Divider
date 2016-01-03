package fem.divider.mesh;

import fem.common.IFemSettings;
import java.util.*;

import fem.divider.*;
import fem.divider.figure.Contour;
import fem.divider.figure.Figure;
import fem.geometry.Triangle;

public class MethodRRegular extends MethodAbstract {

	private MethodRRegular() {
	}

	/* 
	 * @see divider.mesh.MethodInterface#getInstance()
	 */
	public static MethodAbstract getInstance() {
		if( methodInstance==null ) methodInstance=new MethodRRegular();
		return methodInstance;
	}

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getName()
	 */
	public String getName() {
		return Messages.getString("MethodRRegular.RRegular_method_1");  //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getDescription()
	 */
	public String getDescription()
	{return Messages.getString("MethodRRegular.Rectangle_Regular._Only_for_rectangles_with_sides_parallel_to_axes._2");  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#meshdown(divider.figure.Figure)
	 */
	public Mesh meshdown(Figure figure_) {
		haveExcessiveSquare=true;
		Mesh mesh = new Mesh();
		this.mesh = mesh; this.figure=figure_;
		MeshSettings settings_ = figure_.getMeshSettings();
		mesh.settings=settings_;
				

//		excessiveSquare=initialExcessiveSquare=getExcessiveSquare();
//		haveExcessiveSquare=true;
//		upgrade(mesh);
//here should go triangulation

		//TRIANGULATION
		//collect information about figure
		Contour contour=(Contour)figure_.getContourByIndex(0);
		fem.divider.figure.Node node1, node2, node3, 
			bl_n=null, br_n=null, tl_n=null, tr_n=null;//bottom&top, left&right;
		fem.divider.figure.Segment left_s=null, right_s=null, top_s=null, bottom_s=null;
		fem.divider.figure.Node left_s_n, right_s_n, bottom_s_n, top_s_n; //begin of the segments 
		int i;
		for(i=0; i<8; i++)//find out relative place of nodes
		{
			node1=(fem.divider.figure.Node)contour.nodes.get( i%4 );
			node2=(fem.divider.figure.Node)contour.nodes.get( (i+1)%4 );
			node3=(fem.divider.figure.Node)contour.nodes.get( (i+2)%4 );
			if( Math.abs(node1.getY()-node2.getY())<IFemSettings.GENERAL_ACCURACY &&
				node3.getY()>node2.getY() )//found bottom left or right node
				{
					if( contour.isClockwise() )
					{br_n=node1;
					bl_n=node2;
					tl_n=node3;	
					tr_n=(fem.divider.figure.Node)contour.nodes.get( (i+3)%4 );
					left_s=bl_n.getNextSegment();
					top_s=tl_n.getNextSegment();
					right_s=tr_n.getNextSegment();
					bottom_s=br_n.getNextSegment();
					}//end if( contour.isClockwise() ) (true)
					else
					{
					bl_n=node1;
					br_n=node2;
					tr_n=node3;
					tl_n=(fem.divider.figure.Node)contour.nodes.get( (i+3)%4 );
					left_s=tl_n.getNextSegment();
					bottom_s=bl_n.getNextSegment();
					right_s=br_n.getNextSegment();
					top_s=tr_n.getNextSegment();
					}//end if( contour.isClockwise() ) (false) 
				}//end if found base node
		}//end for nodes
		left_s_n=left_s.getBegin();
		right_s_n=right_s.getBegin();
		bottom_s_n=bottom_s.getBegin();
		top_s_n=top_s.getBegin();
		//end find out relative node and side position		
		//triangulation parameters
		double width=br_n.getX()-bl_n.getX();
		double height=tl_n.getY()-bl_n.getY();
		double preferred_side=Math.sqrt(2*figure.getMeshSettings().getMaxArea());
		int x_count=(int)Math.ceil(width/preferred_side);//count of elments on x
		double x_side=width/((double)x_count);
		int y_count=(int)Math.ceil(height/preferred_side);//count of elements on y
		double y_side=height/((double)y_count);

		
		Node node;
		Triangle el1, el2;
		List<Node> nodes=mesh.nodes;
		int node_i=0;
		double x, y;
		//first row
		y=bl_n.getY();
		x=bl_n.getX();
		int xi, yi;
		node=new Node(mesh, bl_n);//create node and add it to mesh
		node_i++;
		for(xi=1; xi<x_count; xi++)
		{x+=x_side;
			node=new Node(mesh, x, y);
			node_i++;
			node.setSegment(bottom_s, Math.abs(bottom_s.getBegin().getX()-x));
		}//end for xi
		node=new Node(mesh, br_n);
		node_i++;
		
		//main part of mesh
		for(yi=1; yi<y_count; yi++)
		{
			y+=y_side;
			x=bl_n.getX();
			node=new Node(mesh, x, y);
			node_i++;
			node.setSegment(left_s, Math.abs(left_s.getBegin().getY()-y));
			for(xi=1; xi<=x_count; xi++)
			{x+=x_side;
				node=new Node(mesh, x, y);
				node_i++;
				el1=new Element((Node)nodes.get(node_i-2),
						(Node)nodes.get(node_i-3-x_count),
						(Node)nodes.get(node_i-2-x_count) ); 
				el2=new Element((Node)nodes.get(node_i-1),
						(Node)nodes.get(node_i-2),
						(Node)nodes.get(node_i-2-x_count) ); 
			}
			node.setSegment(right_s, Math.abs(right_s.getBegin().getY()-y));
		}//end for yi
		
		//last row
		node=new Node(mesh, tl_n);//create node and add it to mesh
		node_i++;
		y=tl_n.getY();
		x=tl_n.getX();
		for(xi=1; xi<x_count; xi++)
		{x+=x_side;
			node=new Node(mesh, x, y);
			node_i++;
			node.setSegment(top_s, Math.abs(top_s.getBegin().getX()-x));
			el1=new Element((Node)nodes.get(node_i-2),
					(Node)nodes.get(node_i-3-x_count),
					(Node)nodes.get(node_i-2-x_count) ); 
			el2=new Element((Node)nodes.get(node_i-1),
					(Node)nodes.get(node_i-2),
					(Node)nodes.get(node_i-2-x_count) ); 

		}//end for xi
		node=new Node(mesh, tr_n);
		node_i++;
		el1=new Element((Node)nodes.get(node_i-2),
				(Node)nodes.get(node_i-3-x_count),
				(Node)nodes.get(node_i-2-x_count) ); 
		el2=new Element((Node)nodes.get(node_i-1),
				(Node)nodes.get(node_i-2),
				(Node)nodes.get(node_i-2-x_count) ); 
		//END TRIANGULATION
		
		figure_.updateSegmentsIndexes();
		mesh.updateNodesIndexes();
		mesh.updateElementsIndexes();
		mesh.determineCZones(figure_);

		mesh.figure=figure_;
		return mesh;
	}//end meshdown

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#test(divider.figure.Figure)
	 */
	public String test(Figure figure_) {
		for(Contour contour : figure_.getContours() ) {
			if(contour.nodes.size()!=4) 
				return Messages.getString("MethodRRegular.Rectangle_required_1");

			fem.divider.figure.Node node1, node2, node3, node4;
			node1=(fem.divider.figure.Node)contour.nodes.get(0);
			node2=(fem.divider.figure.Node)contour.nodes.get(1);
			node3=(fem.divider.figure.Node)contour.nodes.get(2);
			node4=(fem.divider.figure.Node)contour.nodes.get(3);
			double angle=0.0;

			angle=node1.angle(node4, node2);
			if(Math.abs(angle-Math.PI/2)>IFemSettings.GENERAL_ACCURACY) 
				return Messages.getString("MethodRRegular.Rectangle_required_2");

			angle=node2.angle(node1, node3);
			if(Math.abs(angle-Math.PI/2)>IFemSettings.GENERAL_ACCURACY)
				return Messages.getString("MethodRRegular.Rectangle_required_2");

			angle=node3.angle(node2, node4);
			if(Math.abs(angle-Math.PI/2)>IFemSettings.GENERAL_ACCURACY)
				return Messages.getString("MethodRRegular.Rectangle_required_2");

			angle=node4.angle(node3, node1);
			if(Math.abs(angle-Math.PI/2)>IFemSettings.GENERAL_ACCURACY)
				return Messages.getString("MethodRRegular.Rectangle_required_2");
		}
		return null;
	}//end test(Figure)

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getProgress()
	 */
	public double getProgress() {
		double progress;
//		if(haveExcessiveSquare)
//				progress=1.0-excessiveSquare/initialExcessiveSquare;
//			else 
				progress=0.0;
		//System.out.println("Ask progress:"+progress );
		return progress;
	}

	private double getExcessiveSquare()
	{
		double square=0.0, sq1;
		Triangle el;
		int s = mesh.elements.size();
		for(int i = 0; i<s; i++) //for all elements
		{
				el = (Triangle)mesh.elements.get(i);
				sq1 = el.getArea()-mesh.settings.maxArea; //excessive area of one element
				if( sq1>0 ) 
					{
//						System.out.println("max="+mesh.settings.maxArea+" real="+el.area()+
//							" excessive="+sq1);
						square+=sq1;
					} 
		}
		
		return square;		
	}//end getExcessiveSquare()
	
//	private double 
	private Mesh mesh;
	private Figure figure;
	static private MethodRRegular methodInstance=null;
	
	//these values required to determine progress
	private double initialExcessiveSquare=0.0;
	private double excessiveSquare=0.0;
	private boolean haveExcessiveSquare=false;
}
