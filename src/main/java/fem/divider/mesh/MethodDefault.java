package fem.divider.mesh;

import java.util.ArrayList;

import fem.divider.Messages;
import fem.divider.figure.Contour;
import fem.divider.figure.Figure;
import fem.geometry.Dot;
import fem.geometry.Triangle;

/**
 * @author gefox
 */
public class MethodDefault extends MethodAbstract {

	public MethodDefault() {
	}

	/* 
	 * @see divider.mesh.MethodInterface#getInstance()
	 */
	public static MethodAbstract getInstance() {
		if( methodInstance==null ) methodInstance=new MethodDefault();
		return methodInstance;
	}

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getName()
	 */
	public String getName() {
		return Messages.getString("MethodDefault.Default_method_1"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getDescription()
	 */
	public String getDescription()
	{return Messages.getString("MethodDefault.Good_for_most_figures_2"); //$NON-NLS-1$
	}

	/**
	 * Generate mesh from figure
	 * @param figure_ --- source figure
	 * @return resulting mesh
	 */
	public Mesh meshdown(Figure figure_) {
		haveExcessiveSquare=true;
		Mesh mesh = new Mesh();
		this.mesh = mesh; 
		MeshSettings settings_ = figure_.getMeshSettings();
		mesh.settings=settings_;
				
		//make initial triangulation, without adding new Nodes
		String msg = initial_triangle_dividing(mesh, figure_);
		if(msg != null)
		{
				javax.swing.JOptionPane.showMessageDialog(null, msg, Messages.getString("MethodDefault.Meshdown_failed_3"),  //$NON-NLS-1$
					javax.swing.JOptionPane.ERROR_MESSAGE);
				return null;
		}
		excessiveSquare=initialExcessiveSquare=getExcessiveSquare();
		haveExcessiveSquare=true;
		divide_triangles(mesh);

		figure_.updateSegmentsIndexes();
		mesh.sortNodes();
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
		return null;
	}

	/* (non-Javadoc)
	 * @see divider.mesh.MethodInterface#getProgress()
	 */
	public double getProgress() {
		double progress;
		if(haveExcessiveSquare)
				progress=1.0-excessiveSquare/initialExcessiveSquare;
			else 
				progress=0.0;
		//System.out.println("Ask progress:"+progress );
		return progress;
	}




	private static final int DEBUG_LEVEL_NONE = 0;
	private static final int DEBUG_LEVEL_NOUPGRADE = 1;
	private static final int DEBUG_LEVEL_NOREMOVE = 2;
//	private static final int DEBUG_LEVEL_EXTREME = 100;

//	private static int debugLevel = DEBUG_LEVEL_NOREMOVE;
//	private static int debugLevel = DEBUG_LEVEL_NOUPGRADE;
	private static int debugLevel = DEBUG_LEVEL_NONE;
	
	/**
	 * Makes initial "rough" triangulation (do not add new nodes to @fig)
	 * Returns error message on failure or null on success
	 * TODO: nodes with 180 degree angle may produce redundant empty triangle
	 */
	private String initial_triangle_dividing(Mesh mesh, Figure fig)
	{
			fem.divider.RectangleArea bounds = fig.calculateBounds();
			if(bounds==null)
					return Messages.getString("MethodDefault.Empty_figure._Can__t_meshdown_4"); //$NON-NLS-1$
				
			// Create auxiliary element (triangle), that surrounds our figure
			// They will add itself to 'mesh' in own constructors
			Node aux1 = new Node(mesh, bounds.getRight()+bounds.getWidth(), bounds.getBottom()-bounds.getHeight()/2);
			Node aux2 = new Node(mesh, (bounds.getLeft()+bounds.getRight())/2, bounds.getTop()+bounds.getHeight());
			Node aux3 = new Node(mesh, bounds.getLeft()-bounds.getWidth(), bounds.getBottom()-bounds.getHeight()/2);
			
			mesh.createElement(aux1, aux2, aux3);
			Node node;
			Element el;
			
			//we will use this array of arrays to trace contours (that may be broken or unclosed)
			ArrayList<ArrayList<Node>> traces = new ArrayList<ArrayList<Node>>(fig.contoursCount());
			/* At first, create large triangle, that certainly contain @fig, then add a dot
			 *		from @fig and split original triangle to three smaller by this dot.
			 * Then select next dot, choice triangle, that contains it - and do splitting on it.
			 */
			for( Contour contour : fig.getContours() ) {
					ArrayList<Node> mesh_nodes = new ArrayList<Node>( contour.getNodesAmount() );
					//Add nodes of contour to mesh
					for( Dot dot : contour.getDots() ) {
							el = mesh.findElementThatCovers(dot);
							if(el==null)
								return "Error: found a node (x=" + dot.getX() + ",y=" + dot.getY() 
										+ ") that isn't covered by any element"; //$NON-NLS-1$
							//add new node
							node = Node.createConditionaly(mesh, dot);
							//replace old triangle by 3 new
							mesh.createElement(el.getNodes()[0], el.getNodes()[1], node);
							mesh.createElement(el.getNodes()[1], el.getNodes()[2], node);
							mesh.createElement(el.getNodes()[2], el.getNodes()[0], node);
							el.delete();
							
							node.lawson();
							mesh_nodes.add(node);//remember node
					}
					traces.add( mesh_nodes );
			}

			aux1.delete();
			aux2.delete();
			aux3.delete();
			mesh.cleanElements(fig.getContours());	
			
			
			// Fix a Contour if it is broken (make sure, that Nodes-neighbors are connected)
			Node prev, curr;			
			for( ArrayList<Node> as_contour : traces ) {		
				if( as_contour.size() == 0 ) return "Error: get empty contour";
				if( as_contour.size() == 1 ) continue;

				curr = as_contour.get(0);
				int i;
				for( i = 1; i < as_contour.size(); i++ ){
					prev = curr;
					curr = as_contour.get(i);
					if( curr.elements == null || prev.elements == null ) // if mesh.cleanElements go wrong
						mesh.fixUnconnectedNodes( prev, curr, traces );
					mesh.fixEdge(prev, curr);
				}
				if( i > 1 ) {// fix first and last Nodes (not necessary, if they are only two)
					mesh.fixEdge(curr, as_contour.get(0));
				}
			}

			if(debugLevel>=DEBUG_LEVEL_NOREMOVE) {
				System.out.println("NOTICE: removing irrelevant elements switched off for debug");
				return null;
			}
			
			return null;
	}
	
	
	/**
	 * Divide the figure's area to mesh by adding new nodes  
	 */
	private void divide_triangles(Mesh mesh)
	{
			if(debugLevel>=DEBUG_LEVEL_NOUPGRADE) {
				System.out.println("NOTICE: mesh upgrade switched off for debug");
				return;
			}
			Element el;
			boolean affected;
			double sq1, sqSum;
			do
			{
				sqSum=0;
				int s = mesh.elements.size();
				affected = false;
				for(int i = 0; i<s; i++) //for all elements
				{
					el = (Element)mesh.elements.get(i);
					sq1=el.getArea()-mesh.settings.maxArea;//calculate exceeding area
					if(sq1>0) sqSum+=sq1;
					if( el.upgrade() ) { //old element was removed, we gonna stop earlier
					   s--; 
					   affected=true;
					}				
				}//end for all elements
				excessiveSquare = sqSum;
			}
			while(affected);
	}//end divide_triangles
	
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
	}
	
	private Mesh mesh;
	static private MethodDefault methodInstance=null;
	
	//these values required to determine progress
	private double initialExcessiveSquare=0.0;
	private double excessiveSquare=0.0;
	private boolean haveExcessiveSquare=false;
}
