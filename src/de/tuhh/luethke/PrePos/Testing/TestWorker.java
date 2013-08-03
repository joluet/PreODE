package de.tuhh.luethke.PrePos.Testing;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.Prediction.Prediction;
import de.tuhh.luethke.Prediction.Predictor;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class TestWorker implements Callable<Double> {

	private Measurement[] mMeasurements;
	SampleModel mModel;

	public TestWorker(Measurement[] measurements, SampleModel model) {
		try {
			this.mModel = new SampleModel(model);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mMeasurements = measurements;
	}

	@Override
	public Double call() throws Exception {
		/*long time = System.currentTimeMillis();
		double[][] dxVector = new double[4][1];
		dxVector[0][0] = mMeasurements.get(0).getLat();
		dxVector[1][0] = mMeasurements.get(0).getLng();
		dxVector[2][0] = mMeasurements.get(1).getLat();
		dxVector[3][0] = mMeasurements.get(1).getLng();
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);
		dxVector = new double[2][1];
		dxVector[0][0] = mMeasurements.get(2).getLat();
		dxVector[1][0] = mMeasurements.get(2).getLng();
		pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector2 = Preprocessor.projectData(pointVector);
		double prob = mModel.trapezoidRule(pointVector1, pointVector2, 100, 100);
		System.out.println("time: "+(System.currentTimeMillis()-time));
		return prob;*/
		Predictor predictor = new Predictor(mModel);
		Prediction pre = predictor.twoStepPredict(mMeasurements[0], mMeasurements[1]);
		
		LatLng point0 = new LatLng(pre.latitude, pre.longitude);
		LatLng point1 = new LatLng(mMeasurements[2].getLat(), mMeasurements[2].getLng());
		LatLng point2 = new LatLng(mMeasurements[0].getLat(), mMeasurements[0].getLng());
		LatLng point3 = new LatLng(mMeasurements[1].getLat(), mMeasurements[1].getLng());
		double sampleDistance = LatLngTool.distance(point2, point3, LengthUnit.METER);
		double sampleDistance1 = LatLngTool.distance(point3, point1, LengthUnit.METER);
		double distance = LatLngTool.distance(point0, point1, LengthUnit.METER);
		System.out.println(mMeasurements[0].getLat()+" "+ mMeasurements[0].getLng()+" | "
				+mMeasurements[1].getLat()+" "+ mMeasurements[1].getLng()+" | "
				+mMeasurements[2].getLat()+" "+ mMeasurements[2].getLng());
		System.out.println(pre.latitude+" "+ pre.longitude+ " "+distance+" "+sampleDistance+" "+sampleDistance1+" "+pre.marginalProbability+" "+pre.probability+" "+pre.widerProbability);
		return distance;
	}

}
