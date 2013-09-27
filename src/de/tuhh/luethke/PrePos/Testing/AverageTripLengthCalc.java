package de.tuhh.luethke.PrePos.Testing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;

public class AverageTripLengthCalc {

	public static void main(String[] args) {
		final File folder = new File(args[0]);
		ArrayList<String> fileNames = listFilesForFolder(folder);
		String[] filesSorted = fileNames.toArray(new String[1]);
		Arrays.sort(filesSorted);
		double overallAvgTripTime = 0;
		for (String file : filesSorted) {
			LinkedList<Measurement> trainingData = CabDataParser.parseExtended(folder + "/" + file);
			Preprocessor.processTestData(trainingData, 2, -1);
			Measurement tmp = trainingData.poll();
			double sumTimeLags = 0;
			double sumDistances = 0;
			ArrayList<Double> times = new ArrayList<Double>();
			double count = 0;
			for(int i=0; i<(trainingData.size()-3); i++) {
			//for (Iterator<Measurement> i = trainingData.iterator(); i.hasNext();) {
				//Measurement m = (Measurement) i.next();
				Measurement m = trainingData.get(i);
				Measurement m1 = trainingData.get(i+1);
				Measurement m2 = trainingData.get(i+2);
				if (tmp.getFare() == 1 && tmp.getSpeed()<=2) {
					while (i<(trainingData.size()-3) && ( m.getFare()==1 /*|| (m1.getFare() ==1&&m.timeDiffInSeconds(m2)<120)*/ || m.getSpeed()>2) ) {
						m = (Measurement) trainingData.get(i+1);
						m1 = trainingData.get(i+2);
						m2 = trainingData.get(i+3);
						i++;
					}
					double timeLag = tmp.timeDiffInSeconds(m);
					double dist = tmp.distanceInMeters(m);
					times.add(timeLag);
					/*if(timeLag > 2000)
						System.out.println("mehr als 33min");*/
					sumTimeLags += timeLag;
					sumDistances += dist;
					count++;
				}
				tmp = m;
			}
			overallAvgTripTime += (sumTimeLags / count);
			Double[] timesSorted = times.toArray(new Double[0]);
			Arrays.sort(timesSorted);
			System.out.println(file);
			System.out.println("Average trip time: " + (sumTimeLags / count));
			System.out.println("Median trip time: " + timesSorted[timesSorted.length/2]);

			//System.out.println("Average distance: " + (sumDistances / count));

			double mean = (sumTimeLags / count);
			double meanDist = (sumDistances / count);
			double variance = 0;
			double varDist = 0;
			for (Iterator<Measurement> i = trainingData.iterator(); i.hasNext();) {
				Measurement m = (Measurement) i.next();
				if (tmp.getFare() == 1 && tmp.getFare() == m.getFare()) {
					while (tmp.getFare() == 1 && tmp.getFare() == m.getFare() && i.hasNext()) {
						m = (Measurement) i.next();
					}
					double timeLag = tmp.timeDiffInSeconds(m);
					variance += ((timeLag-mean)*(timeLag-mean));
					double dist = tmp.distanceInMeters(m);
					varDist += ((dist-meanDist)*(dist-meanDist));

					count++;
				}
				tmp = m;
			}
			variance /= count;
			varDist /= count;

			System.out.println(Math.sqrt(variance)+"\n");
			//System.out.println(Math.sqrt(varDist));
		}
		System.out.println("overall avg trip time: "+overallAvgTripTime/((double) filesSorted.length));
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
}
