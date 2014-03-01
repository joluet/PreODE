/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Testing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Extract {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int noOfThreads = Integer.parseInt(args[0]);
		final File folder = new File(args[1]);
		ArrayList<String> fileNames = listFilesForFolder(folder);
		String targetFolder = args[2];
		int steps = Integer.parseInt(args[3]);
		int stepsize = Integer.parseInt(args[4]);
		int tolerance = Integer.parseInt(args[5]);
		int numberOfSamples = Integer.parseInt(args[6]);
		int numberOfFilesNeeded = Integer.parseInt(args[7]);
		double minTravelDistance = Double.parseDouble(args[8]);
		int useOneStepExtension = 0;
		if(args.length > 9)
			useOneStepExtension = Integer.parseInt(args[9]);


		
		
		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
		String[] filesSorted = fileNames.toArray(new String[1]);
		Arrays.sort(filesSorted);
		ArrayList<Future<Boolean>> futureResults = new ArrayList<Future<Boolean>>();
		for(String file : filesSorted){
			Callable<Boolean> worker = new Extractor(folder.getAbsolutePath()+"/"+file, targetFolder, steps, stepsize, tolerance, numberOfSamples, minTravelDistance, useOneStepExtension);
			futureResults.add(executor.submit(worker));
		}
		int count = 0;
		for (Future<Boolean> f : futureResults) {
			try {
				Boolean b = null;
				if(b=f.get()) {
					count++;
					if(count >= numberOfFilesNeeded){
						executor.shutdownNow();
						executor.awaitTermination(10, TimeUnit.SECONDS);
						return;
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
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
