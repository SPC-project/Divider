/*
 * Element.java
 *
 * Created on Monday, 28, March 2005, 18:26
 */

package fem.divider.mesh;

import fem.common.IFemSettings;
import fem.divider.Divider;

import java.awt.Color;
import java.awt.Graphics2D;
import fem.geometry.Dot;
import fem.geometry.DotMaterial;
import fem.geometry.Triangle;

public class Element extends Triangle {
	private int index; //index in array of elements in mesh
    private Mesh mesh;
    private boolean valid = true;

    public static double INSIDE_ANGLE = Math.toRadians(1);
    public static double OUTER_UPGRADE_ANGLE = Math.toRadians(20);

    public static Color elementNumberColor = new Color(0, 100, 0);
    public static Color airElementColor    = new Color(43, 127, 176);
    public static Color elementInnerColor  = new Color(128, 128, 128, 50);

		/** Creates new Element
		 * and adds it to mesh of given node
		 */
    public Element(Node A_, Node B_, Node C_) {
    	// TODO Check if some of Nodes is equal
       super( A_, B_, C_ );
       mesh = A_.mesh;
       A_.add(this);
       B_.add(this);
       C_.add(this);
       mesh.elements.add(this);
    }

	 private void drawSide( Node start, Node end, Graphics2D g ){
	     Color col = g.getColor();
		  if( start.getMaterial() == DotMaterial.AIR || end.getMaterial() == DotMaterial.AIR )
            g.setPaint(airElementColor);
		  else
		     g.setPaint(Color.black);

        g.drawLine( mesh.panel.xsi( start.getX() ), mesh.panel.ysi( start.getY() ),
                    mesh.panel.xsi( end.getX() ),   mesh.panel.ysi( end.getY() )    );
        g.setPaint(col);
	 }
	 private void fillTriangle( Node first, Node second, Node third, Graphics2D g ){
	    if( !isFigure() )
	       return;
	    int[] xPoints = { mesh.panel.xsi(first.getX()), mesh.panel.xsi(second.getX()), mesh.panel.xsi(third.getX()) };
	    int[] yPoints = { mesh.panel.ysi(first.getY()), mesh.panel.ysi(second.getY()), mesh.panel.ysi(third.getY()) };
	    g.setPaint(elementInnerColor);
		g.fillPolygon( xPoints, yPoints, 3 );
	 }
    public void draw(Graphics2D g)
    {
       Node A = getNodes()[0];
       Node B = getNodes()[1];
       Node C = getNodes()[2];
       MeshPanel p = mesh.panel;
					
       if( Divider.getDivider().getPreferences().isFillFigureElements() )
    	   fillTriangle(A, B, C, g);
       drawSide(A, B, g);
       drawSide(B, C, g);
       drawSide(C, A, g);

       if(fem.divider.Divider.getDivider().getPreferences().isShowMeshElementNumbers()) {
          Color col = g.getColor();
			 g.setPaint(elementNumberColor);
			 g.drawString((index+1)+"" , p.xsi( getCentralDot().getX() ), p.ysi( getCentralDot().getY()  )); //$NON-NLS-1$
			 g.setPaint(col);
		 }
    }

    /**
     *Removes all known links to this element
     */
    public void delete()
    {
                    mesh.forget(this);
//				A.forget(this);
//				B.forget(this);
//				C.forget(this);
                    getNodes()[0].forget(this);
                    getNodes()[1].forget(this);
                    getNodes()[2].forget(this);

                    valid = false;
    }

    /**
     *Removes all known links to this element, 
     *except those that are in given node
     */
    public void deleteBy(Node node)
    {
                    mesh.forget(this);
                    if(node!=getNodes()[0])
                                    getNodes()[0].forget(this);
                    if(node!=getNodes()[1])
                                    getNodes()[1].forget(this);
                    if(node!=getNodes()[2])
                                    getNodes()[2].forget(this);

                    valid = false;
    }

//		public void replace

    public boolean hasNode(Node node)
    {
                    for(int i=0;i<3;i++)
                    {
                                    if(node==getNodes()[i]) return true;
                    }
                    return false;
    }

    public boolean isReallyInside(fem.geometry.Dot dot)
    {
                    if(!isInside(dot)) return false;
                    if( getNodes()[0].angle(dot, getNodes()[1]) < INSIDE_ANGLE) return false;
                    if( getNodes()[1].angle(dot, getNodes()[2]) < INSIDE_ANGLE) return false;
                    if( getNodes()[2].angle(dot, getNodes()[0]) < INSIDE_ANGLE) return false;
                    return true;
    }


    /**
     * @returns (on success) element that borders with this and not contain to node, that given
     * @returns (on failure) null
     */
    public Element oppositeOf(Node node)
    {
        Node node1;
        Node node2;
        if(getNodes()[0]!=node)
        {
            node1=getNodes()[0];
            if(getNodes()[1]!=node)
                node2=getNodes()[1];
            else
                node2=getNodes()[2];
        }
        else //getNodes()[0]==node
        {
            node1=getNodes()[1];
            node2=getNodes()[2];
        }
        
        for(Element el : node1.elements) {
            if(el==this)          continue;
            if(el.hasNode(node2)) return el;
        }
        return null;
    }

    /**
     *    /_\  =>  /|\
     *    \ /      \|/
     *@returns if it is allowed 
     */
    public boolean swapDiagonalWith(Element el)
    {
    	Node a,b,//common nodes
    	c1, c2;//individual nodes
    	if(!el.hasNode(getNodes()[0]))
    	{
    		c1 = getNodes()[0];
    		a = getNodes()[1];
    		b = getNodes()[2];
    	}
    	else if(!el.hasNode(getNodes()[1]))
    	{
    		c1 = getNodes()[1];
    		a = getNodes()[2];
    		b = getNodes()[0];
    	}
    	else
    	{
    		c1 = getNodes()[2];
    		a = getNodes()[0];
    		b = getNodes()[1];
    	}
    	if(!hasNode(el.getNodes()[0]))
    		c2 = el.getNodes()[0];
    	else if(!hasNode(el.getNodes()[1]))
    		c2 = el.getNodes()[1];
    	else
    		c2 = el.getNodes()[2];

    	// Prevent swapping of inner bound's elements (the bound will be destroyed, if it was a-b and we swap)
    	if( a.isFigure() && b.isFigure() && ( c1.isFigure() ^ c2.isFigure()) )
    	   return false;
    	
    	double angle = 0;
    	//c2 a c1 b
    	angle+=a.angle(c1, c2);
    	angle+=c1.angle(a, b);
    	angle+=b.angle(c1, c2);
    	angle+=c2.angle(b,a);
    	if( angle<2*Math.PI-IFemSettings.GENERAL_ACCURACY )
    		return false;
    
    	//replace nodes
    	getNodes()[0]=c1;
    	getNodes()[1]=a;
    	getNodes()[2]=c2;

    	el.getNodes()[0]=c2;
    	el.getNodes()[1]=b;
    	el.getNodes()[2]=c1;

    	//update element lists in nodes
    	//ex-this
    	c1.add(el);
    	a.forget(el);
    	b.forget(this);
    	//ex-el
    	c2.add(this);
    	return true;
    }

    /**
     * Get third node of element or null 
     * 
     * @param n1
     * @param n2
     * @return
     */
    private Node getThirdNode(Node n1, Node n2)
    {
                    for(int i=0; i<3; i++)
                    {
                                    if(getNodes()[i]!=n1&&getNodes()[i]!=n2)
                                                    return getNodes()[i];
                    }
                    return null;
    }

//		static int i =0;

    /**
     * Split element info smaller elements.
	  * If needed result[1] has added elements 
     * If these was no upgrade returns zero-size array
     * @returns true - if the element upgraded
     */
    public boolean upgrade()
    {
    	boolean isNeedUpgrade = valid && ( 
    			getArea() < mesh.settings.maxArea*areaShrink() 
    			&& getAngleValue(getMinAngleIndex()) > mesh.settings.getMinAngle());
    	if( isNeedUpgrade || getArea() < mesh.settings.minArea)
    		return false; //area OK
    	
    	boolean isOuter = !isInside(circleCenter()) || getAngleValue(getMinAngleIndex()) < OUTER_UPGRADE_ANGLE;
    	if( isOuter )
    		return outerUpgrade();
    	else
    		return innerUpgrade();
    }

    /**
     * New node inside the element
     */
    private boolean innerUpgrade()
    {
        Dot cdot = getCentralDot();
        Node cnode = new Node(mesh, cdot);
        
        mesh.createElement(getNodes()[0], getNodes()[1], cnode);
        mesh.createElement(getNodes()[1], getNodes()[2], cnode);
        mesh.createElement(getNodes()[2], getNodes()[0], cnode);
        this.delete();
        
        cnode.lawson();
        
        return true;
    }

    /**
     * New node on element border
     */
    private boolean outerUpgrade()
    {
       int maxAngIndx = getMaxAngleIndex(); // index of corner with biggest angle
       Node nodeMax = getNodes()[maxAngIndx];

       Node node1 = getNodes()[getOtherCorner1Index(maxAngIndx)];
       Node node2 = getNodes()[getOtherCorner2Index(maxAngIndx)];

       // Get opposite element that: 1) border with this element, 2) didn't include 'modeMax' node
       Element op4 = this.oppositeOf(nodeMax);
       if(op4!=null) {
          int op4max = op4.getMaxAngleIndex(); Node op4Nmax = op4.getNodes()[op4max];
          int op4_1 =  op4.getOtherCorner1Index(op4max); Node op4N1 = op4.getNodes()[op4_1];
          int op4_2 = op4.getOtherCorner2Index(op4max); Node op4N2 = op4.getNodes()[op4_2];
          //check if we won't make the opposite element worse
          if( op4Nmax.angle(op4N1, op4N2) > (3*Math.PI/4) && 
                (op4Nmax == node1 || op4Nmax == node2) ) return false; //not upgraded 
       }

       // Split opposite to 'nodeMax' edge in middle point
       Node nodeMid = new Node(node1, node2, 0.5, op4==null?true:false ); 
       mesh.createElement(nodeMid, node2, nodeMax);
       mesh.createElement(nodeMid, nodeMax, node1);
       this.delete();

       if(op4!=null){ //we have opposite element, let's split it too
          Node opTo_nodeMax = op4.getThirdNode(node1, node2);

          mesh.createElement(opTo_nodeMax, node2, nodeMid);
          mesh.createElement(nodeMid, node1, opTo_nodeMax);

          op4.delete();
       }

       nodeMid.lawson();
       return true;
    }

    /**
     *Returns coefficient (0...1] , that tells maximal allowed size
     *of this element (relatively to mesh.settings.maxArea)
     */
    private double areaShrink()
    {
                    double leastOriginalAngle=Math.PI; //180 degres
                    for(int i=0; i<2; i++)
                    {
                                    if(getNodes()[i].original)
                                    {
                                                    if(getNodes()[i].originalAngle < leastOriginalAngle)
                                                                    leastOriginalAngle=getNodes()[i].originalAngle;
                                    }
                    }

                    return leastOriginalAngle/Math.PI;
    }

    public int getIndex() {
            return index;
    }

    void setIndex(int i) {
            index = i;
    }

    public void setNodes(Node[] nodes)
    {
            setCorners(nodes);
    }

    @Override
    protected Dot[] createCornersArray() {
        return new Node[3];
    }
		
    public boolean isFigure() {
		Node[] nodes = getNodes();
		
		return nodes[0].isFigure() && nodes[1].isFigure() && nodes[2].isFigure();
	 }            
                
    public Node[] getNodes()
    {
    	return (Node[])getCorners();
    }
    
    public boolean isSuchElement(Node first, Node second, Node third){
    	Node[] mine = getNodes();
    	if( first == mine[0] || first == mine[1] || first == mine[2] )
    		if( second == mine[0] || second == mine[1] || second == mine[2] )
    			if( third == mine[0] || third == mine[1] || third == mine[2] )
    				return true;
    	return false;
    }
}//end class
