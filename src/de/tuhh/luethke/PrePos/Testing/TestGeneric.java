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

public class TestGeneric {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Parameters for the sample data
		int argCount = 0;
		String dataFileName = args[argCount++];
		int noOfLearningSamples = Integer.valueOf(args[argCount++]);
		int noOfTestingSamples = Integer.valueOf(args[argCount++]);
		int predictionSteps = Integer.valueOf(args[argCount++]);
		int stepSize = Integer.valueOf(args[argCount++]);
		int tolerance = Integer.valueOf(args[argCount++]);

		// Parameters for oKDE algorithm
		double forgettingFactor = Double.valueOf(args[argCount++]);
		double compressionThreshold = Double.valueOf(args[argCount++]);
		int useOnlyCabsWithPassenger = Integer.valueOf(args[argCount++]);
		int useParticularToD = Integer.valueOf(args[argCount++]);

		String kdeFileName = args[argCount++];

		// Parameters for evaluation of KDE and optimization
		int searchRadius = Integer.valueOf(args[argCount++]);
		int searchSegmentDistance = Integer.valueOf(args[argCount++]);
		int accuracyRadius = Integer.valueOf(args[argCount++]);
		int predictionSegments = Integer.valueOf(args[argCount++]);

		// Parameters for execution
		int noOfWorkerThreads = Integer.valueOf(args[argCount++]);
		long maxUpdateTime = Long.valueOf(args[argCount++]) * 60;
		long maxPredictionTime = Long.valueOf(args[argCount++]) * 60;

		// Create string to display the defined parameters
		String paramterInfoString = "Input data file: " + dataFileName + "\n";
		paramterInfoString += "Number of samples used for learning: " + noOfLearningSamples + "\n";
		paramterInfoString += "Number of samples used for testing: " + noOfTestingSamples + "\n";
		paramterInfoString += "Prediction dimension: " + predictionSteps + "\n";
		paramterInfoString += "Prediction step size: " + stepSize + "s\n";
		paramterInfoString += "Prediction step tolerance: " + tolerance + "s\n";
		paramterInfoString += "oKDE forgetting factor: " + forgettingFactor + "\n";
		paramterInfoString += "oKDE compression threshold: " + compressionThreshold + "\n\n";
		paramterInfoString += "Use only cabs with passengers (1=yes, 0=no, 2=both): " + useOnlyCabsWithPassenger + "\n";
		paramterInfoString += "Use only data from particular time of day (0=[06-12],1=[12-18],2=[18-00],3=[00-06]): "
				+ useParticularToD + "\n\n";

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

		// Read training data (delay vectors) from file
		String delayDataFileName = dataFileName;
		ArrayList<SimpleMatrix> posVectors = readDelayVectors(delayDataFileName, predictionSteps);
		// Check if sufficient data was imported
		if (posVectors.size() < (noOfLearningSamples + noOfTestingSamples)) {
			System.out.println("To few delay vectors.");
			// exit with error
			System.exit(1);
		}

		// UTM projection
		Preprocessor.projectData(posVectors);

		// Create sample model used for kde
		SampleModel dist = new SampleModel(forgettingFactor, compressionThreshold);

		// Calculate start end end indices
		int start = posVectors.size() - noOfLearningSamples - noOfTestingSamples;
		int stop = posVectors.size() - noOfTestingSamples;

		// Initialize kde using first 3 samples
		double[] w = { 1, 1, 1 };
		double[][] c = new double[posVectors.get(0).numRows()][posVectors.get(0).numRows()];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c.length; j++) {
				c[i][j] = 0;
			}
		}
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
		meansA.add(posVectors.get(start));
		meansA.add(posVectors.get(start + 1));
		meansA.add(posVectors.get(start + 2));

		// Execute oKDE algorithm to estimate the sample distribution in the
		// delay embedding space
		long startTime = System.currentTimeMillis();
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			long time = 0;

			for (int i = start + 3; i < stop; i++) {
				if ((System.currentTimeMillis() - startTime) / 1000 > maxUpdateTime) {
					System.out.println("Learning took too much time.");
					System.exit(1);
				}

				// After every 100 samples display some progress information
				if (i % 100 == 0) {
					time = System.currentTimeMillis() - time;
					System.out.println(i + " " + (time) + "ms");
					System.out.println("Components: " + dist.getSubMeans().size());
					System.out.println("Added by EM: " + dist.mEMCount);
					time = System.currentTimeMillis();
				}
				SimpleMatrix pos = posVectors.get(i);
				dist.updateDistribution(pos, new SimpleMatrix(c), 1d);
				meansA.add(pos);

			}
		} catch (EmptyDistributionException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		System.out.println("Time needed for learning: " + (System.currentTimeMillis() - startTime) / 1000);

		// Write estimated distribution to file
		writeKDEToFile(dist, kdeFileName);

		System.out.println("$-----------------------------------------------------------------");
		System.out.println("Start prediction...");

		// Transform delay vectors to measurement arrays needed for mode finding
		List<Measurement[]> testDataVectors = toMeasurements(posVectors);

		// TODO: UTM zone is hardcoded:
		int UTMZoneNumber = 10;
		char UTMZoneLetter = 'S';

		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>();
		ExecutorService executor = Executors.newFixedThreadPool(noOfWorkerThreads);

		for (int i = testDataVectors.size() - noOfTestingSamples; i < testDataVectors.size(); i++) {
			// Create worker tasks for each test data vector
			Callable<Double> worker = new PredictionWorker(testDataVectors.get(i), dist, searchRadius,
					searchSegmentDistance, accuracyRadius, predictionSegments, UTMZoneNumber, UTMZoneLetter, false);
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

	public static ArrayList<SimpleMatrix> readDelayVectors(String filename, int predictionSteps) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.trim();
				String[] posData = sCurrentLine.split(" ");
				SimpleMatrix m = new SimpleMatrix(predictionSteps * 2, 1);
				int offset = posData.length - predictionSteps * 2;
				for (int i = 0; i < predictionSteps * 2; i++)
					m.set(i, 0, Double.parseDouble(posData[i + offset]));
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
			Measurement[] batch = new Measurement[m.numRows() / 2];
			for (int i = 0; i < m.numRows() - 1; i += 2) {
				Measurement meas = new Measurement(m.get(i, 0), m.get(i + 1, 0), 0);
				batch[i / 2] = meas;
			}
			measurements.add(batch);
		}
		return measurements;
	}

	private static void dataToFile(List<SimpleMatrix> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("data1.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.print("[");
			for (int i = 0; i < data.get(0).numRows(); i++) {
				for (SimpleMatrix m : data)
					pw.print(m.get(i, 0) + " ");
				if (i < (data.get(0).numRows() - 1))
					pw.print(";");
			}
			pw.print("]");
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
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

	private static void testDataToFile(List<Measurement> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("testData.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < data.size(); i++) {
				pw.println(data.get(i).getLat() + " " + data.get(i).getLng());
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
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

	private static void measurementsToHeatMapFile(List<Measurement[]> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("input_dataForHeatMap_weighted.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < 10; i++) {
				pw.println(data.get(i)[0].getLat() + " " + data.get(i)[0].getLng() + " " + i);
				pw.println(data.get(i)[1].getLat() + " " + data.get(i)[1].getLng() + " " + i);
				pw.println(data.get(i)[2].getLat() + " " + data.get(i)[2].getLng() + " " + i);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	private static ArrayList<SimpleMatrix> readFromFile() {
		BufferedReader br = null;
		String[] matrix;
		String[] matrixRow1 = null;
		String[] matrixRow2 = null;

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader("data.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				data += sCurrentLine;
			}
			data = data.substring(1);
			data = data.substring(0, data.length() - 1);
			matrix = data.split(";");
			matrixRow1 = matrix[0].split(" ");
			matrixRow2 = matrix[1].split(" ");

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
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();
		for (int i = 0; i < matrixRow1.length; i++) {
			double[][] d = { { Double.parseDouble(matrixRow1[i]) }, { Double.parseDouble(matrixRow2[i]) } };
			dataArray.add(new SimpleMatrix(d));
		}
		return dataArray;

	}

}
