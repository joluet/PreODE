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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.DelayEmbedder;
import de.tuhh.luethke.PrePos.Transformation.Postprocessor;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.LatitudeHistoryParser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.PrePos.utility.PLTParser;
import de.tuhh.luethke.okde.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.okde.model.BaseSampleDistribution;
import de.tuhh.luethke.okde.model.SampleModel;

public class TestGeneric {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int argCount = 0;
		String dataFileName = args[argCount++];
		int noOfLearningSamples = Integer.valueOf(args[argCount++]);
		int noOfTestingSamples = Integer.valueOf(args[argCount++]);
		int predictionSteps = Integer.valueOf(args[argCount++]);
		int stepSize = Integer.valueOf(args[argCount++]);
		int tolerance = Integer.valueOf(args[argCount++]);
		double forgettingFactor = Double.valueOf(args[argCount++]);
		double compressionThreshold = Double.valueOf(args[argCount++]);
		int useOnlyCabsWithPassenger = Integer.valueOf(args[argCount++]);
		int useParticularToD = Integer.valueOf(args[argCount++]);

		String kdeFileName = args[argCount++];
		
		//Parameters for evaluation of KDE and optimization
		int searchRadius = Integer.valueOf(args[argCount++]);
		int searchSegmentDistance = Integer.valueOf(args[argCount++]);
		int accuracyRadius = Integer.valueOf(args[argCount++]);
		int predictionSegments = Integer.valueOf(args[argCount++]);
		
		
		//parameters for execution
		int noOfWorkerThreads = Integer.valueOf(args[argCount++]);
		long maxUpdateTime = Long.valueOf(args[argCount++])*60;
		long maxPredictionTime = Long.valueOf(args[argCount++])*60;


		String paramterInfoString = "Input data file: "+dataFileName+"\n";
		paramterInfoString += "Number of samples used for learning: "+noOfLearningSamples+"\n";
		paramterInfoString += "Number of samples used for testing: "+noOfTestingSamples+"\n";
		paramterInfoString += "Prediction dimension: "+predictionSteps+"\n";
		paramterInfoString += "Prediction step size: "+stepSize+"s\n";
		paramterInfoString += "Prediction step tolerance: "+tolerance+"s\n";
		paramterInfoString += "oKDE forgetting factor: "+forgettingFactor+"\n";
		paramterInfoString += "oKDE compression threshold: "+compressionThreshold+"\n\n";
		paramterInfoString += "Use only cabs with passengers (1=yes, 0=no, 2=both): "+useOnlyCabsWithPassenger+"\n";
		paramterInfoString += "Use only data from particular time of day (0=[06-12],1=[12-18],2=[18-00],3=[00-06]): "+useParticularToD+"\n\n";
		
		paramterInfoString += "Radius to search for maxima during prediction: "+searchRadius+"\n";
		paramterInfoString += "Distance that defines how fine-meshed the search for maxima: "+searchSegmentDistance+"\n";
		paramterInfoString += "Radius to use when estimating the cumulative probability around a prediction point: "+accuracyRadius+"\n";
		paramterInfoString += "Number of segements to be used during estimation of the cumulative probability: "+predictionSegments+"\n\n";
		
		paramterInfoString += "KDE output file name: "+kdeFileName+"\n";
		paramterInfoString += "Number of worker threads: "+noOfWorkerThreads+"\n";
		paramterInfoString += "Maximum time for learning: "+maxUpdateTime+"s\n";
		paramterInfoString += "Maximum time for predicting: "+maxPredictionTime+"s\n";

		int overallSamples = noOfLearningSamples + noOfTestingSamples;
		
		// print info
		System.out.println(paramterInfoString);
		
		// UTM zones are hardcoded for file types!:
		/*int UTMZoneNumber = 0;
		char  UTMZoneLetter = ' ';
		LinkedList<Measurement> testData = null;
		if(dataFileName.endsWith(".txt")){
			// San Francisco
			UTMZoneNumber = 10;
			UTMZoneLetter = 'S';
			testData = CabDataParser.parseExtended(dataFileName);
		}else if(dataFileName.endsWith(".kml")){
			// Hamburg
			UTMZoneNumber = 32;
			UTMZoneLetter = 'U';
			testData = LatitudeHistoryParser.parse(dataFileName);
		}else if(dataFileName.contains("plt")){
			// Beijing
			UTMZoneNumber = 50;
			UTMZoneLetter = 'S';
			testData = PLTParser.parse(dataFileName);
		}
		System.out.println(testData.size()+" samples found.");
		Preprocessor.processTestData(testData, useOnlyCabsWithPassenger, useParticularToD);
		List<Measurement[]> testDataVectors = PositionalTSTransformer.transformTSDataMeasurements(testData, predictionSteps, stepSize, tolerance, overallSamples);


		//Preprocessor.processData(measurements);
		// System.out.println(measurements.size());
		// for (Measurement m : measurements)
		// System.out.println(m.getLat() + " " + m.getLng());
		
		System.out.println("$-----------------------------------------------------------------");

		List<SimpleMatrix> posVectors = PositionalTSTransformer.transformTSData1(testData, predictionSteps, stepSize, tolerance, overallSamples);
		System.out.println("Extracted "+testDataVectors.size()+" possible test- and learning-vectors.");
		System.out.println("Start learning...\n");*/
		int UTMZoneNumber = 10;
		char UTMZoneLetter = 'S';
		String trainingDataFileName = dataFileName;
		ArrayList<SimpleMatrix> posVectors = readTrainingData(trainingDataFileName, predictionSteps);
		List<Measurement[]> testDataVectors = toMeasurements(posVectors);
		
		dataToHeatMapFile(posVectors);

		if(posVectors.size() < (noOfLearningSamples + noOfTestingSamples)){
			System.out.println("To few data vectors could be extracted from data set!");
			// exit with error
			System.exit(1);
		}
			
		
		Preprocessor.projectData(posVectors);

		SampleModel dist = new SampleModel(forgettingFactor, compressionThreshold);

		double[] w = { 1, 1, 1 };
		
		double[][] c = new double[posVectors.get(0).numRows()][posVectors.get(0).numRows()];
		for(int i=0; i<c.length; i++){
			for(int j=0; j<c.length; j++){
				c[i][j] = 0;
			}
		}
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
		int start = posVectors.size()-noOfLearningSamples-noOfTestingSamples;
		int stop = posVectors.size()-noOfTestingSamples;
		meansA.add(posVectors.get(start));
		meansA.add(posVectors.get(start+1));
		meansA.add(posVectors.get(start+2));
		long startTime = System.currentTimeMillis();
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			long time = 0;
			
			for (int i = start+3; i < stop; i++) {
				if((System.currentTimeMillis()-startTime)/1000 > maxUpdateTime) {
					System.out.println("Learning took too much time.");
					System.exit(1);
				}
					
				if (i % 100 == 0){
					time = System.currentTimeMillis()-time;
					System.out.println(i+" "+(time)+"ms");
					System.out.println("Components: "+dist.getSubMeans().size());
					System.out.println("Added by EM: "+dist.mEMCount);
					//System.out.println("Average EM-error: "+dist.mEMError+" ("+dist.mEMCount+")");
					//increase compression threshold if distribution updates take too long
					/*if(time > 5000 && time < 13000) {
						dist.mCompressionThreshold = dist.mCompressionThreshold*1.1;
						System.out.println("Increased compression threshold to: "+dist.mCompressionThreshold);
					}*/
					/*if(time >= 9000) {
						dist.mCompressionThreshold = dist.mCompressionThreshold*1.3;
						System.out.println("Increased compression threshold to: "+dist.mCompressionThreshold);
					}*/
					time = System.currentTimeMillis();
				}
				/*
				 * if (i % 3 == 0) { d1 = StdRandom.gaussian(1, 0.2); d2 =
				 * StdRandom.gaussian(1, 0.2); d3 = StdRandom.gaussian(1, 0.2);
				 * d4 = StdRandom.gaussian(3, 0.2); } else if (i % 2 == 0) { d1
				 * = StdRandom.gaussian(1, 0.2); d2 = StdRandom.gaussian(3,
				 * 0.2); d3 = StdRandom.gaussian(3, 0.2); d4 =
				 * StdRandom.gaussian(3, 0.2); } else if (i % 1 == 0) { d1 =
				 * StdRandom.gaussian(3, 0.2); d2 = StdRandom.gaussian(3, 0.2);
				 * d3 = StdRandom.gaussian(1, 0.2); d4 = StdRandom.gaussian(1,
				 * 0.2); }
				 */
				SimpleMatrix pos = posVectors.get(i);
				dist.updateDistribution(pos, new SimpleMatrix(c), 1d);
				meansA.add(pos);

			}
		} catch (EmptyDistributionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		System.out.println("Time needed for leraning: "+(System.currentTimeMillis()-startTime)/1000);
		writeKDEToFile(dist, kdeFileName);
		
		/*System.out.println("*******************************************");
		for(int i=0; i<10; i++) {
			SimpleMatrix m = new SimpleMatrix(6,1);
			m.set(0,0,testDataVectors.get(i)[0].getLat());
			m.set(1,0,testDataVectors.get(i)[0].getLng());
			m.set(2,0,testDataVectors.get(i)[1].getLat());
			m.set(3,0,testDataVectors.get(i)[1].getLng());
			m.set(4,0,testDataVectors.get(i)[2].getLat());
			m.set(5,0,testDataVectors.get(i)[2].getLng());
			System.out.println(Preprocessor.projectData6(m));
		}
		System.out.println("*******************************************");
		for(int i=0; i<10; i++) {
			System.out.println(posVectors.get(i));
		}
		System.out.println("********************************************");*/

		// define your data
		double[] x = new double[50];
		double[] y = new double[50];

		/*
		 * double coord = 52.8; for (int i = 0; i < 100; i++) { coord += 0.03;
		 * x[i] = coord; }
		 * 
		 * coord = 9.1; for (int i = 0; i < 100; i++) { coord += 0.01; y[i] =
		 * coord; }
		 */
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
		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>();
		ExecutorService executor = Executors.newFixedThreadPool(noOfWorkerThreads);
		for (int i = testDataVectors.size()-noOfTestingSamples; i < testDataVectors.size(); i++) {
			Callable<Double> worker = new TestWorkerXStep(testDataVectors.get(i), dist, searchRadius, searchSegmentDistance, accuracyRadius, predictionSegments, UTMZoneNumber, UTMZoneLetter, false);
			futureResults.add(executor.submit(worker));
		}
		startTime = System.currentTimeMillis();
		double avg = 0;
		for (int i = 0; i < futureResults.size(); i++) {
			if((System.currentTimeMillis()-startTime)/1000 > maxPredictionTime) {
				System.out.println("Prediction took too much time.");
				System.exit(1);
			}
			try {
				if(futureResults.get(i).get() != null) {
					if (i > 0)
						avg = ((float)i / (float)(i+1)) * avg + (1f / (float)(i+1)) * futureResults.get(i).get();
					else
						avg = futureResults.get(i).get();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Time needed for prediction: "+(System.currentTimeMillis()-startTime)/1000);
		executor.shutdownNow();
/*
		for (int i = 12; i < 999; i += 3) {
			long time = System.currentTimeMillis();
			// z1 = calculateY1(x, y,
			// testData.get(i).getLat(),testData.get(i).getLng(),testData.get(i+1).getLat(),testData.get(i+1).getLng(),
			// dist, weightedCoordinates, false);
			double[][] dxVector = new double[4][1];
			dxVector[0][0] = testData.get(i).getLat();
			dxVector[1][0] = testData.get(i).getLng();
			dxVector[2][0] = testData.get(i + 1).getLat();
			dxVector[3][0] = testData.get(i + 1).getLng();
			/*
			 * dxVector[4][0] = testData .get(i+2). getLat(); dxVector[5][0] =
			 * testData .get(i+2). getLng();
			 */
/*			System.out.println("POS: " + testData.get(i + 2).getLat() + "," + testData.get(i + 2).getLng());
			// double[][] dxVector = { { x[i] }, { y[j] } };
			SimpleMatrix pointVector = new SimpleMatrix(dxVector);
			SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);

			dxVector = new double[2][1];
			dxVector[0][0] = testData.get(i + 2).getLat();
			dxVector[1][0] = testData.get(i + 2).getLng();
			pointVector = new SimpleMatrix(dxVector);
			SimpleMatrix pointVector2 = Preprocessor.projectData(pointVector);

			double prob = dist.trapezoidRule(pointVector1, pointVector2, 1000, 10, 10);
			if (prob != 0)
				System.out.println("prob: " + prob);
			else
				System.out.println(0);
			results[i / 3] = prob;
			double k = i;
			if (k > 1)
				avg = ((k - 1) / k) * avg + (1 / k) * prob;
			else
				avg = prob;
			System.out.println("Time: " + (System.currentTimeMillis() - time));
		}

		/*
		 * ExecutorService executor = Executors.newFixedThreadPool(8);
		 * ArrayList<Future<Double>> futureResults = new
		 * ArrayList<Future<Double>>(1000); for (int i = 0; i < 1000; i++) {
		 * ArrayList<Measurement> tmp = new ArrayList<Measurement>(); for (int j
		 * = i; j < (i + 3); j++) { tmp.add(testData.get(j)); } Callable<Double>
		 * worker = new TestWorker(tmp, dist);
		 * futureResults.add(executor.submit(worker)); } for (int i = 0; i <
		 * 1000; i++) { try { results[i] = futureResults.get(i).get();
		 * System.out.println(i); double k = i; if (k > 1) avg = ((k - 1) / k) *
		 * avg + (1 / k) * results[i]; else avg = results[i]; } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (ExecutionException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * 
		 * }
		 * 
		 * System.out.println("AVG: " + avg); Arrays.sort(results);
		 * System.out.println("Median: " + results[500]);
		 */
/*		System.out.println(testData.get(10).getLat() + " " + testData.get(10).getLng() + " - " + testData.get(11).getLat() + " "
				+ testData.get(11).getLng());
		z1 = calculateY1(x, y, testData.get(10).getLat(), testData.get(10).getLng(), testData.get(11).getLat(), testData.get(11).getLng(), dist,
				weightedCoordinates, false);
		dataToFile(z1);
		// create your PlotPanel (you can use it as a JPanel) with a legend at
		// SOUTH

		Plot3DPanel plot = new Plot3DPanel("SOUTH");

		// add grid plot to the PlotPanel
		plot.addGridPlot("kernel", x, y, z1);

		// put the PlotPanel in a JFrame like a JPanel
		JFrame frame = new JFrame("a plot panel");
		frame.setSize(600, 600);
		frame.setContentPane(plot);
		frame.setVisible(true);*/

	}

	/*
	 * public static double calculateY(double x, double y, SampleDist dist,
	 * ArrayList<SimpleMatrix> means) { double z = cos(x * PI) * sin(y * PI);
	 * return z; }
	 */

	/*public static double[][] calculateY1(double[] x, double[] y, double setX, double setY, double setX1, double setY1, SampleModel dist,
			ArrayList<SimpleMatrix> weightedCoordinates, boolean swap) {
		double[][] z = new double[x.length][y.length];
		ExecutorService executor = Executors.newFixedThreadPool(8);
		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>(x.length * y.length);
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < y.length; j++) {
				// System.out.println("x, y: "+x[i]+" - "y[j]);
				double[][] dxVector = new double[6][1];
				dxVector[0][0] = setX;
				dxVector[1][0] = setY;
				dxVector[2][0] = setX1;
				dxVector[3][0] = setY1;
				dxVector[4][0] = 0;
				dxVector[5][0] = 0;

				// double[][] dxVector = { { x[i] }, { y[j] } };
				SimpleMatrix pointVector = new SimpleMatrix(dxVector);
				SimpleMatrix pointVector1 = Preprocessor.projectData6(pointVector);
				pointVector1.set(4, 0, x[i]);
				pointVector1.set(5, 0, y[j]);
				int[] condDim = { 0, 1, 2, 3 };
				Callable<Double> worker = new EvaluationWorker(pointVector1, condDim, dist);
				futureResults.add(executor.submit(worker));
				// z[i][j] = dist.evaluateConditional(pointVector1,condDim);
				/*
				 * if(z[i][j] > 1E-6){ System.out.println(x[i] +" " +y[j]);
				 * System.out.println(z[i][j]); }
				 */

				/*
				 * for (int k = 1; k < ((int) (z[i][j] * 100000000)); k++) {
				 * SimpleMatrix m = new SimpleMatrix(2, 1); m.set(0, 0, x[i]);
				 * m.set(1, 0, y[j]); weightedCoordinates.add(new
				 * SimpleMatrix(m)); }
				 */

		/*	}
		}

		for (int i = 0; i < futureResults.size(); i++) {
			try {
				z[i / y.length][i % y.length] = futureResults.get(i).get();
				System.out.println(i / y.length + " " + i % y.length + " = " + z[i / y.length][i % y.length]);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		executor.shutdownNow();
		ArrayList<int[]> filtered = Postprocessor.medianFilter(z);
		double sum = 0;
		double[][] dxVector = new double[4][1];
		dxVector[0][0] = setX;
		dxVector[1][0] = setY;
		dxVector[2][0] = setX1;
		dxVector[3][0] = setY1;
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);

		System.out.println("dist: " + (x[x.length - 1] - x[0]) + " " + (y[y.length - 1] - y[0]));
		for (int i = 0; i < filtered.size(); i++) {
			SimpleMatrix m = new SimpleMatrix(2, 1);
			m.set(0, 0, x[filtered.get(i)[0]]);
			m.set(1, 0, y[filtered.get(i)[1]]);
			weightedCoordinates.add(Preprocessor.projectDataBack(m));
			//double prob = dist.cummulativeConditional(pointVector1, m, 400, 2, 2);
			//z[filtered.get(i)[0]][filtered.get(i)[1]] = prob;
			//sum += prob;
			System.out.println(sum);
		}
		System.out.println("SUM=" + sum);
		dataToHeatMapFile(weightedCoordinates);
		return z;
	}*/
	
	public static ArrayList<SimpleMatrix> readTrainingData(String filename, int predictionSteps) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.trim();
				String[] posData = sCurrentLine.split(" ");
				SimpleMatrix m = new SimpleMatrix(predictionSteps*2,1);
				int offset = posData.length - predictionSteps*2;
				for(int i=0; i<predictionSteps*2; i++)
					m.set(i,0,Double.parseDouble(posData[i+offset]));
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
		for(SimpleMatrix m : vectors) {
			Measurement[] batch = new Measurement[m.numRows()/2];
			for(int i=0; i<m.numRows()-1; i+=2){
				Measurement meas = new Measurement(m.get(i,0), m.get(i+1,0), 0);
				batch[i/2] = meas;
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
				pw.print("Mean: "+subDist.getGlobalMean());
				pw.print("Covariance: "+subDist.getGlobalCovariance());
				pw.print("BandwidthMatrix: "+subDist.getBandwidthMatrix());
				pw.print("Weight: "+subDist.getGlobalWeight());
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
			for (int i=0; i<10; i++){
				pw.println(data.get(i)[0].getLat() + " " + data.get(i)[0].getLng()+ " "+ i);
				pw.println(data.get(i)[1].getLat() + " " + data.get(i)[1].getLng()+ " "+ i);
				pw.println(data.get(i)[2].getLat() + " " + data.get(i)[2].getLng()+ " "+ i);
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
