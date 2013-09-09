package de.tuhh.luethke.PrePos.Testing;

import java.util.concurrent.Callable;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.Prediction.Prediction;
import de.tuhh.luethke.Prediction.Predictor;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class TestWorkerXStep implements Callable<Double> {

	private Measurement[] mMeasurements;
	SampleModel mModel;
	int searchRadius;
	int searchSegmentDistance;
	int accuracyRadius;
	int predictionSegments;
	int UTMZoneNo;
	char UTMZoneLetter;
	boolean mUseAdditionalInfo;

	

	public TestWorkerXStep(Measurement[] mMeasurements, SampleModel mModel, int searchRadius, int searchSegmentDistance, int accuracyRadius,
			int predictionSegments, int UTMZoneNo, char UTMZoneLetter, boolean useAdditionalInfo) {
		super();
		this.mMeasurements = mMeasurements;
		this.mModel = mModel;
		this.searchRadius = searchRadius;
		this.searchSegmentDistance = searchSegmentDistance;
		this.accuracyRadius = accuracyRadius;
		this.predictionSegments = predictionSegments;
		this.UTMZoneNo = UTMZoneNo;
		this.UTMZoneLetter = UTMZoneLetter;
		this.mUseAdditionalInfo = useAdditionalInfo;
	}



	@Override
	public Double call() throws Exception {
		Predictor predictor = new Predictor(mModel);
		Measurement[] samples = new Measurement[mMeasurements.length-1];
		for(int i=0; i<samples.length; i++){
			samples[i] = mMeasurements[i];
		}
		Prediction pre = predictor.predict(samples, searchRadius, searchSegmentDistance, accuracyRadius, predictionSegments, UTMZoneNo, UTMZoneLetter, mUseAdditionalInfo);
		
		if(pre == null){
			System.out.println("Prediction was not possible. Too few data available.");
			return null;
		}
			
		
		LatLng predictionPoint = new LatLng(pre.latitude, pre.longitude);
		
		LatLng[] samplePoints = new LatLng[mMeasurements.length];
		for(int i=0; i<samplePoints.length; i++){
			samplePoints[i] = new LatLng(mMeasurements[i].getLat(), mMeasurements[i].getLng());
		}
		double[] distances = new double[samplePoints.length];
		for(int i=0; i<distances.length-1; i++){
			distances[i] = LatLngTool.distance(samplePoints[i], samplePoints[i+1], LengthUnit.METER);
		}
		distances[distances.length-1] = LatLngTool.distance(samplePoints[samplePoints.length-1], predictionPoint, LengthUnit.METER);
		double relDist = distances[distances.length-1]/distances[distances.length-2];

		
		String coordianteString = "";
		for(int i=0; i<mMeasurements.length; i++) {
			coordianteString = coordianteString + mMeasurements[i].getLat()+" "+ mMeasurements[i].getLng()+" | ";
		}
		coordianteString = coordianteString + pre.latitude+" "+ pre.longitude;

		String distanceString = "";
		for(int i=0; i<distances.length; i++) {
			distanceString = distanceString + distances[i]+" ";
		}
		
		String probString = relDist+" "+pre.marginalProbability+" "+pre.probability+" "+pre.widerProbability;
		System.out.println(coordianteString+"\n"+distanceString+"\n"+probString+"\n");
		
		return distances[distances.length-1];
	}

}
