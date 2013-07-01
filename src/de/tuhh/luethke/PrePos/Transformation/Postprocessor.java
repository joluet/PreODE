package de.tuhh.luethke.PrePos.Transformation;


import java.util.Iterator;
import java.util.LinkedList;

import de.tuhh.luethke.PrePos.utility.Measurement;

public class Postprocessor {

    // 60km/h
    private final static double MAX_SPEED = 17d;
    
    public static void processData(LinkedList<Measurement> measurements) {
	Measurement tmp = null;
	long l=0;
	for(Iterator<Measurement> i = measurements.iterator(); i.hasNext();){
	    Measurement m = (Measurement) i.next();
	    if(tmp != null){
		double distance = m.distanceInMeters(tmp);
		double timeDiff = m.timeDiffInSeconds(tmp);
		double speed = distance/timeDiff;
		//System.out.println(distance+"m");
		//System.out.println(timeDiff+"s");    
		if(speed > MAX_SPEED) {
		    i.remove();
		    System.out.println(speed*3.6d+"km/h");
		}
	    }
	    tmp = new Measurement(m);
	    l++;
	}
    }
}
