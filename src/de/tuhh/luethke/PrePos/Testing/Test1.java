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

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;
import org.math.plot.Plot3DPanel;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.StdRandom;
import de.tuhh.luethke.oKDE.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.oKDE.model.BaseSampleDistribution;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * LinkedList<Measurement> measurements =
		 * CabDataPaser.parse("custom_TestData.txt"
		 * );//LatitudeHistoryParser.parse("jonas.kml");
		 * Postprocessor.processData(measurements);
		 * //Postprocessor.projectData(measurements);
		 * //System.out.println(measurements.size()); //for (Measurement m :
		 * measurements) // System.out.println(m.getLat() + " " + m.getLng());
		 * measurementsToHeatMapFile(measurements);
		 * 
		 * 
		 * List<SimpleMatrix> posVectors =
		 * PositionalTSTransformer.transformTSData( measurements, 2);
		 * System.out.println(posVectors.size()+" Datenpunkte"); //
		 * System.out.println("matrices"); // for(SimpleMatrix m : matrices) //
		 * System.out.println(m);
		 */
		SampleModel dist = new SampleModel();

		double[] w = { 1, 1, 1 };
		double[][] c = { { 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0 } };
		// double[][] c = { { 0.0, 0.0}, { 0.0, 0.0 } };
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
		double[][] mean1 = { { 0 }, { 0 }, { 1 }, { 1 } };
		double[][] mean2 = { { 1 }, { 1 }, { 0 }, { 0 } };
		double[][] mean3 = { { 0 }, { 0 }, { 1 }, { 1 } };
		// double[][] mean1 = { { 37.810462}, {-122.36472 }};
		// double[][] mean2 = { { 37.783604}, {122.395104 }};
		// double[][] mean3 = { { 37.764744}, {-122.404888 }};
		meansA.add(new SimpleMatrix(mean1));
		meansA.add(new SimpleMatrix(mean2));
		meansA.add(new SimpleMatrix(mean3));
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			double d1 = 0, d2 = 0, d3 = 0, d4 = 0;
			for (int i = 0; i < 10; i++) {
				if (i % 3 == 0) {
					d1 = 1;//StdRandom.gaussian(1, 0.1);
					d2 = 2;//StdRandom.gaussian(2, 0.1);
					d3 = 1;//StdRandom.gaussian(1, 0.1);
					d4 = 3;//StdRandom.gaussian(3, 0.1);
				} else if (i % 2 == 0) {
					d1 = 1;//StdRandom.gaussian(1, 0.1);
					d2 = 3;//StdRandom.gaussian(3, 0.1);
					d3 = 3;//StdRandom.gaussian(3, 0.1);
					d4 = 3;//StdRandom.gaussian(3, 0.1);
				} else if (i % 1 == 0) {
					d1 = 3;//StdRandom.gaussian(3, 0.1);
					d2 = 1;//StdRandom.gaussian(3, 0.1);
					d3 = 1;//StdRandom.gaussian(1, 0.1);
					d4 = 2;//StdRandom.gaussian(2, 0.1);
				}
				double[][] dataVector = { { d1 }, { d2 }, { d3 }, { d4 } };
				SimpleMatrix pos = new SimpleMatrix(dataVector);
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
		// ArrayList<SimpleMatrix> weightedCoordinates = new
		// ArrayList<SimpleMatrix>();

		for (int i = 0; i < dist.getSubDistributions().size(); i++) {
			System.out.println(dist.getSubMeans().get(i) + " w: " + dist.getSubWeights().get(i));

		}

		// dataToFile(meansA);
		System.out.println(meansA.size() + " effektive Datenpunkte");
		
		System.out.println("BW: "+dist.getBandwidthMatrix());

		// define your data
		double[] x = new double[100];
		double[] y = new double[100];

		/*
		 * double coord = 52.8; for (int i = 0; i < 100; i++) { coord += 0.03;
		 * x[i] = coord; }
		 * 
		 * coord = 9.1; for (int i = 0; i < 100; i++) { coord += 0.01; y[i] =
		 * coord; }
		 */
		double coord = 0;
		for (int i = 0; i < 100; i++) {
			coord += .1;
			x[i] = coord;
		}

		coord = 0;
		for (int i = 0; i < 100; i++) {
			coord += .1;
			y[i] = coord;
		}
		double[][] z1;
		double[][] dxVector = new double[2][1];
		dxVector[0][0] = 1;
		dxVector[1][0] = 2;
		// double[][] dxVector = { { x[i] }, { y[j] } };
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		dxVector[0][0] = 1;
		dxVector[1][0] = 3;
		SimpleMatrix testVector = new SimpleMatrix(dxVector);
		double prob = dist.trapezoidRule1(pointVector,testVector, 50,50);
		System.out.println("prob "+prob);
		z1 = calculateY(x, y, 1, 2, dist, false);
		// dataToHeatMapFile(weightedCoordinates);
		// create your PlotPanel (you can use it as a JPanel) with a legend at
		// SOUTH
		Plot3DPanel plot = new Plot3DPanel("SOUTH");

		// add grid plot to the PlotPanel
		plot.addGridPlot("kernel", x, y, z1);

		// put the PlotPanel in a JFrame like a JPanel
		JFrame frame = new JFrame("a plot panel");
		frame.setSize(600, 600);
		frame.setContentPane(plot);
		frame.setVisible(true);

	}

	/*
	 * public static double calculateY(double x, double y, SampleDist dist,
	 * ArrayList<SimpleMatrix> means) { double z = cos(x * PI) * sin(y * PI);
	 * return z; }
	 */

	public static double[][] calculateY(double[] x, double[] y, double setX, double setY, BaseSampleDistribution dist, boolean swap) {
		double[][] z = new double[y.length][x.length];
		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < y.length; j++) {
				// System.out.println("x, y: "+x[i]+" - "y[j]);
				double[][] dxVector = new double[4][1];
				if (!swap) {
					dxVector[0][0] = setX;
					dxVector[1][0] = setY;
					dxVector[3][0] = x[i];
					dxVector[2][0] = y[j];
				} else {
					dxVector[3][0] = setX;
					dxVector[2][0] = setY;
					dxVector[0][0] = x[i];
					dxVector[1][0] = y[j];
				}
				// double[][] dxVector = { { x[i] }, { y[j] } };
				SimpleMatrix pointVector = new SimpleMatrix(dxVector);
				// pointVector = Postprocessor.projectData(pointVector);
				z[i][j] = dist.evaluate(pointVector);

			}
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

	private static void measurementsToHeatMapFile(List<Measurement> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("input_dataForHeatMap_weighted.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (Measurement m : data)
				pw.println(m.getLat() + " " + m.getLng());
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
