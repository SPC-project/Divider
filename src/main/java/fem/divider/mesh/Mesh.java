/*
 * Mesh.java
 *
 * Created on Saturday, 19, March 2005, 12:35
 */

package fem.divider.mesh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import fem.divider.figure.CZone;
import fem.divider.figure.Contour;
import fem.divider.figure.Figure;
import fem.geometry.Dot;
import fem.geometry.Triangle;

/**
 *
 * @author  gefox
 * @version 
 */
public class Mesh {
	public static  Color cMesh = new Color(0, 0, 0); //black
   Figure figure=null;
   MeshPanel panel;
   MeshSettings settings;
   List<Node> nodes = new ArrayList<Node>(30);
   List<Element> elements = new ArrayList<Element>(60);

	/** Creates new Mesh */
    Mesh() {
    }

		static public Mesh factory(Figure fig, MeshPanel panel)
		{
			
				Mesh mesh = MethodDefault.getInstance().meshdown(fig);
				if(mesh!=null)
					{
						mesh.setPanel(panel); 
						panel.setMesh(mesh);
					}
				return mesh;
		}
		
		public void createElement( Node A, Node B, Node C ){
			@SuppressWarnings("unused")
			Element newOne = new Element(A, B, C); // Add this element to Mesh of Node A in newOne's constructor
		}
		public void draw(Graphics2D g)
		{
				g.setPaint(cMesh);
				for(Node node : nodes) {
						node.draw(g);
				}
				for(Element el : elements)	{
						el.draw(g);
				}
		}
		
		/**Redraw mesh on panel
		 */
		public void redraw()
		{
				panel.redraw();
		}
	
		/**Redraw figure that coresponds to this mesh
		 */
		public void redrawFigure()
		{
			if(figure!=null)
				figure.redraw();				
		}
			
		public void setPanel(MeshPanel panel_)
		{
				panel = panel_;
		}
		
		public MeshPanel getPanel()
		{
				return panel;
		}
		
		public Figure getFigure()
		{
			return figure;
		}
		
		/**
		 *Remove node from nodes list
		 */
		public void forget(Node node)
		{
				nodes.remove(node);
		}
		
		/**
		 *Remove element from elements list
		 */
		public boolean forget(Triangle element)
		{
				return elements.remove(element);
		}
		
		/**
		 * Connect Nodes by generating new Element
		 * Called if first or second are no elements involved (unconnected to Figure)
		 */
		void fixUnconnectedNodes( Node first, Node second, ArrayList<ArrayList<Node>> traces ){
			Node nearest = null;
			double dist = Double.POSITIVE_INFINITY;
			// find Node, nearest both to first and second (hope, that triangle will not overlap anything)
			for( ArrayList<Node> contour : traces ){
				for( Node nod : contour ){
					if( nod == first || nod == second )
						continue;
					double x = nod.getX();
					double y = nod.getY();
					double d = Math.abs(x - first.getX()) + Math.abs(y - first.getY() 
							+ Math.abs(x - second.getX()) + Math.abs(y - second.getY()) ); // Euclidian too expensive, I think 
					if( d < dist ) { 
						nearest = nod;
						dist = d;
					}
				}
			}
			
			createElement(first, nearest, second);
		}

		/**
		 *Fix, if both Nodes is neighbors, but belong different Elements
		 *If needed, turn diagonal so that thisNode and anotherNode would belong to one element
		 *@return true if fixing was done
		 */
		boolean fixEdge(Node thisNode, Node anotherNode)
		{			
			Element elOpposite;
			for( Element thisNodeHolder : thisNode.elements )
			{
				if( thisNodeHolder.hasNode(anotherNode) ) 
					return false;

				elOpposite = thisNodeHolder.oppositeOf(thisNode);
				if(elOpposite==null) 
					continue;
				if(!elOpposite.hasNode(anotherNode)) 
					continue;
				
				//here we have neighbors thisNodeHolder and elOpposite, that contains thisNode and anotherNode respectively
				if( thisNodeHolder.swapDiagonalWith(elOpposite) )
					return true;
			}
			
			if( anotherNode.elements.size() < thisNode.elements.size() ){ // decrease looping 
				Node temp = anotherNode;
				anotherNode = thisNode;
				thisNode = temp;
			}

			// Fix half of the gap in the contour by new Element, created on thisNode, anotherNode and nearest to both of them
			Node nearest = null;
			double dist = Double.POSITIVE_INFINITY;
			double thisDist;
			boolean material = thisNode.isFigure() && anotherNode.isFigure();
			for( Element thisNodeHolder : thisNode.elements )
			{
			   for( Node candidate : thisNodeHolder.getNodes() )
			      if( candidate != thisNode ){
			         if( material && !candidate.isFigure() )
			            continue;
			         thisDist = thisNode.distance(candidate) + anotherNode.distance(candidate);
			         if( thisDist < dist ){
			            nearest = candidate;
			            dist = thisDist;
			         }
			      }
			}		
			createElement(thisNode, nearest, anotherNode); 
			
			// fix another half of the gap
			outerLoop:
			for( Element nearestHolder : nearest.elements ){
				for( Node candidate : nearestHolder.getNodes() ){
					if( candidate != anotherNode && candidate != thisNode && candidate != nearest ){
						for( Element candidateHolder : candidate.elements ){
							for( Node connection : candidateHolder.getNodes() ) {
								if( connection == anotherNode ){
									for( Element exist : candidate.elements ){
										if( exist.isSuchElement(anotherNode, candidate, nearest) ) // this element already exist
											break outerLoop;
									}
									createElement( anotherNode, candidate, nearest );
									break outerLoop;
								}
							}
						}
					}
				}
			}
			
			return false;
		}
		
		/**
		 *Remove elements that are not inside of positive contour or are inside negative contour
		 */
		void cleanElements(List<Contour> contours)
		{
				Contour contour;
				Element el;
				boolean in;
				for(int i=elements.size()-1; i>=0; i--)//walk through elements
				{
						el = (Element)elements.get(i);
						in=false;
						for(int j=0; j<contours.size(); j++)//walk through contours
						{
								contour = (Contour)contours.get(j);
								if(contour.isInside( el.getCentralDot() ))
								{//Element inside the contour
										if(contour.isPositive())
										{
												in=true;
										}
										else
										{
												in=false;
												break;
										}
								}
						}//end walk through contours
						if(!in)
								el.delete();
				}//end of walk through elements
		}
		
		
		public Element findElementThatCovers(Dot dot)
		{
				for(Element el : elements)
				{
						if(el.isInside(dot)) 
								return el;
				}
				return null;
		}
		
		public void upgrade()
		{

		}
		
		/**
		 *Update index of each node so that index of node equals its place in list
		 */
		public void updateNodesIndexes()
		{
				int s = nodes.size();
				Node n;
				for(int i=0; i<s; i++)
				{
						n=(Node)nodes.get(i);
						n.index=i;
				}
		}

		public void updateElementsIndexes()
		{
			int s = elements.size();
			Element el;
			for(int i=0; i<s; i++)
			{
					el=(Element)elements.get(i);
					el.setIndex(i);
			}
		}

		/**
		 *For each node determine it it belongs to some CZone and to which exactly
		 *Skips nodes, that have non-null czone property
		 *For correct work of this function edge nodes of mesh must have their segment
		 *set to correct value
		 */
		public void determineCZones(fem.divider.figure.Figure figure)
		{this.determineCZones(figure, false);}
		/**
		 *Same as determineCzones, but allows to overwrite or not overwrite nodes, that already have czone property set 
		 */
		public void determineCZones(fem.divider.figure.Figure figure, boolean overwrite)
		{
			List<CZone> czones = new ArrayList<CZone>(3);
			
			//Collect CZones:
			//for all contours
			 for( fem.divider.figure.Contour contour : figure.getContours() ) {
			 	//for all nodes(segments) of contour
			 	for( fem.divider.figure.Node fnode : contour.nodes ) {
			 		fem.divider.figure.Segment seg = fnode.getNextSegment();
			 		//add all CZones from this segment to our list
			 		czones.addAll(seg.getCZones());
			 	}
			 }//end for contours
			 
			//Check nodes of CZones:
			//for all Nodes
			for(Iterator<Node> ni=nodes.iterator(); ni.hasNext(); )
			{
				Node node = (Node)ni.next();
				if( (node.getCzone()!=null||node.getPrevCzone()!=null)&&!overwrite ) continue;
				node.setCzone(null); node.setPrevCzone(null);
				 //for all CZones
				 for(Iterator<CZone> icz=czones.iterator(); icz.hasNext(); )
				 {
				 	CZone czone = (CZone)icz.next();
			 		//Check CZone
			 		node.checkCZone(czone);
			 	}
			 }
		}//end determineCZones(..)

		/**
		 * Sort nodes to optimize mesh
		 * (doesn't affect index property of nodes, so updateNodesindexes has to be called
		 * after this)
		 */
		public void sortNodes()
		{
			Node left_node, right_node, bottom_node, top_node, node;
			int ni, nn;
			nn=nodes.size();
			if(nn==0) return;
			node=(Node)nodes.get(0);
			left_node=right_node=bottom_node=top_node=node;
			for(ni=1;ni<nn;ni++)
			{
				node=(Node)nodes.get(ni);
				if(node.getX()<left_node.getX()) left_node=node;
					else
					if(node.getX()>right_node.getX()) right_node=node; 
				if(node.getY()<bottom_node.getY()) bottom_node=node;
					else
					if(node.getY()>top_node.getY()) top_node=node; 
			}
			
			Comparator<Node> comparator;
			if( right_node.getX()-left_node.getX() > top_node.getY()-bottom_node.getY() )
				{
					comparator = new Comparator<Node>()
					{
						public int compare(Node na, Node nb) {
							if( na.getX()<nb.getX() ) return -1;
							if( na.getX()>nb.getX() ) return 1;
							//x equals, compare y
							if( na.getY()<nb.getY() ) return -1;
							if( na.getY()>nb.getY() ) return 1;
							return 0;
						}
					};
				} 
				else 
				{comparator = new Comparator<Node>()
					{
						public int compare(Node na, Node nb)
						{
							if( na.getY()<nb.getY() ) return -1;
							if( na.getY()>nb.getY() ) return 1;
							//x equals, compare y
							if( na.getX()<nb.getX() ) return -1;
							if( na.getX()>nb.getX() ) return 1;
							return 0;
						}
					};
				}//end of checking widest direction
				
				Collections.sort(nodes, comparator);				
		}//end sortNodes()
		
		public List<Node> getNodes()
		{
			return nodes;
		}
		
		public int nNodes()
		{
				return nodes.size();
		}
		
		public List<Element> getElements()
		{
			return elements;
		}
		
		public int nElements()
		{
				return elements.size();
		}

}
