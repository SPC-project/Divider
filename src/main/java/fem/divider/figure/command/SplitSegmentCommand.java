/*
 * Created on 26/6/2006
 */
package fem.divider.figure.command;

import fem.divider.Messages;
import fem.divider.figure.*;
import fem.geometry.DotMaterial;

/**
 * @author gefox
 */
public class SplitSegmentCommand extends AbstractCommand {

	/**
	 * @param _figure
	 */
	public SplitSegmentCommand(Figure _figure, Segment _segment) {
		super(_figure);
		segment=_segment;
		silent = false;
	}
	public SplitSegmentCommand(Figure _figure, Segment _segment, boolean _silent) {
      super(_figure);
      segment=_segment;
      silent = _silent; // if true - generate new node without popup menus
   }

	boolean execute()
	{
	   Node node = null;
	   if( silent ){
   		Node n1 = segment.getBegin();
   		Node n2 = segment.getEnd();
   		double x = ( n1.getX() + n2.getX() ) / 2.0;
   		double y = ( n1.getY() + n2.getY() ) / 2.0;
   		DotMaterial m = DotMaterial.AIR;
   		if( n1.getMaterial() == n2.getMaterial() && n1.getMaterial() == DotMaterial.FIGURE ){
   			m = DotMaterial.FIGURE;
   		}
   		node = new Node(x, y, m);
	   } else {
	      node = Node.interactiveCreate( 0.0, 0.0, segment.getEnd().getMaterial() );
	   }
	   
		if(node==null) return false;
		segment.getBegin().getContour().addAfter(segment.getBegin(), node);
		newNode_stateAfter = node;
		return true;
	}
	
	private Segment segment;

	/* (non-Javadoc)
	 * @see divider.figure.command.AbstractCommand#name()
	 */
	public String getName() {
		return Messages.getString("SplitSegmentCommand.split_segment_1"); //$NON-NLS-1$
	}

	public boolean isUndoable() {return true;}
	
	/* (non-Javadoc)
	 * @see divider.figure.command.AbstractCommand#undo()
	 */
	void undo() {
		Contour contour = newNode_stateAfter.getContour();
		contour.deleteNode( newNode_stateAfter );
		figure.redraw();
	}

	/* (non-Javadoc)
	 * @see divider.figure.command.AbstractCommand#redo()
	 */
	void redo() {
		Contour contour = newNode_stateAfter.getContour();
		contour.restoreNode( newNode_stateAfter );
		figure.redraw();
	}
	
	private Node newNode_stateAfter;
	private boolean silent;
}
