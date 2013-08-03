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

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;
import org.math.plot.Plot3DPanel;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.Transformation.Postprocessor;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataPaser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.Prediction.Predictor;
import de.tuhh.luethke.oKDE.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class TestWithActualData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList<Measurement> testData = CabDataPaser.parse(args[0]);
		Preprocessor.processTestData(testData);
		//System.out.println(testData.size());
		List<Measurement[]> testDataVectors = PositionalTSTransformer.transformTSDataMeasurements(testData, 3);
		System.out.println(testDataVectors.size());

		testDataToFile(testData);

		LinkedList<Measurement> measurements = CabDataPaser.parse(args[0]);// LatitudeHistoryParser.parse("jonas.kml");
		//Preprocessor.processData(measurements);
		// System.out.println(measurements.size());
		// for (Measurement m : measurements)
		// System.out.println(m.getLat() + " " + m.getLng());
		measurementsToHeatMapFile(testDataVectors);
		System.out.println("--------------");

		List<SimpleMatrix> posVectors = PositionalTSTransformer.transformTSData1(measurements, 3);
		
		
		Preprocessor.projectData(posVectors);

		System.out.println(posVectors.size() + " Datenpunkte");
		// System.out.println("matrices");
		// for(SimpleMatrix m : matrices)
		// System.out.println(m);
		SampleModel dist = new SampleModel();

		ArrayList<SimpleMatrix> means = new ArrayList<SimpleMatrix>();
		double[] weights = new double[500];
		ArrayList<SimpleMatrix> cov1 = new ArrayList<SimpleMatrix>();
		double[] w = { 1, 1, 1 };
		double[][] c = { { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };
		// double[][] c = { { 0.0, 0.0}, { 0.0, 0.0 } };
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
		// double[][] mean1 = { { 0 }, { 3 }, { 0 }, { 2 } };
		// double[][] mean2 = { { 0 }, { 1 }, { 4 }, { 0 } };
		// double[][] mean3 = { { 0 }, { 2 }, { 0 }, { 3 } };
		//double[][] mean1 = { { 37.810462 }, { -122.36472 }, { 37.783604 }, { -122.395104 }, { 37.783604 }, { -122.395104 } };
		//double[][] mean2 = { { 37.783604 }, { -122.395104 }, { 37.764744 }, { -122.404888 }, { 37.764744 }, { -122.404888 } };
		//double[][] mean3 = { { 37.764744 }, { -122.404888 }, { 37.764744 }, { -122.419479 }, { 37.764744 }, { -122.419479 } };
		int start = posVectors.size()-Integer.valueOf(args[1])-Integer.valueOf(args[2]);
		int stop = posVectors.size()-Integer.valueOf(args[2]);
		meansA.add(posVectors.get(start));
		meansA.add(posVectors.get(start+1));
		meansA.add(posVectors.get(start+2));
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			double d1 = 0, d2 = 0, d3 = 0, d4 = 0;
			long time = 0;
			
			for (int i = start+3; i < stop; i++) {
				if (i % 100 == 0){
					System.out.println(i+" "+(System.currentTimeMillis()-time)+"ms");
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
		ArrayList<SimpleMatrix> weightedCoordinates = new ArrayList<SimpleMatrix>();
		for (int i = 0; i < dist.getSubDistributions().size(); i++) {
			System.out.println(dist.getSubMeans().get(i) + " w: " + dist.getSubWeights().get(i));
			/*
			 * for(int j=0; j<dist.getSubWeights().get(i)*1000; j++){
			 * weightedCoordinates.add(new
			 * SimpleMatrix(dist.getSubMeans().get(i))); }
			 */
		}
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
		dataToFile(meansA);
		System.out.println(meansA.size() + " effektive Datenpunkte");

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

		ArrayList<Future<Double>> futureResults = new ArrayList<Future<Double>>();
		ExecutorService executor = Executors.newFixedThreadPool(8);
		for (int i = testDataVectors.size()-Integer.valueOf(args[2]); i < testDataVectors.size(); i++) {
			Callable<Double> worker = new TestWorker(testDataVectors.get(i), dist);
			futureResults.add(executor.submit(worker));
		}

		double avg = 0;
		for (int i = 0; i < futureResults.size(); i++) {
			try {
				if (i > 0)
					avg = ((float)i / (float)(i+1)) * avg + (1f / (float)(i+1)) * futureResults.get(i).get();
				else
					avg = futureResults.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		executor.shutdownNow();
		System.out.println("avg: "+avg);
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

	public static double[][] calculateY1(double[] x, double[] y, double setX, double setY, double setX1, double setY1, SampleModel dist,
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

			}
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
			double prob = dist.cummulativeConditional(pointVector1, m, 400, 2, 2);
			z[filtered.get(i)[0]][filtered.get(i)[1]] = prob;
			sum += prob;
			System.out.println(sum);
		}
		System.out.println("SUM=" + sum);
		dataToHeatMapFile(weightedCoordinates);
		return z;
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

	private static void dataToFile(double[][] data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("z.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					pw.print(data[i][j] + " ");
				}
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
