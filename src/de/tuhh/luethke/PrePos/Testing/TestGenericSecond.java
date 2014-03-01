/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.okde.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.okde.model.BaseSampleDistribution;
import de.tuhh.luethke.okde.model.SampleModel;

public class TestGenericSecond {

	/**
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Parameters for the sample data
		String dataFileName = args[0];
		int noOfLearningSamples = Integer.valueOf(args[1]);
		int noOfTestingSamples = Integer.valueOf(args[2]);
		int predictionSteps = Integer.valueOf(args[3]);
		int stepSize = Integer.valueOf(args[4]);
		int tolerance = Integer.valueOf(args[5]);

		// Parameters for oKDE algorithm
		double forgettingFactor = Double.valueOf(args[6]);
		double compressionThreshold = Double.valueOf(args[7]);
		String kdeFileName = args[8];

		// Parameters for evaluation of KDE and mode finding
		int searchRadius = Integer.valueOf(args[9]);
		int searchSegmentDistance = Integer.valueOf(args[10]);
		int accuracyRadius = Integer.valueOf(args[11]);
		int predictionSegments = Integer.valueOf(args[12]);

		// Parameters for execution
		int noOfWorkerThreads = Integer.valueOf(args[13]);
		long maxUpdateTime = Long.valueOf(args[14]) * 60;
		long maxPredictionTime = Long.valueOf(args[15]) * 60;

		// Create string to display the defined parameters
		String paramterInfoString = "Input data file: " + dataFileName + "\n";
		paramterInfoString += "Number of samples used for learning: " + noOfLearningSamples + "\n";
		paramterInfoString += "Number of samples used for testing: " + noOfTestingSamples + "\n";
		paramterInfoString += "Prediction dimension: " + predictionSteps + "\n";
		paramterInfoString += "Prediction step size: " + stepSize + "s\n";
		paramterInfoString += "Prediction step tolerance: " + tolerance + "s\n";
		paramterInfoString += "oKDE forgetting factor: " + forgettingFactor + "\n";
		paramterInfoString += "oKDE compression threshold: " + compressionThreshold + "\n\n";

		paramterInfoString += "Radius to search for maxima during prediction: " + searchRadius + "\n";
		paramterInfoString += "Distance that defines how fine-meshed the search for maxima: " + searchSegmentDistance
				+ "\n";
		paramterInfoString += "Radius to use when estimating the cumulative probability around a prediction point: "
				+ accuracyRadius + "\n";
		paramterInfoString += "Number of segements to be used during estimation of the cumulative probability: "
				+ predictionSegments + "\n\n";

		paramterInfoString += "KDE output file name: " + kdeFileName + "\n";
		paramterInfoString += "Number of worker threads: " + noOfWorkerThreads + "\n";
		paramterInfoString += "Maximum time for learning: " + maxUpdateTime + "s\n";
		paramterInfoString += "Maximum time for predicting: " + maxPredictionTime + "s\n";

		int overallSamples = noOfLearningSamples + noOfTestingSamples;

		// Print the parameters that are used
		System.out.println(paramterInfoString);

		// UTM zone is hardcoded:
		int UTMZoneNumber = 10;
		char UTMZoneLetter = 'S';

		// Read training data (delay vectors) from file
		String delayDataFileName = dataFileName;
		ArrayList<SimpleMatrix> delayVectors = readDelayVectors(delayDataFileName, predictionSteps);
		// Check if sufficient data was imported
		if (delayVectors.size() < (noOfLearningSamples + noOfTestingSamples)) {
			System.out.println("To few delay vectors!");
			// Exit with error
			System.exit(1);
		}

		Preprocessor.projectDataFirstOrder(delayVectors);

		SampleModel dist = new SampleModel(forgettingFactor, compressionThreshold);

		double[] w = { 1, 1, 1 };

		double[][] c = new double[delayVectors.get(0).numRows()][delayVectors.get(0).numRows()];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c.length; j++) {
				c[i][j] = 0;
			}
		}
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
		int start = delayVectors.size() - noOfLearningSamples - noOfTestingSamples;
		int stop = delayVectors.size() - noOfTestingSamples;
		meansA.add(delayVectors.get(start));
		meansA.add(delayVectors.get(start + 1));
		meansA.add(delayVectors.get(start + 2));
		meansA.add(delayVectors.get(start + 3));
		long startTime = System.currentTimeMillis();
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			long time = 0;

			for (int i = start + 3; i < stop; i++) {
				if ((System.currentTimeMillis() - startTime) / 1000 > maxUpdateTime) {
					System.out.println("Learning took too much time.");
					System.exit(1);
				}

				if (i % 100 == 0) {
					time = System.currentTimeMillis() - time;
					System.out.println(i + " " + (time) + "ms");
					System.out.println("Components: " + dist.getSubMeans().size());
					System.out.println("Added by EM: " + dist.mEMCount);
					time = System.currentTimeMillis();
				}
				SimpleMatrix pos = delayVectors.get(i);
				dist.updateDistribution(pos, new SimpleMatrix(c), 1d);
				meansA.add(pos);

			}
		} catch (EmptyDistributionException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println("Time needed for leraning: " + (System.currentTimeMillis() - startTime) / 1000);
		writeKDEToFile(dist, kdeFileName);

		// define your data
		double[] x = new double[50];
		double[] y = new double[50];

		SimpleMatrix m1 = new SimpleMatrix(2, 1);
		m1.set(0, 0, 37.61638);
		m1.set(1, 0, -122.38617);
		m1 = Preprocessor.projectData(m1);
		double coord = m1.get(0, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			x[i] = coord;
		}

		coord = m1.get(1, 0) - 10000;
		for (int i = 0; i < 50; i++) {
			coord += 400;
			y[i] = coord;
		}

		double[][] z1;
		double[] results = new double[1000];
		System.out.println("$-----------------------------------------------------------------");
		System.out.println("Start prediction...");

		// Transform delay vectors to measurement arrays needed for mode finding
		List<Measurement[]> testDataVectors = toMeasurements(delayVectors);

		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>();
		ExecutorService executor = Executors.newFixedThreadPool(noOfWorkerThreads);
		for (int i = testDataVectors.size() - noOfTestingSamples; i < testDataVectors.size(); i++) {
			Callable<Double> worker = new PredictionWorker(testDataVectors.get(i), dist, searchRadius,
					searchSegmentDistance, accuracyRadius, predictionSegments, UTMZoneNumber, UTMZoneLetter, true);
			futureResults.add(executor.submit(worker));
		}
		startTime = System.currentTimeMillis();
		double avg = 0;
		for (int i = 0; i < futureResults.size(); i++) {
			if ((System.currentTimeMillis() - startTime) / 1000 > maxPredictionTime) {
				System.out.println("Prediction took too much time.");
				System.exit(1);
			}
			try {
				if (futureResults.get(i).get() != null) {
					if (i > 0)
						avg = ((float) i / (float) (i + 1)) * avg + (1f / (float) (i + 1)) * futureResults.get(i).get();
					else
						avg = futureResults.get(i).get();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Time needed for prediction: " + (System.currentTimeMillis() - startTime) / 1000);
		executor.shutdownNow();

	}

	private static ArrayList<SimpleMatrix> readDelayVectors(String filename, int predictionSteps) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filename));
			// read delay vectors line by line
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.trim();
				String[] posData = sCurrentLine.split(" ");
				// when n-step prediction is used the delay embedding vectors
				// have size (n+1) * 2 + 1
				int delayVectorLength = (predictionSteps + 1) * 2 + 1;

				SimpleMatrix m = new SimpleMatrix(delayVectorLength, 1);
				int offset = posData.length - (delayVectorLength);
				for (int i = 0; i < delayVectorLength; i++) {
					m.set(i, 0, Double.parseDouble(posData[i + offset]));
				}
				dataArray.add(m);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		return dataArray;

	}

	public static List<Measurement[]> toMeasurements(ArrayList<SimpleMatrix> vectors) {
		List<Measurement[]> measurements = new ArrayList<Measurement[]>();
		for (SimpleMatrix m : vectors) {
			Measurement[] batch = new Measurement[2];
			Measurement meas1 = new Measurement(m.get(0, 0), m.get(1, 0), 0);
			meas1.setSpeed(m.get(2, 0));
			meas1.setTimeOfDay((int) m.get(3, 0));
			meas1.setmDirection(m.get(4, 0));
			Measurement meas2 = new Measurement(m.get(5, 0), m.get(6, 0), 0);
			batch[0] = meas1;
			batch[1] = meas2;
			measurements.add(batch);
		}
		return measurements;
	}

	private static void writeKDEToFile(SampleModel model, String fileName) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(fileName);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (BaseSampleDistribution subDist : model.getSubDistributions()) {
				pw.print("Mean: " + subDist.getGlobalMean());
				pw.print("Covariance: " + subDist.getGlobalCovariance());
				pw.print("BandwidthMatrix: " + subDist.getBandwidthMatrix());
				pw.print("Weight: " + subDist.getGlobalWeight());
				pw.print("\n");
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}

}
