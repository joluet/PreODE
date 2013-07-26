package de.tuhh.luethke.PrePos.Transformation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.projection.PVector;
import de.tuhh.luethke.projection.WebMercator;

public class Postprocessor {

	private final static double MAX_SPEED = 28d;// 100km/h
	private final static double MIN_TIME_DIFF = 300d;// 5min

	public static void projectData(List<Measurement> measurements) {
		for (int i = 0; i < measurements.size(); i++) {
			Measurement m = measurements.get(i);
			PVector p = new PVector((float) m.getLng(), (float) m.getLat());
			WebMercator merc = new WebMercator();
			p = merc.transformCoords(p);
			m.setLat(p.y);
			m.setLng(-p.x);
			measurements.set(i, m);
		}
	}

	public static SimpleMatrix projectData(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		PVector p = new PVector((float) m.get(1, 0), (float) m.get(0, 0));
		WebMercator merc = new WebMercator();
		p = merc.transformCoords(p);
		m1.set(0, 0, p.y);
		m1.set(1, 0, -p.x);
		return m1;
	}

	public static SimpleMatrix projectData4(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		PVector p = new PVector((float) m1.get(1, 0), (float) m1.get(0, 0));
		WebMercator merc = new WebMercator();
		p = merc.transformCoords(p);
		m1.set(0, 0, p.y);
		m1.set(1, 0, -p.x);
		p = new PVector((float) m1.get(3, 0), (float) m1.get(2, 0));
		p = merc.transformCoords(p);
		m1.set(2, 0, p.y);
		m1.set(3, 0, -p.x);
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
			if (speed > MAX_SPEED || timeDiff < MIN_TIME_DIFF || distance < 50) {
				i.remove();
				count++;
			} else
				tmp = new Measurement(m);
		}
		System.out.println(count + " Datenpunkte entfernt!");
	}

	public static SimpleMatrix projectDataBack(SimpleMatrix m) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		PVector p = new PVector((float) m1.get(1, 0), (float) m1.get(0, 0));
		WebMercator merc = new WebMercator();
		p = merc.invTransformCoords(p);
		m1.set(0, 0, p.y);
		m1.set(1, 0, p.x);
		p = new PVector((float) m1.get(3, 0), (float) m1.get(2, 0));
		p = merc.invTransformCoords(p);
		m1.set(2, 0, p.y);
		m1.set(3, 0, p.x);
		return m1;
	}
}
