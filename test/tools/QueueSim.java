package tools;

/**
 * Tiny simulation of M/D/1 traffic
 *
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class QueueSim {

    static class Item {
        double start;
        double end;
    }

    static java.util.Random r = new java.util.Random();
    
    /**
     * unit exponential random
     */
    static double exp() {
        return -1*Math.log(1.-r.nextDouble());
    }
    
    public static void main(String[] args) {
        double lambda = 1/500.;         // time between requests, 1/pps
        double delta = 0.001;           // service time

        double delay = 0.010;           // count event if longer than this
        int events = 1000000;           // number to run

        double freeAT = 0.;
        java.util.LinkedList<Item> todo = new java.util.LinkedList<Item>();
        java.util.LinkedList<Item> done = new java.util.LinkedList<Item>();
        double makeAt = 0.;             // time to create request
        double doneAt = 999999999999.;  // time service is over
        
        while(done.size() < events) {
            if (makeAt<=doneAt) {
                // first, make one
                Item i = new Item();
                i.start = makeAt;
                todo.add(i);
                if (todo.size()==1)
                    doneAt = makeAt+delta;
                makeAt += exp()*lambda;
            } else {
                // pull one off
                Item i = todo.removeFirst();
                i.end = doneAt;
                done.add(i);
                // process another?
                if (todo.size()>0) 
                    doneAt += delta;
                else 
                    doneAt = 999999999999.;
            }

        }
        int hits =0;
        double time = 0;
        for (Item i : done) {
            time += (i.end-i.start);
            if (i.end-i.start >= delay)
                hits++;
        }
        System.out.println("find "+(hits)+" of "+done.size()+" longer than "+delay);
        System.out.println("avg delay "+time/done.size());
    }
}

