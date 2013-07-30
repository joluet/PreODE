package de.tuhh.luethke.PrePos.Transformation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;
import uk.me.jstott.jcoord.datum.WGS84Datum;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.projection.PVector;

public class Postprocessor {

	private final static double MAX_SPEED = 28d;// 100km/h
	private final static double MIN_TIME_DIFF = 60d;// 5min
	
	private final static double TEST_TIME_STEP = 270d;// 4.5min


	public static void projectData(List<SimpleMatrix> measurements) {
		for (int i = 0; i < measurements.size(); i++) {
			SimpleMatrix m = measurements.get(i);
			m = projectData6(m);
			measurements.set(i, m);
		}
	}

	public static SimpleMatrix projectData(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		LatLng ll = new LatLng((float) m1.get(0, 0), (float) m1.get(1, 0));
		double easting = ll.toUTMRef().getEasting();
		double northing = ll.toUTMRef().getNorthing();
		m1.set(0, 0, (easting));
		m1.set(1, 0, (northing));
		return m1;
	}

	public static SimpleMatrix projectData4(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		PVector p = new PVector((float) m1.get(0, 0), (float) m1.get(1, 0));
		LatLng ll = new LatLng((float) m1.get(0, 0), (float) m1.get(1, 0));
		double easting = ll.toUTMRef().getEasting();
		double northing = ll.toUTMRef().getNorthing();
		m1.set(0, 0, easting);
		m1.set(1, 0, northing);
				
		ll = new LatLng((float) m1.get(2, 0), (float) m1.get(3, 0));
		easting = ll.toUTMRef().getEasting();
		northing = ll.toUTMRef().getNorthing();
		m1.set(2, 0, easting);
		m1.set(3, 0, northing);
		return m1;
	}
	
	public static SimpleMatrix projectData6(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		LatLng ll = new LatLng((float) m1.get(0, 0), (float) m1.get(1, 0));
		double easting = ll.toUTMRef().getEasting();
		double northing = ll.toUTMRef().getNorthing();
		m1.set(0, 0, easting);
		m1.set(1, 0, northing);
				
		ll = new LatLng((float) m1.get(2, 0), (float) m1.get(3, 0));
		easting = ll.toUTMRef().getEasting();
		northing = ll.toUTMRef().getNorthing();
		m1.set(2, 0, easting);
		m1.set(3, 0, northing);
		
		ll = new LatLng((float) m1.get(4, 0), (float) m1.get(5, 0));
		easting = ll.toUTMRef().getEasting();
		northing = ll.toUTMRef().getNorthing();
		m1.set(4, 0, easting);
		m1.set(5, 0, northing);
		return m1;
	}
	


	public static void processData(LinkedList<Measurement> measurements) {
		Measurement tmp = null;
		int count = 0;
		tmp = measurements.poll();
		for (Iterator<Measurement> i = measurements.iterator(); i.hasNext();) {
			Measurement m = (Measurement) i.next();
			double distance = m.distanceInMeters(tmp);
			double timeDiff = m.timeDiffInSeconds(tmp);
			double speed = distance / timeDiff;
			if (speed > MAX_SPEED || timeDiff < MIN_TIME_DIFF || distance < 100) {
				i.remove();
				count++;
			} else
				tmp = new Measurement(m);
		}
		System.out.println(count + " Datenpunkte entfernt!");
	}
	
	public static void processTestData(LinkedList<Measurement> measurements) {
		Measurement tmp = null;
		int count = 0;
		tmp = measurements.poll();
		for (Iterator<Measurement> i = measurements.iterator(); i.hasNext();) {
			Measurement m = (Measurement) i.next();
			double distance = m.distanceInMeters(tmp);
			double timeDiff = m.timeDiffInSeconds(tmp);
			if (timeDiff != TEST_TIME_STEP) {
				i.remove();
				count++;
			} else
				tmp = new Measurement(m);
		}
		System.out.println(count + " Datenpunkte entfernt!");
	}

	/*public static SimpleMatrix projectDataBack(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		PVector p = new PVector((float) m1.get(1, 0), (float) m1.get(0, 0));
		UTM merc = new UTM(new Ellipsoid(Ellipsoid.WGS_84), 10, 's');
		p = merc.invTransformCoords(p);
		m1.set(0, 0, p.y);
		m1.set(1, 0, p.x);
		p = new PVector((float) m1.get(3, 0), (float) m1.get(2, 0));
		p = merc.invTransformCoords(p);
		m1.set(2, 0, p.y);
		m1.set(3, 0, p.x);
		return m1;
	}*/
}
