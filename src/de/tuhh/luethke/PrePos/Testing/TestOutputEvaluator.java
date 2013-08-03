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
import java.util.List;

import org.ejml.simple.SimpleMatrix;

class SimpleMatrixComp implements Comparator<SimpleMatrix> {
	@Override
	public int compare(SimpleMatrix m, SimpleMatrix m1) {
		return Double.compare(m.get(2, 0), m1.get(2, 0));
	}
}

public class TestOutputEvaluator {

	public static void main(String[] args) {
		for(int i=1; i<9; i++){
			for(int j=0; j<10; j++) {
				ArrayList<SimpleMatrix> results = readFromFile("test_data/2000_300/30minstep/e/"+i);
				ArrayList<SimpleMatrix> bestResults = new ArrayList<SimpleMatrix>();
				ArrayList<SimpleMatrix> worstResults = new ArrayList<SimpleMatrix>();
		
				int count = 0;
				int count1 = 0;
				for(SimpleMatrix m : results) {
					double relError = m.get(2,0)/m.get(4,0);
					
					if(relError< (0.1*(double)j) && m.get(6,0)>=0.4){
						//System.out.println(m.get(0,0)+","+m.get(1,0));
						count++;
						count1++;
						bestResults.add(m);
					}else {
						if(m.get(6,0)>=0.4){
							//System.out.println("error: "+m.get(2,0)+", relError: "+relError+" "+m.get(3,0));
							count1++;
							worstResults.add(m);
						}
					}
				}
				SimpleMatrix[] worstArray = worstResults.toArray(new SimpleMatrix[1]);
				Arrays.sort(worstArray, new SimpleMatrixComp());
				String percent = String.valueOf(((double)count/count1));
				if(percent.length() > 5 )
					percent = percent.substring(0,5);
				System.out.println(String.valueOf((0.1*(double)j)).substring(0,3)+" : "+count+"/"+count1+" = "+percent+"%");
				measurementsToHeatMapFile(bestResults);
			}
			System.out.println("---------------------------------------------------------");
		}

	}

	

	private static ArrayList<SimpleMatrix> readFromFile(String filename) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null) {
				String prediction = sCurrentLine;
				if (prediction.startsWith("avg"))
					break;
				String[] posData = prediction.split(" ");
				SimpleMatrix m = new SimpleMatrix(7, 1);
				m.set(0, 0, Double.valueOf(posData[0]));
				m.set(1, 0, Double.valueOf(posData[1]));
				m.set(2, 0, Double.valueOf(posData[2]));
				m.set(3, 0, Double.valueOf(posData[3]));
				m.set(4, 0, Double.valueOf(posData[4]));
				m.set(5, 0, Double.valueOf(posData[5]));
				m.set(6, 0, Double.valueOf(posData[6]));
				dataArray.add(m);
				br.readLine();
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
