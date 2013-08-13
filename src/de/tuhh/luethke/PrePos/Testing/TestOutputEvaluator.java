package de.tuhh.luethke.PrePos.Testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.print.attribute.standard.Compression;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataPaser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.utility.Compressor;
import de.tuhh.luethke.oKDE.utility.MatrixOps;

class SimpleMatrixComp implements Comparator<SimpleMatrix> {
	@Override
	public int compare(SimpleMatrix m, SimpleMatrix m1) {
		return Double.compare(m.get(2, 0), m1.get(2, 0));
	}
}

public class TestOutputEvaluator {

	public static void main(String[] args) {
		// calculate sigma
		LinkedList<Measurement> testData = CabDataPaser.parse("test_data/new_ofikco.txt");
		Preprocessor.processTestData(testData);
		//System.out.println(testData.size());
		List<SimpleMatrix> testDataVectors = PositionalTSTransformer.transformTSData1(testData, 3, 600, 30, 450);
		Preprocessor.projectData(testDataVectors);

		SimpleMatrix sigma = new SimpleMatrix(2,1);
		SimpleMatrix mu = new SimpleMatrix(2,1);
		double N = testDataVectors.size();
		// calc mu
		SimpleMatrix tmp = new SimpleMatrix(2,1);
		for(SimpleMatrix vector: testDataVectors) {
			tmp.set(0,0,vector.get(vector.numRows()-2,0));
			tmp.set(1,0,vector.get(vector.numRows()-1,0));
			mu = mu.plus(tmp.scale(1/N));
		}
		// calc sigma
		double sigma1 = 0;
		for(SimpleMatrix vector: testDataVectors) {
			tmp.set(0,0,vector.get(vector.numRows()-2,0));
			tmp.set(1,0,vector.get(vector.numRows()-1,0));
			sigma = sigma.plus(MatrixOps.elemPow(tmp.minus(mu),2).scale(1/N));
			sigma1 += Compressor.euclidianDistance(tmp,mu)*(1/N);
		}
		double sigmaLen = Math.sqrt((sigma.get(0,0)*sigma.get(0,0)) + (sigma.get(1,0)*sigma.get(1,0)));
		//euclidian
		// calc mu
		/*tmp = new SimpleMatrix(2,1);
		double mu1 = 0;
		for(SimpleMatrix vector: testDataVectors) {
			tmp.set(0,0,vector.get(vector.numRows()-2,0));
			tmp.set(1,0,vector.get(vector.numRows()-1,0));
			LatLng point1 = new LatLng(tmp.get(0,0), tmp.get(0,1));
			LatLng point2 = new LatLng(mu.get(0,0), mu.get(0,1));
			double distance = LatLngTool.distance(point1, point2, LengthUnit.METER);
			mu1 += (1/N) * distance;
		}
		// calc sigma
		double sigma1 = 0;
		for(SimpleMatrix vector: testDataVectors) {
			tmp.set(0,0,vector.get(vector.numRows()-2,0));
			tmp.set(1,0,vector.get(vector.numRows()-1,0));
			LatLng point1 = new LatLng(tmp.get(0,0), tmp.get(0,1));
			LatLng point2 = new LatLng(mu.get(0,0), mu.get(0,1));
			double distance = LatLngTool.distance(point1, point2, LengthUnit.METER);
			sigma1 += distance * distance * (1/N);
		}*/
		
		double avg02 = 0;
		ArrayList<SimpleMatrix> bestResults = new ArrayList<SimpleMatrix>();
		for(int i=1; i<2; i++){
			for(int j=0; j<1; j++) {
				ArrayList<SimpleMatrix> results = readFromFile("test_data/significance/new"	);
				ArrayList<SimpleMatrix> worstResults = new ArrayList<SimpleMatrix>();
		
				int count = 0;
				int count1 = 0;
				int countSm1000 = 0;
				double mse1 = 0;
				SimpleMatrix mse = new SimpleMatrix(2,1);
				SimpleMatrix pre = new SimpleMatrix(2,1);
				SimpleMatrix act = new SimpleMatrix(2,1);
				int k=0, l=0;
				ArrayList<Double> RelMESList = new ArrayList<Double>();
				for(SimpleMatrix m : results) {
					double relError = m.get(2,0);
					if(m.get(5,0)>0.0 && m.get(3,0)>=0.01){
						k++;
						pre.set(0,0,m.get(6,0));
						pre.set(1,0,m.get(7,0));
						act.set(0,0,m.get(0,0));
						act.set(1,0,m.get(1,0));
						pre = Preprocessor.projectData(pre);
						act = Preprocessor.projectData(act);
	
						mse = mse.plus( MatrixOps.elemPow(pre.minus(act),2) );
						double addToMSE = Compressor.euclidianDistance(pre,act);
						
						mse1 += (1/(double)results.size())*addToMSE;
						
						double norm = Compressor.euclidianDistance(act,mu);
						RelMESList.add((addToMSE/norm));
						if((addToMSE/norm)>0.3){
							System.out.println((addToMSE/norm)+">0.3: "+m.get(5,0));
							l++;
						}
						double dis = Math.sqrt(Compressor.euclidianDistance(pre,act));
						if(Compressor.euclidianDistance(pre,act) < 500){
							countSm1000++;
							System.out.println("<500: "+m.get(4,0));
						}
						
					}
					
					if(relError< (0.1*(double)j) && m.get(5,0)>0.0 /*&& m.get(3,0)>=0.01*/){
						//System.out.println(m.get(0,0)+","+m.get(1,0));
						count++;
						count1++;
						if(j==3)
							bestResults.add(m);
					}else {
						if(m.get(5,0)>0.0/* && m.get(3,0)>=0.01*/){
							//System.out.println("error: "+m.get(2,0)+", relError: "+relError+" "+m.get(3,0));
							count1++;
							worstResults.add(m);
						}
					}
				}
				mse = mse.scale(1/(double)results.size());
				
				double mseLen = Math.sqrt((mse.get(0,0)*mse.get(0,0)) + (mse.get(1,0)*mse.get(1,0)));
				System.out.println("filtered by marginal: "+k+"/"+results.size());
				System.out.println("bad of filtered: "+l+"/"+k);

				System.out.println("mse by len: "+(mseLen/sigmaLen));
				System.out.println("mse1 :"+(mse1/sigma1));
				Double[] MSEMedian = RelMESList.toArray(new Double[0]);
				Arrays.sort(MSEMedian);
				System.out.println("mse1 median: " + MSEMedian[MSEMedian.length/2]);
				System.out.println("smaller 500: "+countSm1000+"/"+count1);
				
				CommonOps.elementDiv(mse.getMatrix(),sigma.getMatrix());
				
				SimpleMatrix[] worstArray = worstResults.toArray(new SimpleMatrix[1]);
				System.out.println("MSE: "+mse);
				Arrays.sort(worstArray, new SimpleMatrixComp());
				String percent = String.valueOf(((double)count/count1));
				if(percent.length() > 5 )
					percent = percent.substring(0,5);
				System.out.println(String.valueOf((0.1*(double)j)).substring(0,3)+" : "+count+"/"+count1+" = "+percent+"%");
				if(j==3)
					avg02 += ((double)count/count1);
			}
			System.out.println("---------------------------------------------------------");
		}
		avg02 /= 12;
		System.out.println(avg02);
		measurementsToHeatMapFile(bestResults);

	}

	

	private static ArrayList<SimpleMatrix> readFromFile(String filename) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String data = "";
			String sCurrentLine;
			
			br = new BufferedReader(new FileReader(filename));
			while(!data.equals("Start prediction...")){
				data = br.readLine();
			}
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = br.readLine();
				sCurrentLine = br.readLine();
				SimpleMatrix m = new SimpleMatrix(8, 1);
				String[] posData = sCurrentLine.split(" | ");
				m.set(6, 0, Double.valueOf(posData[9]));
				m.set(7, 0, Double.valueOf(posData[10]));
				m.set(0, 0, Double.valueOf(posData[6]));
				m.set(1, 0, Double.valueOf(posData[7]));
				br.readLine();
				sCurrentLine = br.readLine();
				String prediction = sCurrentLine;
				posData = prediction.split(" ");
				
				m.set(2, 0, Double.valueOf(posData[0]));
				m.set(3, 0, Double.valueOf(posData[1]));
				m.set(4, 0, Double.valueOf(posData[2]));
				m.set(5, 0, Double.valueOf(posData[3]));
				/*m.set(4, 0, Double.valueOf(posData[4]));
				m.set(5, 0, Double.valueOf(posData[5]));
				m.set(6, 0, Double.valueOf(posData[6]));*/
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

	private static void measurementsToHeatMapFile(List<SimpleMatrix> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("highAccuracyResults.txt");
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
}
