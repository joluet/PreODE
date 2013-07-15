package de.tuhh.luethke.PrePos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;
import org.math.plot.Plot3DPanel;

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.utility.LatitudeHistoryParser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.StdRandom;
import de.tuhh.luethke.oKDE.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.oKDE.model.SampleDist;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
	//LinkedList<Measurement> measurements = LatitudeHistoryParser
	//	.parse("test.kml");
	//Postprocessor.processData(measurements);
	//System.out.println(measurements.size());
	// for(Measurement m : measurements)
	// System.out.println(m.getLat() + " " +m.getLng());
	
	/*List<SimpleMatrix> means = PositionalTSTransformer.transformTSData(
		measurements, 2)*/;
		
	// System.out.println("matrices");
	// for(SimpleMatrix m : matrices)
	// System.out.println(m);
	SampleDist dist = new SampleDist();
	
	
	List<SimpleMatrix> means = new ArrayList<SimpleMatrix>();
	double[] weights = new double[5000];
	ArrayList<SimpleMatrix> cov1 = new ArrayList<SimpleMatrix>();

	double d1=0,d2=0,d3=0,d4=0;
	for (int i = 0; i < 5000; i++) {
	    if(i%3 == 0){
		d1 = StdRandom.gaussian(1, 1);
	    	d2 = StdRandom.gaussian(1, 1);
	    	d3 = StdRandom.gaussian(1, 1);
	    	d4 = StdRandom.gaussian(3, 1);	    	
	    }else if(i%2 == 0){
		d1 = StdRandom.gaussian(1, 1);
	    	d2 = StdRandom.gaussian(3, 1);
	    	d3 = StdRandom.gaussian(3, 1);
	    	d4 = StdRandom.gaussian(3, 1);
	    }else if(i%1 == 0){
		d1 = StdRandom.gaussian(3, 1);
	    	d2 = StdRandom.gaussian(3, 1);
	    	d3 = StdRandom.gaussian(1, 1);
	    	d4 = StdRandom.gaussian(1, 1);
	    }
	    double[][] mean = {{d1},{d2},{d3},{d4}};
	    means.add(new SimpleMatrix(mean));
	    double[][] c = { { 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0 },
		    { 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0 } };
	    cov1.add(new SimpleMatrix(c));
	    weights[i] = 1d;
	    
	}
	try {
	    dist.updateDistribution(means.toArray(new SimpleMatrix[0]), cov1.toArray(new SimpleMatrix[0]), weights);
	} catch (EmptyDistributionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	dataToFile(means);
	
	
	
	// define your data
	double[] x = new double[100];
	double[] y = new double[100];
	double coord = 0;
	for(int i=0; i<100; i++) {
	    coord += 0.1;
	    x[i] = coord;
	}
	coord = 0;
	for(int i=0; i<100; i++) {
	    coord += 0.1;
	    y[i] = coord;
	}
	double[][] z1 = new double[100][100];
	z1 = calculateY(x ,y, 3d, 3d, dist, means);
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
    
    private static double calculateY(double x, double y, double setX, double setY, SampleDist dist, List<SimpleMatrix> means){
	//ArrayList<SimpleMatrix> means = dist.getMeans();
	SimpleMatrix bandwidth = dist.getmBandwidthMatrix();
    	/*double[][] c = {{0.0823 ,  -0.0124 ,   0.0159 ,  -0.0067},
    	   {-0.0124 ,   0.0530 ,  -0.0027   ,-0.0033},
    	    {0.0159,   -0.0027   , 0.1030   , 0.0065},
    	   {-0.0067  , -0.0033  ,  0.0065  ,  0.0764}};
    	SimpleMatrix bandwidth = new SimpleMatrix(c);*/
	double[][] dxVector = {{setX},{setY},{x},{y}};
	SimpleMatrix xVector = new SimpleMatrix(dxVector);
	double d = 0d;
	double n = means.size();
	for(SimpleMatrix m : means) {
	    double tmp = (-0.5d)*xVector.minus(m).transpose().mult(bandwidth.invert()).mult(xVector.minus(m)).trace();
	    d += ( ( 1 / Math.sqrt(4*Math.PI*Math.PI*bandwidth.determinant()) ) * Math.exp(tmp) );
	}
	return d/n;
}

/*public static double calculateY(double x, double y, SampleDist dist, ArrayList<SimpleMatrix> means) {
	double z = cos(x * PI) * sin(y * PI);
	return z;
}*/

public static double[][] calculateY(double[] x, double[] y, double setX, double setY, SampleDist dist, List<SimpleMatrix> means) {
	double[][] z = new double[y.length][x.length];
	for (int i = 0; i < x.length; i++)
	    for (int j = 0; j < y.length; j++)
		z[j][i] = calculateY(x[i], y[j], setX, setY, dist, means);
	return z;
}

private static void dataToFile(List<SimpleMatrix> data) {
    PrintWriter pw = null;
    try
    {
      Writer fw = new FileWriter( "data1.txt" );
      Writer bw = new BufferedWriter( fw );
      pw = new PrintWriter( bw );
      pw.print("[");
      for(int i=0; i<data.get(0).numRows(); i++) {
          for (SimpleMatrix m : data)
              pw.print(m.get(i, 0)+" ");
          if(i<(data.get(0).numRows()-1))
              pw.print(";");
      }
      pw.print("]");
    }
      
    catch ( IOException e ) {
      System.err.println( "Error creating file!" );
    }
    finally {
      if ( pw != null )
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
	    data = data.substring(0, data.length()-1);
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
	for(int i=0; i<matrixRow1.length; i++){
	    double[][] d = {{Double.parseDouble(matrixRow1[i])},{Double.parseDouble(matrixRow2[i])}};
	    dataArray.add(new SimpleMatrix(d));
	}
	return dataArray;
	
}

}
