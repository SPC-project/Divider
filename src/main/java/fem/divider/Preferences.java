/*
 * Created on 6/12/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fem.divider;

/**
 * Settings for divider.
 * Some settings are contained in this class, some it takes from divider to load/save
 * Has methods to save/load settings.
 * 
 * @author gefox
 */
public class Preferences {
	public Preferences(Divider divider_)
	{
		divider=divider_;
	}
	
	/**
	 * Load preferences from storage to application.
	 * Doesn't do changes in UI
	 *- Should be called when everything that has preferences has been created
	 */
	void load()
	{
		java.util.prefs.Preferences pref= java.util.prefs.Preferences.userRoot();
		java.util.prefs.Preferences pnode = pref.node("/divider"); //$NON-NLS-1$
		
		setShowMeshNodeNumbers(pnode.getBoolean("showMeshNodeNumbers", false)); //$NON-NLS-1$
		setShowMeshElementNumbers(pnode.getBoolean("showMeshElementNumbers", false)); //$NON-NLS-1$
		setMeshdownMethodIndex(pnode.getInt("meshdownMethodIndex", 0)); //$NON-NLS-1$

		setNumberFormat(pnode.get("numberFormat", default_numberFormat)); //$NON-NLS-1$
		setNumberDecimalSeparator((pnode.get("numberDecimalSeparator", default_numberDecimalSeparator+"")+".").charAt(0)); //$NON-NLS-1$
		setNumberGroupSeparator((pnode.get("numberGroupSeparator", default_numberGroupSeparator+"")+",").charAt(0)); //$NON-NLS-1$

		//divider.dividerUI.showMENumbers.setSelected(showMeshElementNumbers);
		//divider.dividerUI.showMNNumbers.setSelected(showMeshNodeNumbers);
		//divider.meshdownActor.setMethodIndex(meshdownMethodIndex);
	}
	
	void save()
	{
		java.util.prefs.Preferences pref= java.util.prefs.Preferences.userRoot();
		java.util.prefs.Preferences pnode = pref.node("/divider"); //$NON-NLS-1$
		
		pnode.putBoolean("showMeshNodeNumbers", isShowMeshNodeNumbers()); //$NON-NLS-1$
		pnode.putBoolean("showMeshElementNumbers", isShowMeshElementNumbers()); //$NON-NLS-1$
		pnode.putInt("meshdownMethodIndex", getMeshdownMethodIndex()); //$NON-NLS-1$
		
		pnode.put("numberFormat", getNumberFormat());//$NON-NLS-1$
		pnode.put("numberDecimalSeparator", getNumberDecimalSeparator()+"");//$NON-NLS-1$
		pnode.put("numberGroupSeparator", getNumberGroupSeparator()+"");//$NON-NLS-1$
	}
	

	public boolean isShowMeshElementNumbers() {
		return showMeshElementNumbers;
	}

	public boolean isShowMeshNodeNumbers() {
		return showMeshNodeNumbers;
	}
	
	public boolean isFillFigureElements() {
		return fillFigureElements;
	}

	public void setShowMeshElementNumbers(boolean b) {
		showMeshElementNumbers = b;
	}

	public void setShowMeshNodeNumbers(boolean b) {
		showMeshNodeNumbers = b;
	}
	
	public void setFillFigureElements(boolean b) {
		fillFigureElements = b;
	}

	public int getMeshdownMethodIndex() {
		return meshdownMethodIndex;
	}

	public void setMeshdownMethodIndex(int i) {
		meshdownMethodIndex = i;
	}

   //number format methods
	public char getNumberDecimalSeparator() {
		return numberDecimalSeparator;
	}

	public String getNumberFormat() {
		return numberFormat;
	}
 
	public char getNumberGroupSeparator() {
		return numberGroupSeparator;
	}

	public void setNumberDecimalSeparator(char c) {
		numberDecimalSeparator = c;
	}
	public void setDefaultNumberDecimalSeparator() 
	{setNumberDecimalSeparator(default_numberDecimalSeparator);}
	
	public void setNumberFormat(String string) {
		numberFormat = string;
	}
	public void setDefaultNumberFormat() 
	{setNumberFormat(default_numberFormat);}

	public void setNumberGroupSeparator(char c) {
		numberGroupSeparator = c;
	}
	public void setDefaultNumberGroupSeparator() 
	{setNumberGroupSeparator(default_numberGroupSeparator);}
//	end number format methods


	private Divider divider;
	
	private boolean showMeshNodeNumbers=false; 
	private boolean showMeshElementNumbers=false;
	private boolean fillFigureElements=true;
	private int meshdownMethodIndex=0;
	
	
//	number format fields	
	private String  numberFormat="";
	private char numberDecimalSeparator='.';
	private char numberGroupSeparator=',';

	public final static String  default_numberFormat="0.####";
	public final static char default_numberDecimalSeparator='.';
	public final static char default_numberGroupSeparator=',';
//end 	number format fields	



}
