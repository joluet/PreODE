package de.tuhh.luethke.PrePos.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

public class PLTParser {
	private static final int META_INFO_LEN = 6;

	public static LinkedList<Measurement> parse(String folderName){
		final File folder = new File(folderName);
		ArrayList<String> fileNames = listFilesForFolder(folder);
		LinkedList<Measurement> measurements = new LinkedList<Measurement>();
		for (int i = 0; i < fileNames.size(); i++) {
			ArrayList<String[]> stringData = readFromFileToLineArray(folderName+fileNames.get(i));
			for(String[] line : stringData) {
				SimpleDateFormat sdfToDate = new SimpleDateFormat(
	                    "yyy-MM-dd-HH:mm:ss");
				sdfToDate.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date date1 = null;
	            try {
					date1 = sdfToDate.parse(line[5]+"-"+line[6]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Measurement m = new Measurement(Double.valueOf(line[0]), Double.valueOf(line[1]), (date1.getTime()/1000));
				measurements.add(m);
			}
			/*LinkedList<Measurement> measurementsReverseOrder = new LinkedList<Measurement>();
			for(int j=measurements.size()-1; j>0; j--) {
				measurementsReverseOrder.add(measurements.get(j));
			}*/
		}
		return measurements;
	}

	public static ArrayList<String> listFilesForFolder(final File folder) {
		ArrayList<String> fileNames = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				fileNames.add(fileEntry.getName());
			}
		}
		return fileNames;
	}

	private static ArrayList<String[]> readFromFileToLineArray(String fileName) {
		BufferedReader br = null;
		ArrayList<String[]> lines = new ArrayList<String[]>();

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader(fileName));
			int i = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				i++;
				// skip meta info offset
				if (i > META_INFO_LEN)
					lines.add(sCurrentLine.split(","));
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
