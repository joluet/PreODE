/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Transformation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.projection.PVector;

/**
 * This class provides static post-processing methods for positional time series data including: -
 * Map Projection using UTM - Filtering by speed, time difference, distance
 * 
 * 
 * @author Jonas Luethke
 * 
 */
public class Preprocessor {

	private final static int MIN_DISTANCE = 0;// 5m

	private final static double MAX_SPEED = 60d;// 216km/h

	private final static double MIN_TIME_DIFF = 10d;// 10s

	private final static double TEST_TIME_STEP = 282d;// 4.7min

	private final static double MAX_ACCEL = 5.55; // in 5s from 0 to 100

	/**
	 * Projects positional data from latitiude/longitude to UTM.
	 * 
	 * @param measurements
	 *            The positional time series data
	 */
	public static void projectData(List<SimpleMatrix> measurements) {
		for (int i = 0; i < measurements.size(); i++) {
			SimpleMatrix m = measurements.get(i);
			m = projectData(m);
			measurements.set(i, m);
		}
	}

	/*
	 * public static SimpleMatrix projectData(SimpleMatrix m) { SimpleMatrix m1 = new
	 * SimpleMatrix(m); LatLng ll = new LatLng((float) m1.get(0, 0), (float) m1.get(1, 0)); double
	 * easting = ll.toUTMRef().getEasting(); double northing = ll.toUTMRef().getNorthing();
	 * m1.set(0, 0, (easting)); m1.set(1, 0, (northing)); return m1; }
	 */

	public static SimpleMatrix projectDataBack(SimpleMatrix m, int zoneNo, char zoneLetter) {
		SimpleMatrix m1 = new SimpleMatrix(m);
		for (int i = 0; i < m.numRows() - 1; i += 2) {
			// UTMRef utm = new UTMRef(10,'S',(float) m1.get(i, 0), (float) m1.get(i+1, 0));
			// UTMRef utm = new UTMRef(32,'U',(float) m1.get(i, 0), (float) m1.get(i+1, 0));
			UTMRef utm = new UTMRef(zoneNo, zoneLetter, (float) m1.get(i, 0), (float) m1.get(i + 1, 0));
			LatLng ll = utm.toLatLng();
			double lat = ll.getLatitude();
			double lng = ll.getLongitude();
			m1.set(i, 0, lat);
			m1.set(i + 1, 0, lng);
		}
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

	public static SimpleMatrix projectData(SimpleMatrix m) {
		for (int i = 0; i < m.numRows() - 1; i += 2) {
			LatLng ll = new LatLng((float) m.get(i, 0), (float) m.get(i + 1, 0));
			double easting = ll.toUTMRef().getEasting();
			double northing = ll.toUTMRef().getNorthing();
			m.set(i, 0, easting);
			m.set(i + 1, 0, northing);
		}
		return m;
	}

	/**
	 * Filters given measurement data by speed, acceleration and minimum distance.
	 * 
	 * @param measurements
	 *            The positional time series data
	 */
	public static void processTestData(LinkedList<Measurement> measurements) {
		Measurement tmp = null;
		int count = 0;
		int distanceCount = 0;
		int speedCount = 0;
		int accelCount = 0;
		double accelleration = 0;
		double prevSpeed = 0;
		tmp = measurements.poll();
		for (Iterator<Measurement> i = measurements.iterator(); i.hasNext();) {
			Measurement m = (Measurement) i.next();
			double distance = m.distanceInMeters(tmp);
			double timeDiff = m.timeDiffInSeconds(tmp);
			double speed = distance / timeDiff;
			if (prevSpeed > 0)
				accelleration = (speed - prevSpeed) / timeDiff;

			if (distance < MIN_DISTANCE || speed > MAX_SPEED || accelleration > MAX_ACCEL) {
				i.remove();
				count++;
				if (distance < MIN_DISTANCE)
					distanceCount++;
				if (accelleration > MAX_ACCEL)
					accelCount++;
				if (accelleration <= MAX_ACCEL && speed > MAX_SPEED)
					speedCount++;
			} else {
				tmp = new Measurement(m);
				prevSpeed = speed;
			}
		}
		System.out.println(count + " data points removed by preprocessor! (distance < " + MIN_DISTANCE + "m ("
				+ distanceCount + "), speed > " + MAX_SPEED * 3.6 + "km/h (" + speedCount + ")" + ", accelleration > "
				+ MAX_ACCEL + "m/s^2 (" + accelCount + "))");
	}

}
