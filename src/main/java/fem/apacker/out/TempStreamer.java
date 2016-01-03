/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fem.apacker.out;

import java.util.Iterator;

/**
 *
 * @author Paul Lysak
 */
public class TempStreamer extends AbstractStreamer {
        
    public void write() {
        int node = 1;
        for(Iterator<Double> i = getModelFacace().getNodalTemperature().iterator(); i.hasNext(); )
        {
            Double t = i.next();
            if(isIncludeNodeNumbers())
                getPrintStream().print(node+" ");
            getPrintStream().println(t);
            node++;
        }
    }//

    protected boolean isIncludeNodeNumbers() {
        return false;
    }
}
