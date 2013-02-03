/*
 * Created on 9/7/2006
 */
package divider.mesh;

/**
*Every meshdown method has to implement this interface
* @author gefox
*/
public abstract class MethodAbstract {

	/**
	 *Get instance of meshdown method.
	 *This function should be redefined in subclasses 
	 */
	static public MethodAbstract getInstance() 
	{return null;}
	
	/**
	 *get name of method 
	 */
	abstract public String getName();
	
	/**
	 *get description of method 
	 */
	abstract public String getDescription();
	
	/**
	 * Generate mesh from figure
	 * @param figure_ --- source figure
	 * @return resulting mesh
	 */
	abstract public Mesh meshdown(divider.figure.Figure figure_);
	
	/**
	 * Check if method may be applied to figure
	 * @param figure_ -- figure to check
	 * @return null on ok, error message on failure
	 */
	abstract public String test(divider.figure.Figure figure_);
	
	/**
	 * Get meshdown progress (may be called from other thread) 
	 * @return number: 0.0 means nothing, 1.0 means done
	 */
	abstract public double getProgress();
}
