/*
 * DividerUI.java
 *
 * Created on Saturday, 19, March 2005, 10:48
 */

package fem.divider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import fem.common.IFemSettings;
import fem.divider.figure.FigurePanel;
import fem.divider.mesh.MeshPanel;
import fem.divider.mesh.MethodAbstract;

/**
 * 
 * @author  Paul Lysak
 */
public class DividerUI extends javax.swing.JFrame {
   private static final long serialVersionUID = 1L;
   private javax.swing.JToolBar toolBar;
	private javax.swing.JTabbedPane tabbedPane;

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu viewMenu;
	private JMenu meshMenu;
	private JMenu measureMenu;
	private JTextField statusField;
	
	private JCheckBoxMenuItem showMENumbers;
	private JCheckBoxMenuItem showMNNumbers;
	private JCheckBoxMenuItem fillFigureElements;
		
	private JFileChooser fileChooser;
	private JFileChooser meshFileChooser;
	private JFileChooser measureFileChooser;
	
	Divider divider;
	
	FigurePanel figurePanel;
	MeshPanel meshPanel;
	
	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 480;
	public static final int DEFAULT_X = 10;
	public static final int DEFAULT_Y = 10;
	public static final String packedMeshExtension = "pmd";
	
		/** Creates new form DividerUI */
    public DividerUI(Divider d) {
        initControls();

        divider = d;

        //default window parameters
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocation(DEFAULT_X, DEFAULT_Y);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);


        initMainMenu();

        initToolbar();

        figurePanel = new FigurePanel (divider.figure, divider.world);
        meshPanel = new MeshPanel(divider.world);

        tabbedPane.addTab(Messages.getString("DividerUI.Figure_5"), figurePanel); //$NON-NLS-1$
        tabbedPane.addTab(Messages.getString("DividerUI.Mesh_6"), meshPanel); //$NON-NLS-1$

        statusField = new JTextField();
        statusField.setEditable(false);
        getContentPane().add(statusField, java.awt.BorderLayout.SOUTH);


        initFileChoosers();
    }
    
    
    
    private JMenu createMeshdownMethodMenu()
    {
		ButtonGroup  methodGroup=new ButtonGroup();
		JMenu methodMenu=new JMenu(Messages.getString("DividerUI.Method_1")); //$NON-NLS-1$
		
		int i, n, i_;
		MethodAbstract methods[]=divider.meshdownActor.getMethods();
		i_=divider.meshdownActor.getMethodIndex();
		n=methods.length;
		for(i=0; i<n; i++)
		{
			JRadioButtonMenuItem methodItem = new JRadioButtonMenuItem(methods[i].getName());
			methodGroup.add(methodItem);
			methodMenu.add(methodItem);
			if(i==i_) methodItem.setSelected(true); 
				else methodItem.setSelected(false);  

			methodItem.addActionListener( 
				new MethodButtonListener(divider.meshdownActor,i));						
		}//end for all methods
		return methodMenu;
    }
    
    
    
    
    private void initMainMenu()
    {
		showMENumbers = new JCheckBoxMenuItem(Messages.getString("DividerUI.Show_mesh_elements_numbers_1")); //$NON-NLS-1$
		if(divider.getPreferences().isShowMeshElementNumbers()) showMENumbers.setState(true);
		showMNNumbers = new JCheckBoxMenuItem(Messages.getString("DividerUI.Show_mesh_nodes_numbers_2")); //$NON-NLS-1$
		if(divider.getPreferences().isShowMeshNodeNumbers()) showMNNumbers.setState(true);
		fillFigureElements = new JCheckBoxMenuItem(Messages.getString("DividerUI.Fill_figure_elements"));
		if(divider.getPreferences().isFillFigureElements()) fillFigureElements.setState(true);
		showMENumbers.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					divider.getPreferences().setShowMeshElementNumbers(showMENumbers.getState());
					meshPanel.redraw();
				}
			}
		);//end call showMENumbers.addActionListener 

		showMNNumbers.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					divider.getPreferences().setShowMeshNodeNumbers(showMNNumbers.getState());
					meshPanel.redraw();
				}
			}
		);//end call showMNNumbers.addActionListener 
		
		fillFigureElements.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent event)
					{
						divider.getPreferences().setFillFigureElements(fillFigureElements.getState());
						meshPanel.redraw();
					}
				}
		);
	    	
    	
		setJMenuBar(menuBar = new JMenuBar());
		menuBar.add(fileMenu = new JMenu(Messages.getString("DividerUI.File_1"))); //$NON-NLS-1$
		fileMenu.add(divider.newFigureAction);
		fileMenu.add(divider.openFigureAction);
		fileMenu.add(divider.saveFigureAction);
		fileMenu.addSeparator();
		fileMenu.add(divider.exitAction);
		
		menuBar.add(editMenu = new JMenu(Messages.getString("DividerUI.Edit_2"))); //$NON-NLS-1$
		editMenu.add(divider.undoAction);
		editMenu.add(divider.redoAction);
		editMenu.addSeparator();
		editMenu.add(divider.createContourAction);
		editMenu.add(divider.editWorldAction);
		editMenu.add(divider.createAirFrameAction);
		editMenu.addSeparator();
		editMenu.add(divider.editInfluenceAction);
		editMenu.add(divider.editPreferencesAction);
		
		menuBar.add(viewMenu = new JMenu(Messages.getString("DividerUI.View_3"))); //$NON-NLS-1$
		viewMenu.add(zoomInAction);
		viewMenu.add(zoomOutAction);
		viewMenu.addSeparator();
		viewMenu.add(showMENumbers);
		viewMenu.add(showMNNumbers);
		viewMenu.add(fillFigureElements);
		
		menuBar.add(meshMenu = new JMenu(Messages.getString("DividerUI.Mesh_4"))); //$NON-NLS-1$
		meshMenu.add(divider.meshdownAction);
		meshMenu.addSeparator();
		meshMenu.add(divider.editMeshSettingsAction);
		meshMenu.addSeparator();
		meshMenu.add(divider.saveMeshAction);
		meshMenu.add(createMeshdownMethodMenu());
		
		menuBar.add(measureMenu = new JMenu(Messages.getString("DividerUI.Measure_3"))); //$NON-NLS-1$
		measureMenu.add(divider.measure.getMeasureController().editAction);
		measureMenu.add(divider.measure.getMeasureController().loadAction);
		measureMenu.add(divider.measure.getMeasureController().saveAction);    	
    }//end method initMainMenu
    
    private void initToolbar()
    {
		toolBar.add(divider.createContourAction);
		toolBar.add(divider.createAirFrameAction);
		toolBar.addSeparator();
		toolBar.add(divider.undoAction);
		toolBar.add(divider.redoAction);
		toolBar.addSeparator();
		toolBar.add(zoomInAction);
		toolBar.add(zoomOutAction);
		toolBar.addSeparator();
		toolBar.add(divider.meshdownAction);
		toolBar.addSeparator();
		toolBar.add(divider.editWorldAction);
		toolBar.add(divider.editMeshSettingsAction);    	
    }
		
    private void initFileChoosers()
    {
    	File curDir = new File(".");
		fileChooser = new JFileChooser(curDir.getPath());
		String figureFile = Divider.getDivider().cmdLineDetails.figureFile;
		if(figureFile != null)
			fileChooser.setSelectedFile(new File(figureFile));
		
		meshFileChooser = new JFileChooser(curDir.getPath());
		FileFilter multilineMeshFilter = new fem.divider.mesh.MeshFileFilter(null,
				"Mesh parts in multiple files", 
				new fem.divider.mesh.MultifileMeshStreamer());
		FileFilter packedMeshFileFilter = new fem.divider.mesh.MeshFileFilter(packedMeshExtension,
				"Packed mesh data in a single file (."+packedMeshExtension+")", 
				new fem.divider.mesh.PackedMeshStreamer());
		meshFileChooser.addChoosableFileFilter(multilineMeshFilter);
		meshFileChooser.addChoosableFileFilter(packedMeshFileFilter);
		meshFileChooser.setFileFilter(packedMeshFileFilter);
		meshFileChooser.setAcceptAllFileFilterUsed(false);
		
		measureFileChooser = new JFileChooser(curDir.getPath());    	
		measureFileChooser.addChoosableFileFilter(new fem.divider.mesh.measure.MeasureFileFilter());
    }
    
	public final JFileChooser getMeshFileChooser() {
		return meshFileChooser;
	}

	public final void setMeshFileChooser(JFileChooser meshFileChooser) {
		this.meshFileChooser = meshFileChooser;
	}

	public final JFileChooser getMeasureFileChooser() {
		return measureFileChooser;
	}

	public final void setMeasureFileChooser(JFileChooser measureFileChooser) {
		this.measureFileChooser = measureFileChooser;
	}

	public final void setFileChooser(JFileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}

	public void updateFigure()
	{
			figurePanel.setFigure(divider.figure);
	}

	public void switchToMesh()
	{
			tabbedPane.setSelectedComponent(meshPanel);
	}

	public void switchToFigure()
	{
			tabbedPane.setSelectedComponent(figurePanel);
	}
	
	public void setStatusbarText(String txt)
	{
			statusField.setText(txt);
	}
		
	private void initControls() {                          
			toolBar = new javax.swing.JToolBar();
			tabbedPane = new javax.swing.JTabbedPane();
			
			setTitle("Divider "+IFemSettings.VERSION); //$NON-NLS-1$
			addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent evt) {
							exitForm(evt);
					}
			});
			
			getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);
			
			getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);
			
			pack();
	}

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {                          
        divider.exit(0);
    }

	public MeshPanel getMeshPanel() {return meshPanel;}
	public FigurePanel getFigurePanel() {return figurePanel;}

	public JFileChooser getFileChooser() {return fileChooser;}


	
		
		
		Action zoomInAction = new
		AbstractAction (Messages.getString("DividerUI.Zoom_In_8"),  //$NON-NLS-1$
				new ImageIcon(Divider.class.getResource("resources/images/stock_zoom_in_16.png")) //$NON-NLS-1$
				)
		{
		      private static final long serialVersionUID = 2L;
            public void actionPerformed(ActionEvent event)
				{
						AbstractPanel panel = (AbstractPanel)tabbedPane.getSelectedComponent();
						panel.zoomIn();
				}
				{
						putValue(Action.SHORT_DESCRIPTION, Messages.getString("DividerUI.Zoom_in_10")); //$NON-NLS-1$
				}
		};
		
		Action zoomOutAction = new
		AbstractAction (Messages.getString("DividerUI.Zoom_Out_11"),  //$NON-NLS-1$
				new ImageIcon(Divider.class.getResource("resources/images/stock_zoom_out_16.png")) //$NON-NLS-1$
				)
		{
		      private static final long serialVersionUID = 3L;
            public void actionPerformed(ActionEvent event)
				{
						AbstractPanel panel = (AbstractPanel)tabbedPane.getSelectedComponent();
						panel.zoomOut();
				}
				{
						putValue(Action.SHORT_DESCRIPTION, Messages.getString("DividerUI.Zoom_out_13")); //$NON-NLS-1$
				}
		};


}

//this class has method that switches meshdown method
class MethodButtonListener implements ActionListener
	{
		MethodButtonListener(MeshdownActor actor_, int m_i_) 
		{
			m_i=m_i_;
			actor=actor_;
		}
							
		public void actionPerformed(ActionEvent event)
		{
			actor.setMethodIndex(m_i);
			Divider.getDivider().getPreferences().setMeshdownMethodIndex(m_i);
		}
							
		int m_i;
		MeshdownActor actor;
	};
