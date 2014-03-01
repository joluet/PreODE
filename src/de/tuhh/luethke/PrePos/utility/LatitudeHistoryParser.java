/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.utility;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.gx.Track;

public class LatitudeHistoryParser {

    public static LinkedList<Measurement> parse(String filename) {
	final Kml kml = Kml.unmarshal(new File(filename));
	final Document doc = (Document) kml.getFeature();
	final List<Feature> features = doc.getFeature();
	Feature f = features.get(0);
	Placemark p = (Placemark) f;
	Track track = (Track) p.getGeometry();
	LinkedList<Measurement> measurements = readTrack(track);
	return measurements;
    }

    /**
     * Reads contents of a gx:Track element
     * 
     * @param track
     *            gx:Track element to read
     */
    private static LinkedList<Measurement> readTrack(Track track) {
	List<String> times = track.getWhen();
	List<String> coordinates = track.getCoord();
	LinkedList<Measurement> measurements = new LinkedList<Measurement>();
	for (int i = 0; i < times.size(); i++) {
	    String t = times.get(i);
	    String c = coordinates.get(i);

	    Long time = StringUtils.getTime(t)/1000;

	    String[] coords = c.split(" ");

	    Double lng = Double.parseDouble(coords[0]);
	    Double lat = Double.parseDouble(coords[1]);

	    Measurement measurement = new Measurement(lat, lng, time);
	    measurements.add(measurement);
	}
	return measurements;
    }
}
