package tools;

/**
 * Timer implements a simple discrete-time simulation.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class Timer {
    java.util.List<Timed> items = new java.util.ArrayList<Timed>();
    long time = 0;
        
    public void add(Timed t) {
        items.add(t);
    }
    
    public void run(int ticks) {
        for (int i=0; i<ticks; i++) {
            for (Timed e : items) {
                e.tick(time);
            }
            time++;
        }
    }
    
}
