package de.tuhh.luethke.PrePos.utility;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

public class Measurement {

	private final int FACTOR_10E6 = 1000000;
	private final double EARTH_RADIUS = 6371;

	private double mLat;
	private double mLng;
	private long mDate;

	public Measurement(double lat, double lng, long date) {
		super();
		this.mLat = lat;
		this.mLng = lng;
		this.mDate = date;
	}

	public Measurement(Measurement m) {
		super();
		this.mLat = m.getLat();
		this.mLng = m.getLng();
		this.mDate = m.getDate();
	}

	public double distanceInUnits(Measurement m) {
		double distance = 0;
		if (m != null) {
			double lat1 = this.getLat();
			double lat2 = m.getLat();
			double lon1 = this.getLng();
			double lon2 = m.getLng();
			double dLat = lat2 - lat1;
			double dLon = lon2 - lon1;
			distance = Math.sqrt(Math.pow(dLat, 2) + Math.pow(dLon, 2));
		}
		return distance;
	}

	public double distanceInMeters(Measurement m) {
		double distance = 0;
		if (m != null) {
			/*
			 * double lat1 = this.getLat(); double lat2 = m.getLat(); double
			 * lon1 = this.getLng(); double lon2 = m.getLng(); double dLat =
			 * Math.toRadians(lat2 - lat1); double dLon = Math.toRadians(lon2 -
			 * lon1); double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			 * Math.cos(Math.toRadians(lat1)) Math.cos(Math.toRadians(lat2)) *
			 * Math.sin(dLon / 2) Math.sin(dLon / 2); double c = 2 *
			 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); distance =
			 * EARTH_RADIUS * c;
			 */
			double lat1 = this.getLat();
			double lat2 = m.getLat();
			double lon1 = this.getLng();
			double lon2 = m.getLng();
			LatLng point1 = new LatLng(lat1, lon1);
			LatLng point2 = new LatLng(lat2, lon2);
			distance = LatLngTool.distance(point1, point2, LengthUnit.METER);
		}
		return distance;
	}

	public double timeDiffInSeconds(Measurement m) {
		double diff = 0;
		if (m != null) {
			double t1 = this.getDate();
			double t2 = m.getDate();
			diff = Math.abs(t2 - t1);
		}
		return diff;
	}
	
	public double timeDiffInSecondsWithSign(Measurement m) {
		double diff = 0;
		if (m != null) {
			double t1 = this.getDate();
			double t2 = m.getDate();
			diff = t2 - t1;
		}
		return diff;
	}

	public double getLat() {
		return mLat;
	}

	public double getLat10E6() {
		return mLat * FACTOR_10E6;
	}

	public void setLat(double lat) {
		this.mLat = lat;
	}

	public double getLng() {
		return mLng;
	}

	public double getLng10E6() {
		return mLng * FACTOR_10E6;
	}

	public void setLng(double lng) {
		this.mLng = lng;
	}

	public long getDate() {
		return mDate;
	}

	public void setDate(long date) {
		this.mDate = date;
	}

}
