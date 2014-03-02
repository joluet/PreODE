/*
 * Copyright (c) 2014 Jonas Luethke
 */

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

import de.tuhh.luethke.PrePos.Transformation.DelayEmbedder;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;

/**
 * This class extracts location batches from a location history file using the given embedding
 * parameters.
 * 
 * @author Jonas Luethke
 * 
 */
public class Extractor implements Callable<Boolean> {

	private String filename = "";
	private String targetFolder = "";
	private int steps = 0;
	private int stepsize = 0;
	private int tolerance = 0;
	private int numberOfSamples = 0;
	private double minTravelDistance = 0;

	public Extractor(String filename, String targetFolder, int steps, int stepsize, int tolerance, int numberOfSamples,
			double minTravelDistance) {
		this.filename = filename;
		this.targetFolder = targetFolder;
		this.steps = steps;
		this.stepsize = stepsize;
		this.tolerance = tolerance;
		this.numberOfSamples = numberOfSamples;
		this.minTravelDistance = minTravelDistance;
	}

	private static void coordinatesToFile(List<SimpleMatrix> data, String file) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(file);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (SimpleMatrix m : data) {
				String s = "";
				for (int i = 0; i < m.numRows(); i++) {
					s += " " + m.get(i, 0);
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

	@Override
	public Boolean call() throws Exception {
		LinkedList<Measurement> trainingData = CabDataParser.parseExtended(filename);
		System.out.println(filename);
		Preprocessor.processTestData(trainingData);

		List<SimpleMatrix> posVectors;

		posVectors = DelayEmbedder.embed(trainingData, steps, stepsize, tolerance, numberOfSamples, minTravelDistance);

		if (posVectors.size() >= numberOfSamples) {
			String substring = filename.substring(filename.lastIndexOf('/'));
			coordinatesToFile(posVectors, targetFolder + substring + ".ext");
			return true;
		}
		return false;
	}

}
