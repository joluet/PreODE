package de.tuhh.luethke.PrePos.Testing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;

/**
 * This class extracts location batches from a location history file. Using the given embedding parameters
 * 
 * @author Jonas Luethke
 *
 */
public class Extractor implements  Callable<Boolean>{

	private String filename="";
	private String targetFolder="";
	private int steps = 0;
	private int stepsize = 0;
	private int tolerance = 0;
	private int numberOfSamples = 0;
	private double minTravelDistance=0;
	private int useOneStepExtension = 0;
	
	public Extractor(String filename, String targetFolder,int steps, int stepsize, int tolerance, int numberOfSamples, double minTravelDistance, int useOneStepExtension){
		this.filename = filename;
		this.targetFolder = targetFolder;
		this.steps = steps;
		this.stepsize = stepsize;
		this.tolerance = tolerance;
		this.numberOfSamples = numberOfSamples;
		this.minTravelDistance = minTravelDistance;
		this.useOneStepExtension = useOneStepExtension;
	}
	
	private static void coordinatesToFile(List<SimpleMatrix> data, String file) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(file);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (SimpleMatrix m : data){
				String s = "";
				for(int i=0; i<m.numRows(); i++){
					s += " " + m.get(i,0);
				}
				pw.println(s);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
		if (pw != null)
			pw.close();
	}
	
	private static void coordinatesToFile(LinkedList<Measurement> data, String file) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(file);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (Measurement m : data){
				String s = m.getLat()+" "+m.getLng();
				pw.println(s);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
		if (pw != null)
			pw.close();
	}

	@Override
	public Boolean call() throws Exception {
		LinkedList<Measurement> trainingData = CabDataParser.parseExtended(filename);
		System.out.println(filename);
		Preprocessor.processTestData(trainingData, 2, -1);

		List<SimpleMatrix> posVectors;
		if(useOneStepExtension == 0) 
			posVectors = PositionalTSTransformer.transformTSData1(trainingData, steps, stepsize, tolerance, numberOfSamples, minTravelDistance);
		else
			posVectors = PositionalTSTransformer.transformTSDataFirstOrder(trainingData, stepsize, tolerance, numberOfSamples, minTravelDistance);

		if(posVectors.size()>=numberOfSamples){
			String substring = filename.substring(filename.lastIndexOf('/'));
			coordinatesToFile(posVectors, targetFolder+substring+".ext");
			return true;
		}
		return false;
	}

}
