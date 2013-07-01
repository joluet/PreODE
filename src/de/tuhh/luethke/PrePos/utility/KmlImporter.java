package de.tuhh.luethke.PrePos.utility;


import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.gx.MultiTrack;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Imports KML file to MyTracks.
 * 
 * @author Andrew Johnston
 */
public class KmlImporter {
  
  private final MyTracksProviderUtils myTracksProviderUtils;
  
  // Current track
  private Track track;
  
  // Current location
  private Location location;
  
  // ID of this track
  private Long trackId;
  
  public KmlImporter(MyTracksProviderUtils myTracksProviderUtils) {
    this.myTracksProviderUtils = myTracksProviderUtils;
  }

  /**
   * Imports a KML file to the internal MyTracks database.
   * Currently written to read KML as written by KmlTrackWriter
   * @param inputStream
   * @param myTracksProviderUtils
   * @param minRecordingDistance
   * @return
   * @throws IOException
   */
  public static long[] importKMLFile(InputStream inputStream,
      MyTracksProviderUtils myTracksProviderUtils) {
    
    KmlImporter kmlImporter = new KmlImporter(myTracksProviderUtils);
    
    long tid;      
    long start = System.currentTimeMillis();
      
    // Convert Kml to Java objects and read to internal database
    tid = kmlImporter.addTrack(Kml.unmarshal(inputStream));
      
    long end = System.currentTimeMillis();
    Log.d(Constants.TAG, "Total import time: " + (end - start) + "ms");
    
    // we need to return an array of longs
    // this is purely for compatibility with the GPX importer 
    long[] track = new long[]{tid};
    return track;    
  }
  
  /**
   * Add parsed KML to the internal database
   * @param kml KML data in Java object form
   */
  private Long addTrack(Kml kml) {
       
    this.track = new Track();
    Uri uri = myTracksProviderUtils.insertTrack(track);
    track.setId(Long.parseLong(uri.getLastPathSegment()));
    this.trackId = track.getId();
    
    Feature feature = kml.getFeature();
    readFeature(feature);
    
    return this.trackId;
  }
  
  /**
   * Reads contents of a Feature (Document, Folder, Placemark) of a KML document to MyTracks database
   * @param feature Feature element to read
   */
  public void readFeature(Feature feature) {
    if (feature instanceof Document) {
      readDocument((Document) feature);
    }
    else if (feature instanceof Folder) {
      readFolder((Folder) feature);
    }
    else if (feature instanceof Placemark) {
      readPlacemark((Placemark) feature);
    }
  }
  
  /**
   * Read contents of a Document element to MyTracks database
   * @param document Document element to be read
   */
  public void readDocument(Document document) {
    this.track.setName(document.getName());
    this.track.setDescription(document.getDescription());
    
    // get all subfeatures of Document and read them
    List<Feature> features = document.getFeature();
    for (Feature f : features) {
      readFeature(f);
    }
  }
  
  /**
   * Read contents of a Folder element to MyTracks database
   * @param folder Folder element to be read
   */
  public void readFolder(Folder folder) {
    // get all subfeatures of Folder and read them
    List<Feature> features = folder.getFeature();
    for (Feature f : features) {
      readFeature(f);
    }
  }
  
  /**
   * Read contents of a Placemark element to MyTracks database
   * @param placemark Placemark element to be read
   */
  public void readPlacemark(Placemark placemark) {
    // Read name and description for placemark
    this.track.setName(placemark.getName());
    String description = placemark.getDescription();
    
    Geometry g = placemark.getGeometry();
    
    if (g instanceof Point) {
      readPoint((Point) g);
      //Point pt = (Point) p.getGeometry();
      //
    }
    else if (g instanceof MultiTrack) {
      readMultiTrack((MultiTrack) g);
    } 
  }
  /**
   * Read contents of a gx:MultiTrack element to MyTracks database
   * @param multitrack gx:MultiTrack element to be read
   */
  public void readMultiTrack(MultiTrack multitrack) {
    // Unfortunately, we have two classes called 'Track'
    // we cannot import this one too so we have to refer to it in full
    List<de.micromata.opengis.kml.v_2_2_0.gx.Track> tracks = multitrack.getTrack();
   
    for (de.micromata.opengis.kml.v_2_2_0.gx.Track track : tracks) {
      readTrack(track);
    }
  }
  
  /**
   * Reads contents of a gx:Track element to the MyTracks database
   * @param track gx:Track element to read
   */
  private void readTrack(de.micromata.opengis.kml.v_2_2_0.gx.Track track) {
    List<String> times = track.getWhen();
    List<String> coordinates  = track.getCoord();
    
    for (int i = 0; i < times.size(); i++) {
      String t = times.get(i);
      String c = coordinates.get(i);
      
      Long time = StringUtils.getTime(t);
      String[] coords = c.split(" ");
      
      Long longitude = Long.parseLong(coords[0]);
      Long latitude = Long.parseLong(coords[1]);
      Long altitude = Long.parseLong(coords[2]);

      location = createNewLocation(latitude, longitude, altitude, time);
      myTracksProviderUtils.insertTrackPoint(location, this.trackId);   
    }
  }

  public void readPoint(Point p) {
    List<Coordinate> coords = p.getCoordinates();
       
    for (Coordinate coord : coords) {
      double latitude = coord.getLatitude();
      double longitude = coord.getLongitude();
      double altitude = coord.getAltitude();
      
      location = createNewLocation(latitude, longitude, altitude, -1L);
      myTracksProviderUtils.insertTrackPoint(location, this.trackId);

    }
  }
  
  /**
   * Creates a new location
   * @param latitude location latitude
   * @param longitude location longitude
   * @param time location time
   */
  private Location createNewLocation(double latitude, double longitude, double altitude, long time) {
    Location loc = new Location(LocationManager.GPS_PROVIDER);
    loc.setLatitude(latitude);
    loc.setLongitude(longitude);
    loc.setAltitude(altitude);
    loc.setTime(time);
    loc.removeAccuracy();
    loc.removeBearing();
    loc.removeSpeed();
    return loc;
  } 
  
}