package org.openlcb;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory used by thread pools created by OLCB objects. 
 *
 * @author Paul Bender Copyright (C) 2018 
 */

public class OlcbThreadFactory implements ThreadFactory{
    private String name;
    private int count;
    private int factoryNumber;
    static int factoryCount = 0;

    public OlcbThreadFactory(){
       factoryNumber=++factoryCount;
       name = "Olcb-Pool-";
       count = 0;
    }

    @Override
    public Thread newThread(Runnable r){
       Thread t = new Thread(r, name + factoryNumber + "-Thread-" + (count++));
       return t;
    }

}
