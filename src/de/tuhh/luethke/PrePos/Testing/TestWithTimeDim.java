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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;
import org.math.plot.Plot3DPanel;

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.oKDE.model.BaseSampleDistribution;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class TestWithTimeDim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList<Measurement> testData = CabDataParser.parse("new_uvjeahot_tst_1.txt");
		testDataToFile(testData);
		
		
		LinkedList<Measurement> measurements = CabDataParser.parse("new_uvjeahot.txt");//LatitudeHistoryParser.parse("jonas.kml");
		Preprocessor.processData(measurements);
		//System.out.println(measurements.size());
		//for (Measurement m : measurements)
		//	System.out.println(m.getLat() + " " + m.getLng());
		measurementsToHeatMapFile(measurements);
		System.out.println("--------------");
		
		List<SimpleMatrix> posVectors = PositionalTSTransformer.transformTSData1(
		  measurements, 2);
		
		Preprocessor.projectData(posVectors);

		System.out.println(posVectors.size()+" Datenpunkte");
		// System.out.println("matrices");
		// for(SimpleMatrix m : matrices)
		// System.out.println(m);
		SampleModel dist = new SampleModel();

		ArrayList<SimpleMatrix> means = new ArrayList<SimpleMatrix>();
		double[] weights = new double[500];
		ArrayList<SimpleMatrix> cov1 = new ArrayList<SimpleMatrix>();
		double[] w = { 1, 1, 1 };
		double[][] c = { { 0.0, 0.0, 0.0, 0.0, 0.0  }, { 0.0, 0.0, 0.0, 0.0, 0.0  }, { 0.0, 0.0, 0.0, 0.0, 0.0 },{ 0.0, 0.0, 0.0, 0.0, 0.0 },{ 0.0, 0.0, 0.0, 0.0, 0.0 }};
		//double[][] c = { { 0.0, 0.0}, { 0.0, 0.0 } };
		SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c), new SimpleMatrix(c) };
		ArrayList<SimpleMatrix> meansA = new ArrayList<SimpleMatrix>();
//		double[][] mean1 = { { 0 }, { 3 }, { 0 }, { 2 } };
//		double[][] mean2 = { { 0 }, { 1 }, { 4 }, { 0 } };
//		double[][] mean3 = { { 0 }, { 2 }, { 0 }, { 3 } };
		double[][] mean1 = { { 37.810462}, {-122.36472  },{37.783604},{-122.395104},{120}};
		double[][] mean2 = { { 37.783604}, {-122.395104  },{37.764744},{ -122.404888},{120}};
		double[][] mean3 = { { 37.764744}, {-122.404888  },{37.764744},{ -122.419479},{120}};
		meansA.add(Preprocessor.projectData4(new SimpleMatrix(mean1)));
		meansA.add(Preprocessor.projectData4(new SimpleMatrix(mean2)));
		meansA.add(Preprocessor.projectData4(new SimpleMatrix(mean3)));
		try {
			dist.updateDistribution(meansA.toArray(new SimpleMatrix[3]), cov, w);

			double d1 = 0, d2 = 0, d3 = 0, d4 = 0;
			for (int i = 0; i < posVectors.size(); i++) {
				if(i%100 == 0)
					System.out.println(i);
				/*if (i % 3 == 0) {
					d1 = StdRandom.gaussian(1, 0.2);
					d2 = StdRandom.gaussian(1, 0.2);
					d3 = StdRandom.gaussian(1, 0.2);
					d4 = StdRandom.gaussian(3, 0.2);
				} else if (i % 2 == 0) {
					d1 = StdRandom.gaussian(1, 0.2);
					d2 = StdRandom.gaussian(3, 0.2);
					d3 = StdRandom.gaussian(3, 0.2);
					d4 = StdRandom.gaussian(3, 0.2);
				} else if (i % 1 == 0) {
					d1 = StdRandom.gaussian(3, 0.2);
					d2 = StdRandom.gaussian(3, 0.2);
					d3 = StdRandom.gaussian(1, 0.2);
					d4 = StdRandom.gaussian(1, 0.2);
				}*/
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
		for(int i=0; i<dist.getSubDistributions().size(); i++){
			System.out.println(dist.getSubMeans().get(i)+" w: " +dist.getSubWeights().get(i));
			/*for(int j=0; j<dist.getSubWeights().get(i)*1000; j++){
				weightedCoordinates.add(new SimpleMatrix(dist.getSubMeans().get(i)));
			}*/
		}
		
		dataToFile(meansA);
		System.out.println(meansA.size()+" effektive Datenpunkte");

		// define your data
		double[] x = new double[400];
		double[] y = new double[400];
		
		/*double coord = 52.8;
		 * for (int i = 0; i < 100; i++) {
			coord += 0.03;
			x[i] = coord;
		}

		 coord = 9.1;
		for (int i = 0; i < 100; i++) {
			coord += 0.01;
			y[i] = coord;
		}*/
		double coord = 37;
		for (int i = 0; i < 100; i++) {
			coord += 0.01;
			x[i] = coord;
		}

		 coord = -122;
		for (int i = 0; i < 100; i++) {
			coord -= 0.01;
			y[i] = coord;
		}
		
		double[][] z1;
		double avg = 0;
		double[] results = new double[333];
		/*for(int i=0; i<999; i+=3){
			
			//z1 = calculateY1(x, y, testData.get(i).getLat(), testData.get(i).getLng(),testData.get(i+1).getLat(), testData.get(i+1).getLng(), dist, weightedCoordinates, false);
			double[][] dxVector = new double[4][1];
			dxVector[0][0] = testData.get(i).getLat();
			dxVector[1][0] = testData.get(i).getLng();
			dxVector[2][0] = testData.get(i+1).getLat();
			dxVector[3][0] = testData.get(i+1).getLng();
			/*dxVector[4][0] = testData.get(i+2).getLat();
			dxVector[5][0] = testData.get(i+2).getLng();***
			System.out.println("POS: "+testData.get(i+2).getLat()+","+testData.get(i+2).getLng());
			// double[][] dxVector = { { x[i] }, { y[j] } };
			SimpleMatrix pointVector = new SimpleMatrix(dxVector);
			SimpleMatrix pointVector1 = Postprocessor.projectData4(pointVector);
			
			dxVector = new double[2][1];
			dxVector[0][0] = testData.get(i+2).getLat();
			dxVector[1][0] = testData.get(i+2).getLng();
			pointVector = new SimpleMatrix(dxVector);
			SimpleMatrix pointVector2 = Postprocessor.projectData(pointVector);

			double prob = dist.trapezoidRule(pointVector1, pointVector2, 100, 100);
			if(prob!=0)
				System.out.println("prob: "+prob);
			else
				System.out.println(0);
			results[i/3] = prob;
			double k = i;
			if(k>1)
				avg = ((k-1)/k)*avg + (1/k)*prob;
			else
				avg = prob;
		}
		System.out.println("AVG: "+avg);
		Arrays.sort(results);
		System.out.println("Median: "+results[166]);*/
		z1 = calculateY1(x, y, testData.get(10).getLat(),testData.get(10).getLng(), 360, dist, weightedCoordinates, false);
		dataToHeatMapFile(weightedCoordinates);
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

	public static double[][] calculateY1(double[] x, double[] y, double setX, double setY, long timediff ,BaseSampleDistribution dist, ArrayList<SimpleMatrix> weightedCoordinates, boolean swap) {
		double[][] z = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < y.length; j++) {
				// System.out.println("x, y: "+x[i]+" - "y[j]);
				double[][] dxVector = new double[5][1];
					dxVector[0][0] = setX;
					dxVector[1][0] = setY;
					dxVector[2][0] = x[i];
					dxVector[3][0] = y[j];
					dxVector[4][0] = timediff;
				// double[][] dxVector = { { x[i] }, { y[j] } };
				SimpleMatrix pointVector = new SimpleMatrix(dxVector);
				SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);
				z[i][j] = dist.evaluate(pointVector1);
				/*for(int k=1; k<((int)(z[i][j]/10000000)); k++){
					SimpleMatrix m = new SimpleMatrix(2,1);
					m.set(0,0,pointVector.get(4,0));
					m.set(1,0,pointVector.get(5,0));
					weightedCoordinates.add(new SimpleMatrix(m));
				}*/
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
	
	private static void testDataToFile(List<Measurement> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("testData.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < data.size(); i++) {
				pw.println(data.get(i).getLat() + " "+data.get(i).getLng() );
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
				pw.println(m.get(0, 0) + " "+m.get(1, 0) );
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
				pw.println(m.getLat() + " "+m.getLng() );
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
