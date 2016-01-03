/*
 * DefaultSegment.java
 *
 * Created on Saturday, 19, March 2005, 13:54
 */

package fem.divider.figure;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import fem.divider.*;
import fem.geometry.Dot;

/**
 * 
 * @author gefox
 * @version
 */
public class Segment {

	/** Creates new DefaultSegment */
	public Segment(Node begin_node, Node end_node) {
		popupMenu = new JPopupMenu();
		begin = begin_node;
		end = end_node;
		begin.setNextSegment(this);// set this as next segment of begin node
		end.setPrevSegment(this);//and prev ious of end node
		// Also initiates fillMenu through updateContour()
	}

	private void fillMenu() {
		popupMenu.removeAll();
		joinToMenu(popupMenu);
		popupMenu.addSeparator();
		if (begin.contour != null)
			begin.contour.joinToMenu(popupMenu);
	}

	/**
	 * TODO: wat has i written here? I wish i knew. :-)
	 */
	public void updateContour() {
		if (popupMenu != null)
			;
		fillMenu();
	}

	public void draw(Graphics2D graphics) {
		geometry.draw(this, graphics);
	}// end of draw


	public double distance(Dot dot) {
		return geometry.distance(this, dot);
	}

	/**
	 * @returns either begin node, or end node, if one of the nodes is in the
	 *          handle_ zone of (x_;y_) point. Otherwise returns null
	 */
	public Node findNode(double x_, double y_, double handle_) {
		if (begin.distance(x_, y_) < handle_)
			return begin;
		if (end.distance(x_, y_) < handle_)
			return end;
		return null;
	}

	/**
	 * get distance from segment to point
	 */
	public double distance(double x, double y) {
		return distance(new Dot(x, y));
	}

	/**
	 * Get relative x coordinate of segment point, shifted by offset. 
	 * Relative to segment begin. 
	 * 
	 * @return x coordinate shift of the point, shifted by "offset" distance
	 *         from begin in segment direction
	 */
	public double shift_x(double offset) {
		return geometry.shiftDot(this, offset).getX();
	}

	/**
	 * Get relative y coordinate of segment point, shifted by offset. 
	 * Relative to segment begin. 
	 * 
	 * @return y coordinate shift of the point, shifted by "offset" distance
	 *         from begin in segment direction
	 */
	public double shift_y(double offset) {
		return geometry.shiftDot(this, offset).getY();		
	}

	public void setNewBegin(Node begin_) {
		begin.unsetNextSegment();
		begin = begin_;
	}

	public void setNewEnd(Node end_) {
		end.unsetPrevSegment();
		end = end_;
	}

	public Map getProperties() {
		return null;
	}

	/**
	 * Add to popupMenu items, connected with this segment
	 */
	void joinToMenu(JPopupMenu popupMenu) {
		popupMenu.add(splitSegmentAction);
		popupMenu.add(splitSegmentByMiddleAction);
		popupMenu.add(editCZonesAction);
		popupMenu.add(editGeometryAction);
	}

	public RectangleArea calculateBounds() {
		return geometry.calculateBounds(this);
	}

	public int countRightXRayIntersections(Dot dot) {
		return geometry.countRightXRayIntersections(this, dot);
	}

	public ArrayList copyCZones() {
		int s = czones.size();
		ArrayList list = new ArrayList(s);
		int i;
		CZone czone;
		for (i = 0; i < s; i++) {
			czone = (CZone) czones.get(i);
			list.add(czone.clone());
		}
		return list;
	}

	/**
	 * Returns czones list "as is"
	 */
	public ArrayList<CZone> getCZones() {
		return czones;
	}

	/**
	 * Sets czones list to what is given, and sets segment of czones to this
	 */
	public void setCZones(ArrayList czones_) {
		czones = czones_;
		for (Iterator i = czones.iterator(); i.hasNext();) {
			((CZone) (i.next())).setSegment(this);
		}
	}

	public void addCZone(CZone zone_) {
		czones.add(zone_);
		zone_.setSegment(this);
	}

	Segment get_me() {
		return this;
	}

	public boolean hasNode(Node node_) {
		if (begin == node_ || end == node_)
			return true;
		else
			return false;
	}

	public double getLength() {
		return geometry.getLength(this);
	}

	/**
	 * Segment direction in specified place.
	 * @param offset offset from the begin of segment
	 */
	public double[] direction(double offset) {
		Dot direction = geometry.getDirection(this, offset);
		double vec[] = { direction.getX(), direction.getY() };

		return vec;
	}

	
	/**
	 * Appends dots of this segment (in offset ascending order) 
	 * to given list and returns
	 * number of added dots.
	 * Dots of a segment are beginning node, edges of contact zones
	 * and 
	 * 
	 * @param dots list of dots
	 * @return number of added dots
	 */
	public int appendDotsToList(List<Dot> dots) {
		int nAdded =0;
		//add node to list
		dots.add(begin);
		nAdded++;
		//look for czone edges and add them to list
//		czones=segment.getCZones();
		CZone czone;
		double x, y;
		CZMark mark1 = null;
		CZMark mark2 = null;
		Iterator<CZone> czi = czones.iterator(); 
		while(czi.hasNext()) //for czones of segment
		{
			czone=czi.next();
			
			//need to put CZMark on czone begin
			if(czone.getOffsetMode()==CZone.OFFSET_VAL &&
					czone.getOffsetVal()<getLength()) 
			{
				x=getBegin().getX()+shift_x( czone.getOffsetVal() );
				y=getBegin().getY()+shift_y( czone.getOffsetVal() );
				mark1 = new CZMark(x, y, czone, czone.getOffsetVal());
			}
			
			//need to put CZMark on czone end
			if(czone.getLengthMode()==CZone.LENGTH_VAL &&
					czone.getRealOffset()+czone.getLengthVal()<getLength()) 
			{
				x=getBegin().getX()+shift_x( czone.getOffsetVal()+czone.getLengthVal() );
				y=getBegin().getY()+shift_y( czone.getOffsetVal()+czone.getLengthVal() );
				mark2 = new CZMark(x, y, czone, czone.getOffsetVal()+czone.getLengthVal());
			}
		} //end for czones of segment
		nAdded += appendCurveDotsToList(dots, mark1, mark2);
		return nAdded;	
	}

	/**
	 * If segment is curve, represents it as a polyline and 
	 * adds its points to list of dots.
	 * If mark1 or mark2 not null, adds them in appropriate place
	 * (according to offset)
	 * Returns number of added dots (mark1 and mark2 also included 
	 * in this number).
	 *  
	 * @param dots
	 * @param mark1
	 * @param mark2
	 * @return
	 */
	private int appendCurveDotsToList(List<Dot> dots, CZMark mark1, CZMark mark2) {
		int nAdded = 0;
		boolean addedMark1 = false;
		boolean addedMark2 = false;
		if(geometry instanceof LineSegmentGeometry) {
			//linear
		} else {
			//curve
			double step = begin.contour.figure.getMeshSettings().getCurveQuantLength();
			int nSteps = (int)Math.ceil(getLength()/step);
			step = getLength()/nSteps;
			double x, y;
			double offset = step;
			CurveMark curveMarkNew = null;
			CurveMark curveMarkOld = null;
			//create&add curve marks (need nSteps-1 items, that's why begin from 1)
			for(int i=1; i<nSteps; i++) {
				x=getBegin().getX()+shift_x(offset);
				y=getBegin().getY()+shift_y(offset);
				curveMarkNew = new CurveMark(x, y, this, offset);
				//TODO: think about nearly situated curve and czone marks
				//add czone marks
				if(curveMarkOld!=null) {
					if(mark1 != null &&
							curveMarkOld.getOffset() < mark1.getOffset()&&
							mark1.getOffset() < curveMarkNew.getOffset()) {
						dots.add(mark1);
						nAdded++;
						addedMark1 = true;
					}
					if(mark2 != null &&
							curveMarkOld.getOffset() < mark2.getOffset()&&
							mark2.getOffset() < curveMarkNew.getOffset()) {
						dots.add(mark2);
						nAdded++;
						addedMark2 = true;
					}
				} else {
					if(mark1 != null &&
							mark1.getOffset() < curveMarkNew.getOffset()) {
						dots.add(mark1);
						nAdded++;
						addedMark1 = true;
					}
					if(mark2 != null &&
							mark2.getOffset() < curveMarkNew.getOffset()) {
						dots.add(mark2);
						nAdded++;
						addedMark2 = true;
					}					
				}
				//end add czone marks
//				if(nAdded>0)
//					{
					dots.add(curveMarkNew);
//					}
				curveMarkOld = curveMarkNew;
				nAdded++;
				offset += step;
			}
			//end create&add curve marks
		}//end if curve
		if(!addedMark1 && mark1 != null) {
			dots.add(mark1);
			nAdded++;
		}
		if(!addedMark2 && mark2 != null) {
			dots.add(mark2);
			nAdded++;
		}
		return nAdded;
	}
	
	/**
	 * Calls dialog for contact zones editing
	 * 
	 * @return true on ok, false on cancel or failure
	 */
	public boolean editCZones() {
		return czone_dialog.run(this);
	}

	/**
	 * Calls dialog for geometry editing
	 * 
	 * @return true on ok, false on cancel or failure
	 */
	public boolean editGeometry() {
		return geometryEditor.run(this);
	}

	public Node getBegin() {
		return begin;
	}

	public Node getEnd() {
		return end;
	}

	public Segment getThisSegment() {
		return this;
	}

	Action splitSegmentAction = new AbstractAction(Messages
			.getString("DefaultSegment.Split_Segment_2"), //$NON-NLS-1$
			new ImageIcon(Divider.class
					.getResource("resources/images/splitSegment.png")) //$NON-NLS-1$
	) {
		public void actionPerformed(ActionEvent event) {
			/*
			 * Node node = Node.interactiveCreate(); if(node==null) return;
			 * begin.contour.addAfter(begin, node);
			 */
			if (begin.contour.figure.getCommandStack().doNewCommand(
					new fem.divider.figure.command.SplitSegmentCommand(
							begin.contour.figure, getThisSegment())))
				// begin.contour.figure.panel.redraw();
				begin.contour.figure.redraw();
		}
	};
	
	Action splitSegmentByMiddleAction = new AbstractAction( "Split by middle point", //$NON-NLS-1$
         new ImageIcon(Divider.class.getResource("resources/images/splitSegment.png")) //$NON-NLS-1$
   ) {
      public void actionPerformed(ActionEvent event) {
         if (begin.contour.figure.getCommandStack().doNewCommand(
               new fem.divider.figure.command.SplitSegmentCommand(
                     begin.contour.figure, getThisSegment(), true)))
            // begin.contour.figure.panel.redraw();
            begin.contour.figure.redraw();
      }
   };

	Action editCZonesAction = new AbstractAction(Messages
			.getString("DefaultSegment.edit_contact_zones_4"), //$NON-NLS-1$
			new ImageIcon(Divider.class
					.getResource("resources/images/czone.png")) //$NON-NLS-1$
	) {
		public void actionPerformed(ActionEvent event) {

			if (begin.contour.figure.getCommandStack().doNewCommand(
					new fem.divider.figure.command.EditContactZonesCommand(
							begin.contour.figure, getThisSegment()))) {
				begin.contour.figure.panel.redraw();
			}
		}

		{
			putValue(
					Action.SHORT_DESCRIPTION,
					Messages
							.getString("DefaultSegment.Edit_contact_zones_of_this_segment_6")); //$NON-NLS-1$
		}

	};

	Action editGeometryAction = new AbstractAction(Messages
			.getString("DefaultSegment.EditGeometry") //$NON-NLS-1$
	// ,new ImageIcon(Divider.class.getResource("resources/images/czone.png"))
	// //$NON-NLS-1$
	) {
		public void actionPerformed(ActionEvent event) {
			if (begin.contour.figure.getCommandStack().doNewCommand(
					new fem.divider.figure.command.EditSegmentGeometryCommand(
							begin.contour.figure, getThisSegment()))) {
				begin.contour.figure.panel.redraw();
			}
		}

		{
			putValue(Action.SHORT_DESCRIPTION, Messages
					.getString("DefaultSegment.DescriotionEditGeometry")); //$NON-NLS-1$
		}

	};

	public AbstractSegmentGeometry getGeometry() {
		return geometry;
	}

	public void setGeometry(AbstractSegmentGeometry geometry) {
		this.geometry = geometry;
	}

	public int getSegmentIndex() {
		return segmentIndex;
	}

	public void setSegmentIndex(int segmentIndex) {
		this.segmentIndex = segmentIndex;
	}
	
	private Node begin;
	private Node end;
	private AbstractSegmentGeometry geometry = new LineSegmentGeometry();
	
	private int segmentIndex;
	


	JPopupMenu popupMenu;
	ArrayList<CZone> czones = new ArrayList<CZone>();
	static CZonesEditDialog czone_dialog = new CZonesEditDialog();
	static SegmentGeometryEditor geometryEditor = new SegmentGeometryEditor();
}





class CZonesEditDialog extends fem.divider.AbstractEditingDialog {
	CZonesEditDialog() {
		super(Messages.getString("DefaultSegment.Edit_Contact_Zones_7")); //$NON-NLS-1$
		errorMessageTitle = Messages
				.getString("DefaultSegment.Contact_Zones_Editing_Error_8"); //$NON-NLS-1$
		tableModel = new CZoneTableModel(this);
		table = new JTable(tableModel); // create and set up table
		table.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		TableColumn offsetModeCol = table.getColumnModel().getColumn(2);
		OffsetModeComboBox = new JComboBox();
		OffsetModeComboBox.addItem(CZone.OFFSET_MODE_DESCRIPTION[0]);
		OffsetModeComboBox.addItem(CZone.OFFSET_MODE_DESCRIPTION[1]);
		OffsetModeComboBox.addItem(CZone.OFFSET_MODE_DESCRIPTION[2]);
		offsetModeCol.setCellEditor(new DefaultCellEditor(OffsetModeComboBox));

		TableColumn lengthModeCol = table.getColumnModel().getColumn(4);
		LengthModeComboBox = new JComboBox();
		LengthModeComboBox.addItem(CZone.LENGTH_MODE_DESCRIPTION[0]);
		LengthModeComboBox.addItem(CZone.LENGTH_MODE_DESCRIPTION[1]);
		LengthModeComboBox.addItem(CZone.LENGTH_MODE_DESCRIPTION[2]);
		lengthModeCol.setCellEditor(new DefaultCellEditor(LengthModeComboBox));

		JButton delButton = new JButton(Messages
				.getString("DefaultSegment.Delete_9")); //$NON-NLS-1$
		JButton addButton = new JButton(Messages
				.getString("DefaultSegment.Add_10")); //$NON-NLS-1$

		// button: Add CZone
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (segment == null)
					return;
				else
					// segment.czones.add(new CZone(segment));
					tmpCZones.add(new CZone(segment));
				tableModel.fireTableDataChanged();
				return;
			}
		});

		// button: remove CZone
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int rows[];
				if (segment == null)
					return;
				else
					rows = table.getSelectedRows();
				if (rows.length == 0)
					return;
				// segment.czones.remove(rows[0]);
				tmpCZones.remove(rows[0]);
				tableModel.fireTableDataChanged();
				return;
			}
		});

		addButton.setMaximumSize(new Dimension(100, (int) addButton
				.getPreferredSize().getHeight()));
		Box b1 = Box.createHorizontalBox();
		Box b2 = Box.createVerticalBox();
		getContentPane().add(b1, BorderLayout.CENTER);
		b1.add(new JScrollPane(table));
		b1.add(b2);
		b2.add(delButton);
		b2.add(addButton);

		/*
		 * positiveCheckBox = new JCheckBox("Is the contour positive (adds area
		 * to figure)?", true); contentPanel.add(positiveCheckBox);
		 */
		setSize(640, 200);
	}

	protected String onOk() {
		// check intersections
		CZone zone1, zone2;
		int n = tmpCZones.size();
		for (int i = 0; i < n; i++) {
			zone1 = (CZone) tmpCZones.get(i);
			for (int j = i + 1; j < n; j++) {
				zone2 = (CZone) tmpCZones.get(j);
				if (zone1.intersects(zone2))
					return Messages
							.getString("DefaultSegment.Foud_contact_zones_intersection._11"); //$NON-NLS-1$
			}
		}
		// segment.setPositive(positiveCheckBox.isSelected());
		segment.setCZones(new ArrayList(tmpCZones));
		return null;
	}// end onOk()

	boolean run(Segment segment_) {
		segment = segment_;
		// tmpCZones.clear();
		// tmpCZones.addAll( segment.getCZones() );
		tmpCZones = segment.copyCZones();
		// System.out.println("This segment has "+segment.czones.size()+"
		// czones");
		tableModel.fireTableDataChanged();
		show();
		// tableModel.fireTableCellUpdated(table.getSelectedRow(),
		// table.getSelectedColumn());
		if (result)// apply result if OK button pressed
			segment.setCZones(tmpCZones);
		return result;
	}

	JTable table;
	JComboBox OffsetModeComboBox;
	JComboBox LengthModeComboBox;
	Segment segment;
	CZoneTableModel tableModel;
	ArrayList tmpCZones = new ArrayList();

	private class CZoneTableModel extends AbstractTableModel {
		CZoneTableModel(CZonesEditDialog dialog_) {
			dialog = dialog_;
		}

		public int getColumnCount() {
			return 6;
		}

		public int getRowCount() {
			/*
			 * if(dialog.segment==null) return 0; else return
			 * dialog.segment.czones.size();
			 */
			return tmpCZones.size();
		}

		/*
		 * Get cell values
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (dialog.segment == null)
				return null;
			// CZone zone = (CZone)dialog.segment.czones.get(rowIndex);
			if (rowIndex >= tmpCZones.size())
				return null;
			CZone zone = (CZone) tmpCZones.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return zone.getName();
			case 1:
				return zone.getGroupName();
			case 2:
				return CZone.OFFSET_MODE_DESCRIPTION[zone.getOffsetMode()];
			case 3:
				if (zone.getOffsetMode() == CZone.OFFSET_VAL)
					return "" + zone.getOffsetVal(); //$NON-NLS-1$
				else
					return "---"; //$NON-NLS-1$
			case 4:
				return CZone.LENGTH_MODE_DESCRIPTION[zone.getLengthMode()];
			case 5:
				if (zone.getLengthMode() == CZone.LENGTH_VAL)
					return "" + zone.getLengthVal(); //$NON-NLS-1$
				else
					return "---"; //$NON-NLS-1$

			}
			return rowIndex + ":" + columnIndex; //$NON-NLS-1$
		}

		/*
		 * Set table cells values
		 */
		public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
			if (dialog.segment == null)
				return;
			if (rowIndex >= tmpCZones.size())
				return;
			// CZone zone = (CZone)dialog.segment.czones.get(rowIndex);
			CZone zone = (CZone) tmpCZones.get(rowIndex);
			String str;
			switch (columnIndex) {

			case 0: // Set Zone Name
				str = (String) newValue;
				if (str.length() == 0 || str.indexOf(" ") >= 0) // bad zone name //$NON-NLS-1$
																// //$NON-NLS-1$
				{
					JOptionPane
							.showMessageDialog(
									null,
									Messages
											.getString("DefaultSegment.Zone_name_should_not_be_empty_or_contain_spaces_18"), //$NON-NLS-1$
									Messages
											.getString("DefaultSegment.Bad_zone_name_19"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				} else // all right
				{
					zone.setName((String) newValue);
				}
				break;
			case 1: // Set Group Name
				str = (String) newValue;
				if (str.length() == 0 || str.indexOf(" ") >= 0) // bad group //$NON-NLS-1$
																// name
																// //$NON-NLS-1$
				{
					JOptionPane
							.showMessageDialog(
									null,
									Messages
											.getString("DefaultSegment.Group_name_should_not_be_empty_or_contain_spaces_21"), //$NON-NLS-1$
									Messages
											.getString("DefaultSegment.Bad_group_name_22"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				} else // all right
				{
					zone.setGroupName((String) newValue);
				}
				break;
			case 2: // Set Offset Mode
			// System.out.println("sel
			// item#="+dialog.OffsetModeComboBox.getSelectedIndex());
				if (dialog.OffsetModeComboBox.getSelectedIndex() == -1)
					break;
				zone
						.setOffsetMode(dialog.OffsetModeComboBox
								.getSelectedIndex());
				tableModel.fireTableDataChanged();
				break;
			case 3: // set Offset Value
				try {
					double val = Double.parseDouble((String) newValue);
					if (val < 0)
						throw new NumberFormatException();
					zone.setOffsetVal(val);
				} catch (NumberFormatException e) {
					JOptionPane
							.showMessageDialog(
									null,
									(String) newValue
											+ Messages
													.getString("DefaultSegment._is_not_a_valid_positive_floating-point_number_23"), //$NON-NLS-1$
									Messages
											.getString("DefaultSegment.Wrong_format_24"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
				}
				break;
			case 4: // Set Length Mode
			// System.out.println("sel
			// item#="+dialog.LengthModeComboBox.getSelectedIndex());
				if (dialog.LengthModeComboBox.getSelectedIndex() == -1)
					break;
				/*
				 * if(zone.getOffsetMode()==CZone.OFFSET_VAL &&
				 * dialog.LengthModeComboBox.getSelectedIndex() ==
				 * CZone.LENGTH_ZERO) //zero length and offset, given by value,
				 * are not compatible { JOptionPane.showMessageDialog(null,
				 * Messages.getString("DefaultSegment.When_offset_is_given_by_value,_zero_length_can__t_be_applied_25"),
				 * //$NON-NLS-1$
				 * Messages.getString("DefaultSegment.Irellevant_length_mode_26"),
				 * JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ } else
				 */
				{
					zone.setLengthMode(dialog.LengthModeComboBox
							.getSelectedIndex());
				}
				tableModel.fireTableDataChanged();
				break;
			case 5: // Set Length Value
				try {
					double val = Double.parseDouble((String) newValue);
					if (val < 0)
						throw new NumberFormatException();
					zone.setLengthVal(val);
				} catch (NumberFormatException e) {
					JOptionPane
							.showMessageDialog(
									null,
									(String) newValue
											+ Messages
													.getString("DefaultSegment._is_not_a_valid_positive_floating-point_number_27"), //$NON-NLS-1$
									Messages
											.getString("DefaultSegment.Wrong_format_28"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
				}
				break;
			}

		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (dialog.segment == null)
				return false;
			// CZone zone = (CZone)dialog.segment.czones.get(rowIndex);
			CZone zone = (CZone) tmpCZones.get(rowIndex);
			switch (columnIndex) {
			case 3:
				if (zone.getOffsetMode() == CZone.OFFSET_VAL)
					return true;
				else
					return false;
			case 5:
				if (zone.getLengthMode() == CZone.LENGTH_VAL)
					return true;
				else
					return false;
			}

			return true;
		}

		public String getColumnName(int c) {
			if (c == 0)
				return Messages.getString("DefaultSegment.Name_29"); //$NON-NLS-1$
			c--;
			if (c == 0)
				return Messages.getString("DefaultSegment.Group_Name_30"); //$NON-NLS-1$
			c--;
			if (c == 0)
				return Messages.getString("DefaultSegment.Offset_Mode_31"); //$NON-NLS-1$
			c--;
			if (c == 0)
				return Messages.getString("DefaultSegment.Offset_Value_32"); //$NON-NLS-1$
			c--;
			if (c == 0)
				return Messages.getString("DefaultSegment.Length_Mode_33"); //$NON-NLS-1$
			c--;
			return Messages.getString("DefaultSegment.Length_Value_34"); //$NON-NLS-1$
		}

		CZonesEditDialog dialog;
	}// end of class CZoneTableModel
}// end of class CZonesEditDialog

