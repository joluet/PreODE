package de.tuhh.luethke.PrePos.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class CabDataPaser {

	public static LinkedList<Measurement> parse(String fileName){
		LinkedList<Measurement> measurements = new LinkedList<Measurement>();
		ArrayList<String[]> stringData = readFromFileToLineArray(fileName);
		for(String[] line : stringData) {
			Measurement m = new Measurement(Double.valueOf(line[0]), Double.valueOf(line[1]), Integer.valueOf(line[3]));
			measurements.add(m);
		}
		LinkedList<Measurement> measurementsReverseOrder = new LinkedList<Measurement>();
		for(int i=measurements.size()-1; i>0; i--) {
			measurementsReverseOrder.add(measurements.get(i));
		}
		return measurementsReverseOrder;
	}

	private static ArrayList<String[]> readFromFileToLineArray(String fileName) {
		BufferedReader br = null;
		ArrayList<String[]> lines = new ArrayList<String[]>();

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader(fileName));

			int i=0;
			while ((sCurrentLine = br.readLine()) != null) {
				i++;
				lines.add(sCurrentLine.split(" "));
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
		
		return lines;

	}
}
