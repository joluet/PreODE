package de.tuhh.luethke.PrePos.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.oKDE.utility.Projection.Projector;

public class CabDataParser {

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
	public static LinkedList<Measurement> parseExtended(String fileName){
		LinkedList<Measurement> measurements = new LinkedList<Measurement>();
		ArrayList<String[]> stringData = readFromFileToLineArray(fileName);
		
		for(int i=1; i<stringData.size(); i++) {
			String[] line = stringData.get(i);
			/*String[] prevLine = stringData.get(i-1);
			double[][] prevoiusPos = {{Double.valueOf(prevLine[0])},{Double.valueOf(prevLine[1])}};
			Measurement m1 = new Measurement(prevoiusPos[0][0], prevoiusPos[1][0], Integer.valueOf(prevLine[3]));*/
			double[][] currentPos = {{Double.valueOf(line[0])},{Double.valueOf(line[1])}};
			Measurement m = new Measurement(currentPos[0][0], currentPos[1][0], Integer.valueOf(line[2]), Integer.valueOf(line[3]));
			
			/*double timeDiff = m.timeDiffInSeconds(m1);
			double distance = m.distanceInMeters(m1);
			double speed = ( distance / timeDiff );*/
			
			// time of day
			java.util.Date time = new java.util.Date(m.getDate()*1000);
			Calendar c = Calendar.getInstance();
			c.setTime(time);
			int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
			m.setTimeOfDay(timeOfDay);
			
			// direction by arctan(dy/dx) and					
			// speed is calculated and set by preprocessor
			
			measurements.add(m);
		}
		LinkedList<Measurement> measurementsReverseOrder = new LinkedList<Measurement>();
		for(int i=measurements.size()-1; i>0; i--) {
			measurementsReverseOrder.add(measurements.get(i));
		}
		return measurementsReverseOrder;
	}
}
