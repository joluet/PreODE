package de.tuhh.luethke.Prediction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Testing.EvaluationWorker;
import de.tuhh.luethke.PrePos.Transformation.Postprocessor;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class Predictor {

	private SampleModel mSampleModel;
	
	public Predictor(SampleModel model){
		mSampleModel = model;
	}
	

	
	public Prediction twoStepPredict(Measurement firstPosition, Measurement secondPosition) {
		
		//estimate area to look for local maxima
		SimpleMatrix secondPositionVector = new SimpleMatrix(2, 1);
		secondPositionVector.set(0, 0, secondPosition.getLat());
		secondPositionVector.set(1, 0, secondPosition.getLng());
		secondPositionVector = Preprocessor.projectData(secondPositionVector);
		
		double[] x = new double[50];
		double[] y = new double[50];
		
		double coord = secondPositionVector.get(0, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			x[i] = coord;
		}

		coord = secondPositionVector.get(1, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			y[i] = coord;
		}

		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>(x.length * y.length);
		double[][] z = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < y.length; j++) {
				double[][] dxVector = new double[6][1];
				dxVector[0][0] = firstPosition.getLat();
				dxVector[1][0] = firstPosition.getLng();
				dxVector[2][0] = secondPosition.getLat();
				dxVector[3][0] = secondPosition.getLng();
				SimpleMatrix pointVector = new SimpleMatrix(dxVector);
				SimpleMatrix pointVector1 = Preprocessor.projectData6(pointVector);
				pointVector1.set(4, 0, x[i]);
				pointVector1.set(5, 0, y[j]);
				int[] condDim = { 0, 1, 2, 3 };
				Callable<Double> worker = new EvaluationWorker(pointVector1, condDim, mSampleModel);
				futureResults.add(executor.submit(worker));
			}
		}

		for (int i = 0; i < futureResults.size(); i++) {
			try {
				z[i / y.length][i % y.length] = futureResults.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		//executor.shutdownNow();
		ArrayList<int[]> filtered = Postprocessor.medianFilter(z);
		double[][] dxVector = new double[4][1];
		dxVector[0][0] = firstPosition.getLat();
		dxVector[1][0] = firstPosition.getLng();
		dxVector[2][0] = secondPosition.getLat();
		dxVector[3][0] = secondPosition.getLng();
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);
		double maxProb = 0;
		double widerProb = 0;
		int[][] maxPoint = new int[2][1];
		ArrayList<SimpleMatrix> weightedCoordinates = new ArrayList<SimpleMatrix>();
		for (int i = 0; i < filtered.size(); i++) {
			SimpleMatrix m = new SimpleMatrix(2, 1);
			m.set(0, 0, x[filtered.get(i)[0]]);
			m.set(1, 0, y[filtered.get(i)[1]]);
			double prob = mSampleModel.cummulativeConditional(pointVector1, m, 200, 2, 2);
			z[filtered.get(i)[0]][filtered.get(i)[1]] = prob;
			weightedCoordinates.add(Preprocessor.projectDataBack(m));
			if(prob > maxProb) {
				maxProb = prob;
				maxPoint[0][0] = filtered.get(i)[0];
				maxPoint[1][0] = filtered.get(i)[1];
			}
		}
		
		
		
		
		coord = x[maxPoint[0][0]] - 100;
		x = new double[50];
		for (int i = 0; i < 50; i++) {
			coord += 4;
			x[i] = coord;
		}

		coord = y[maxPoint[1][0]] - 100;
		y = new double[50];
		for (int i = 0; i < 50; i++) {
			coord += 4;
			y[i] = coord;
		}

		
		futureResults = new ArrayList<Future<Double>>(x.length * y.length);
		z = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < y.length; j++) {
				dxVector = new double[6][1];
				dxVector[0][0] = firstPosition.getLat();
				dxVector[1][0] = firstPosition.getLng();
				dxVector[2][0] = secondPosition.getLat();
				dxVector[3][0] = secondPosition.getLng();
				pointVector = new SimpleMatrix(dxVector);
				pointVector1 = Preprocessor.projectData6(pointVector);
				pointVector1.set(4, 0, x[i]);
				pointVector1.set(5, 0, y[j]);
				int[] condDim = { 0, 1, 2, 3 };
				Callable<Double> worker = new EvaluationWorker(pointVector1, condDim, mSampleModel);
				futureResults.add(executor.submit(worker));
			}
		}
		for (int i = 0; i < futureResults.size(); i++) {
			try {
				z[i / y.length][i % y.length] = futureResults.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		dxVector = new double[4][1];
		dxVector[0][0] = firstPosition.getLat();
		dxVector[1][0] = firstPosition.getLng();
		dxVector[2][0] = secondPosition.getLat();
		dxVector[3][0] = secondPosition.getLng();
		pointVector = new SimpleMatrix(dxVector);
		pointVector1 = Preprocessor.projectData4(pointVector);
		maxProb = 0;
		double marginal = 0;
		widerProb = 0;
		maxPoint = new int[2][1];
		for (int i = 0; i < z.length; i++) {
			for (int j = 0; j < z[0].length; j++) {
				SimpleMatrix m = new SimpleMatrix(2, 1);
				m.set(0, 0, x[i]);
				m.set(1, 0, y[j]);
				double prob = mSampleModel.cummulativeConditional(pointVector1, m, 5, 1, 1);
				if(prob > maxProb) {
					//marginal = mSampleModel.cummulativeMarginal(pointVector1, m, 5, 1, 1);
					maxProb = prob;
					maxPoint[0][0] = i;
					maxPoint[1][0] = j;
				}
			}
		}
		
		
		
		
		SimpleMatrix prediction = new SimpleMatrix(2,1);
		prediction.set(0,0,x[maxPoint[0][0]]);
		prediction.set(1,0,y[maxPoint[1][0]]);
		widerProb = mSampleModel.cummulativeConditional(pointVector1, prediction, 4000, 10, 10);
		marginal = mSampleModel.cummulativeMarginal(pointVector1, prediction, 4000, 10, 10);
		prediction = Preprocessor.projectDataBack(prediction);
		dataToHeatMapFile(weightedCoordinates);
		Prediction p = new Prediction(prediction.get(0,0),prediction.get(1,0),marginal,maxProb,widerProb,250);
		executor.shutdown();
		return p;
	}
	
	private static void dataToHeatMapFile(List<SimpleMatrix> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("dataForHeatMap_weighted.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (SimpleMatrix m : data)
				pw.println(m.get(0, 0) + " " + m.get(1, 0));
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	
	
	
	
public Prediction predict(Measurement[] measurements) {
		
		//estimate area to look for local maxima
		SimpleMatrix lastPositionVector = new SimpleMatrix(2, 1);
		int steps = measurements.length;
		lastPositionVector.set(0, 0, measurements[steps-1].getLat());
		lastPositionVector.set(1, 0, measurements[steps-1].getLng());
		lastPositionVector = Preprocessor.projectData(lastPositionVector);
		
		double[] x = new double[50];
		double[] y = new double[50];
		
		double coord = lastPositionVector.get(0, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			x[i] = coord;
		}

		coord = lastPositionVector.get(1, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			y[i] = coord;
		}

		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>(x.length * y.length);
		double[][] z = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < y.length; j++) {
				double[][] dxVector = new double[2*steps+2][1];
				for(int k=0; k<steps; k++){
					dxVector[2*k][0] = measurements[k].getLat();
					dxVector[2*k+1][0] = measurements[k].getLng();
				}
				SimpleMatrix pointVector = new SimpleMatrix(dxVector);
				SimpleMatrix pointVector1 = Preprocessor.projectData(pointVector);
				pointVector1.set(pointVector1.numRows()-2, 0, x[i]);
				pointVector1.set(pointVector1.numRows()-1, 0, y[j]);
				int[] condDim = new int[pointVector1.numRows()-2];
		    	for(int k=0; k<condDim.length; k++){
		    		condDim[k] = k;
		    	}
				Callable<Double> worker = new EvaluationWorker(pointVector1, condDim, mSampleModel);
				futureResults.add(executor.submit(worker));
			}
		}

		for (int i = 0; i < futureResults.size(); i++) {
			try {
				z[i / y.length][i % y.length] = futureResults.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		//executor.shutdownNow();
		ArrayList<int[]> filtered = Postprocessor.medianFilter(z);
		double sum = 0;
		double[][] dxVector = new double[2*steps][1];
		for(int k=0; k<steps; k++){
			dxVector[2*k][0] = measurements[k].getLat();
			dxVector[2*k+1][0] = measurements[k].getLng();
		}
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector1 = Preprocessor.projectData(pointVector);
		double maxProb = 0;
		double widerProb = 0;
		int[][] maxPoint = new int[2][1];
		ArrayList<SimpleMatrix> weightedCoordinates = new ArrayList<SimpleMatrix>();
		for (int i = 0; i < filtered.size(); i++) {
			SimpleMatrix m = new SimpleMatrix(2, 1);
			m.set(0, 0, x[filtered.get(i)[0]]);
			m.set(1, 0, y[filtered.get(i)[1]]);
			double prob = mSampleModel.cummulativeConditional(pointVector1, m, 400, 2, 2);
			z[filtered.get(i)[0]][filtered.get(i)[1]] = prob;
			weightedCoordinates.add(Preprocessor.projectDataBack(m));
			if(prob > maxProb) {
				maxProb = prob;
				maxPoint[0][0] = filtered.get(i)[0];
				maxPoint[1][0] = filtered.get(i)[1];
			}
			sum += prob;
		}
		
		
		
		
		coord = x[maxPoint[0][0]] - 100;
		x = new double[50];
		for (int i = 0; i < 50; i++) {
			coord += 4;
			x[i] = coord;
		}

		coord = y[maxPoint[1][0]] - 100;
		y = new double[50];
		for (int i = 0; i < 50; i++) {
			coord += 4;
			y[i] = coord;
		}

		
		futureResults = new ArrayList<Future<Double>>(x.length * y.length);
		z = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < y.length; j++) {
				dxVector = new double[2*steps+2][1];
				for(int k=0; k<steps; k++){
					dxVector[2*k][0] = measurements[k].getLat();
					dxVector[2*k+1][0] = measurements[k].getLng();
				}
				pointVector = new SimpleMatrix(dxVector);
				pointVector1 = Preprocessor.projectData(pointVector);
				pointVector1.set(pointVector1.numRows()-2, 0, x[i]);
				pointVector1.set(pointVector1.numRows()-1, 0, y[j]);
				int[] condDim = new int[pointVector1.numRows()-2];
		    	for(int k=0; k<condDim.length; k++){
		    		condDim[k] = k;
		    	}
				Callable<Double> worker = new EvaluationWorker(pointVector1, condDim, mSampleModel);
				futureResults.add(executor.submit(worker));
			}
		}
		for (int i = 0; i < futureResults.size(); i++) {
			try {
				z[i / y.length][i % y.length] = futureResults.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		dxVector = new double[2*steps][1];
		for(int k=0; k<steps; k++){
			dxVector[2*k][0] = measurements[k].getLat();
			dxVector[2*k+1][0] = measurements[k].getLng();
		}
		pointVector = new SimpleMatrix(dxVector);
		pointVector1 = Preprocessor.projectData(pointVector);
		maxProb = 0;
		double marginal = 0;
		widerProb = 0;
		maxPoint = new int[2][1];
		for (int i = 0; i < z.length; i++) {
			for (int j = 0; j < z[0].length; j++) {
				SimpleMatrix m = new SimpleMatrix(2, 1);
				m.set(0, 0, x[i]);
				m.set(1, 0, y[j]);
				double prob = mSampleModel.cummulativeConditional(pointVector1, m, 5, 1, 1);
				if(prob > maxProb) {
					//marginal = mSampleModel.cummulativeMarginal(pointVector1, m, 5, 1, 1);
					maxProb = prob;
					maxPoint[0][0] = i;
					maxPoint[1][0] = j;
				}
				sum += prob;
			}
		}
		
		
		
		
		SimpleMatrix prediction = new SimpleMatrix(2,1);
		prediction.set(0,0,x[maxPoint[0][0]]);
		prediction.set(1,0,y[maxPoint[1][0]]);
		widerProb = mSampleModel.cummulativeConditional(pointVector1, prediction, 4000, 10, 10);
		marginal = mSampleModel.cummulativeMarginal(pointVector1, prediction, 4000, 10, 10);
		prediction = Preprocessor.projectDataBack(prediction);
		Prediction p = new Prediction(prediction.get(0,0),prediction.get(1,0),marginal,maxProb,widerProb,200);
		executor.shutdown();
		return p;
	}
	
}
