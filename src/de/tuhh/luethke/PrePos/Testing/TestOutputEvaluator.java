/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

import org.ejml.data.Matrix64F;
import org.ejml.ops.MatrixComponent;
import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.DelayEmbedder;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.okde.utility.Compression.Compressor;
import de.tuhh.luethke.okde.utility.Matrices.MatrixOps;

class SimpleMatrixComp implements Comparator<SimpleMatrix> {
	@Override
	public int compare(SimpleMatrix m, SimpleMatrix m1) {
		return Double.compare(m.get(2, 0), m1.get(2, 0));
	}
}

/**
 * This class is used to evaluate the test results provided by the prediction algorithm.
 * 
 * @author Jonas Luethke
 *
 */
public class TestOutputEvaluator {

	private static final String ABS_ERROR_FOLDER = "APE/";
	private static final String ABS_ERROR_MEAN_FOLDER = "APE_M/";
	private static final String SPATIAL_ERROR_FOLDER = "SPATIAL_ERROR_DIST/";
	
	private static final String RELATIVE_ERROR_FOLDER = "RELATIVE_ERROR/";

			
	private static final int SAMPLING = 1;

	
	public static void main(String[] args) {
		String folderName = args[0];
		int order = Integer.parseInt(args[1]);
		double reliabilityRadius = Double.parseDouble(args[2]);

		
		ArrayList<Double> predictionReliabilities = new ArrayList<Double>();
		ArrayList<Double> relativeErrorOfRight = new ArrayList<Double>();

		ArrayList<SimpleMatrix> wrongResults = new ArrayList<SimpleMatrix>();
		ArrayList<SimpleMatrix> rightResults = new ArrayList<SimpleMatrix>();
		ArrayList<SimpleMatrix> referencePositions = new ArrayList<SimpleMatrix>();



		final File folder = new File(folderName);
		ArrayList<String> fileNames = listFilesForFolder(folder);
		
		
		List<Double> overallAbsErrors = new ArrayList<Double>();
		List<Double> overallAbsErrorsUsingMean = new ArrayList<Double>();
		
		double overallNoOfPredictions = 0;
		double overallNoOfRightPredictions = 0;
		double overallClaimedNoOfRightPredictions = 0;
		

		
		for (int i = 0; i < fileNames.size(); i++) {
			String cabFileName = readInfoFromFile(folderName + fileNames.get(i));
			System.out.println(cabFileName);
			ArrayList<TestResult> results = readFromFile(folderName + fileNames.get(i), order);
			String trainingDataFileName = fileNames.get(i);
			String trainingDataFullPath = readTrainingDataFileName(folderName + fileNames.get(i));


			ArrayList<Double> relativeErrorDistribution =new ArrayList<Double>();
			for (int j = 0; j < 10; j++) {
				relativeErrorDistribution.add(0d);
			}
			String trainingFileName = trainingDataFullPath.substring(trainingDataFullPath.indexOf('/'));
			trainingDataFullPath = "/home/jonas/Dokumente/Masterarbeit/Testing/testinput"+trainingFileName;
			ArrayList<SimpleMatrix> trainingData = readTrainingData(trainingDataFullPath);
			SimpleMatrix mean = mean(trainingData,0,3000);
			mean = Preprocessor.projectData(mean);
			SimpleMatrix var = variance(trainingData,mean,0,3000);
			System.out.println("var: "+var);
			System.out.println("|var|: "+Math.sqrt(var.elementSum()));
			
			SimpleMatrix mean_tst = mean(trainingData,3000,3500);
			mean_tst = Preprocessor.projectData(mean_tst);
			SimpleMatrix var_tst = variance(trainingData,mean_tst,3000,3500);
			System.out.println("var of test set: "+var_tst);
			System.out.println("|var_tst|: "+Math.sqrt(var_tst.elementSum()));



			//metrics
			double noOfRightPredictions = 0;
			double noOfRightPredUsingMean = 0;
			List<Double> absErrors = new ArrayList<Double>();
			List<Double> absErrorsUsingMean = new ArrayList<Double>();
			List<SimpleMatrix> spatialErrorDist = new ArrayList<SimpleMatrix>();

			double avgLastDistance = 0;

			
			
			for (TestResult result : results) {
				double relError = result.relativeError;
				SimpleMatrix pre = new SimpleMatrix(result.prediction);
				SimpleMatrix ref = new SimpleMatrix(result.referencePosition);
				SimpleMatrix refPlusError = new SimpleMatrix(3,1);
				refPlusError.set(0,0,ref.get(0,0));
				refPlusError.set(1,0,ref.get(1,0));
				refPlusError.set(2,0,result.error);
				spatialErrorDist.add(refPlusError);
				pre = Preprocessor.projectData(pre);
				ref = Preprocessor.projectData(ref);
				avgLastDistance += result.lastDistance;
				if (result.reliability > 0) {
					if(!Double.isInfinite(relError) && relError<6)
						relativeErrorOfRight.add(relError);
					if (result.error <= reliabilityRadius) {
						noOfRightPredictions++;
						rightResults.add(result.prediction);
					}else
						wrongResults.add(result.prediction);
					/*if(Compressor.euclidianDistance(ref,mean)<=reliabilityRadius){
						noOfRightPredUsingMean++;
					}*/
					predictionReliabilities.add(result.reliability );
					absErrors.add(result.error);
					absErrorsUsingMean.add(Compressor.euclidianDistance(ref,mean));
				}
				referencePositions.add(new SimpleMatrix(ref));
				overallNoOfPredictions++;
				overallClaimedNoOfRightPredictions += result.reliability;
			}
			avgLastDistance /= ((double)results.size());
			overallNoOfRightPredictions += noOfRightPredictions;
			// print error distributions of each cab to seperate files:
			cumulativeErrorDistToFile(absErrors, SAMPLING, folderName+ABS_ERROR_FOLDER+trainingDataFileName);
			cumulativeErrorDistToFile(absErrorsUsingMean, SAMPLING, folderName+ABS_ERROR_MEAN_FOLDER+trainingDataFileName);
			spatialerrorDistToFile(spatialErrorDist, folderName+SPATIAL_ERROR_FOLDER+trainingDataFileName, 5000);
			coordinatesToFile(rightResults,"rightResults.txt");

			overallAbsErrors.addAll(absErrors);
			overallAbsErrorsUsingMean.addAll(absErrorsUsingMean);

			
			
			/*System.out.println("below X: " + countSm1000);
			if(count1 > 0)
				avgBetterThan1000 += ((double)countSm1000)/((double)count1)*(1/((double)fileNames.size()));
			System.out.println(((double)countSm1000)/((double)count1));
			System.out.println(((double)meanRight)/((double)count1));*/
			
			System.out.println(noOfRightPredictions+"/"+results.size());
			System.out.println(noOfRightPredUsingMean+"/"+results.size());
			System.out.println("avg last distance: "+avgLastDistance);

			System.out.println("---------------------------------------------------------");
		}

		// print overall error distributions of all cabs to files:
		cumulativeErrorDistToFile(overallAbsErrors, SAMPLING, folderName+ABS_ERROR_FOLDER+"overallAbsError");
		cumulativeErrorDistToFile(overallAbsErrorsUsingMean, SAMPLING, folderName+ABS_ERROR_MEAN_FOLDER+"overallAbsErrorUsingMean");
		
		double avgRightPredictions = overallNoOfRightPredictions/overallNoOfPredictions; 
		System.out.println("Average overall rigth prediction portion: "+avgRightPredictions);
		overallClaimedNoOfRightPredictions  = overallClaimedNoOfRightPredictions/overallNoOfPredictions; 
		System.out.println("Average overall claimed rigth prediction portion: "+overallClaimedNoOfRightPredictions);

		double avgRelErrorOfRight = 0;
		for(int i=0; i<relativeErrorOfRight.size(); i++) {
			double relError = relativeErrorOfRight.get(i);
			if(relError > 4){
				relativeErrorOfRight.set(i,0d);
				System.out.println(relError);
			}
			avgRelErrorOfRight += relError*relError;
		}
		avgRelErrorOfRight /= ((double)relativeErrorOfRight.size());
		System.out.println("AVG Relative error of right: "+Math.sqrt(avgRelErrorOfRight));
		Double[] sortedRelativeErrorOfRight = relativeErrorOfRight.toArray(new Double[0]);
		Arrays.sort(sortedRelativeErrorOfRight);
		errorsToFile(sortedRelativeErrorOfRight, folderName+RELATIVE_ERROR_FOLDER+"sortedRelativeErrorOfRight.txt");
		System.out.println("Median Relative error of right: "+sortedRelativeErrorOfRight[sortedRelativeErrorOfRight.length/2]);

		double mse = 0;
		double mean = 0;
		for(Double error: overallAbsErrors) {
			mse += error*error;
			mean += error;
		}
		mse /= ((double)overallAbsErrors.size());
		mean /= ((double)overallAbsErrors.size());
		System.out.println("RMSE: "+Math.sqrt(mse));
		Double[] sortedAbsErrors = overallAbsErrors.toArray(new Double[0]);
		Arrays.sort(sortedAbsErrors);
		System.out.println("Median of absolute error: "+sortedAbsErrors[sortedAbsErrors.length/2]);
		System.out.println("Mean of absolute error: "+mean);

		double mseMean = 0;
		double meanMean = 0;
		for(Double error: overallAbsErrorsUsingMean) {
			mseMean += error*error;
			meanMean += error;
		}
		mseMean /= ((double)overallAbsErrorsUsingMean.size());
		meanMean /= ((double)overallAbsErrorsUsingMean.size());
		System.out.println("(Mean-Predictor) RMSE: "+Math.sqrt(mseMean));
		Double[] sortedAbsErrorsUsingMean = overallAbsErrorsUsingMean.toArray(new Double[0]);
		Arrays.sort(sortedAbsErrorsUsingMean);
		System.out.println("(Mean-Predictor) Median of absolute error: "+sortedAbsErrorsUsingMean[sortedAbsErrorsUsingMean.length/2]);
		System.out.println("(Mean-Predictor) Mean of absolute error: "+meanMean);
		
		
		//System.out.println("avg better using mean: "+(meanRight/k));
		double meanError = 0;
		double meanErrorOfRight = 0;
		double errorOfRightCount = 0;
		double meanSquaredErrorOfRight = 0;
		double meanSquaredError = 0;
		double errorCount = 0;
		ArrayList<Double> realErrorList = new ArrayList<Double>();
/*		for(double error : errors) {
			meanError += error;
			if(error > 1000) {
				meanSquaredError += ((error-1000)*(error-1000));
				errorCount++;
				meanError += (error-1000);
				realErrorList.add(error - 1000d);
			}else {
				meanErrorOfRight += error;
				meanSquaredErrorOfRight += (error*error);
				errorOfRightCount++;
			}
				
		}
		meanErrorOfRight /= errorOfRightCount;
		System.out.println("good predictions error mean: "+meanErrorOfRight);
		Double[] realErrorArray = realErrorList.toArray(new Double[0]);
		Arrays.sort(realErrorArray);
		double errorMedian = realErrorArray[realErrorArray.length/2];
		System.out.println("error median: "+errorMedian);
		meanError /= errorCount;
		meanSquaredError /= errorCount;
		System.out.println("Error mean "+meanError);
		double errorVariance = 0;
		double errorVarianceOfRight = 0;
		
		// error distribution
		Double[] errorsSorted = errors.toArray(new Double[0]);
		Arrays.sort(errorsSorted);
		double[] errorsSortedSubSet = new double[(errorsSorted.length/1)+1];
		for(int i=0; i<errorsSorted.length; i++){
			if(i%1 == 0)
				errorsSortedSubSet[i/1]=errorsSorted[i]; 
		}
		errorsToFile(errorsSortedSubSet, "errors_pred.txt");
		
		// prediction reliability
		Double[] sortedPredictionReliabilities = predictionReliabilities.toArray(new Double[0]);
		Arrays.sort(sortedPredictionReliabilities);
		double[] sortedPredictionReliabilitiesSubSet = new double[(sortedPredictionReliabilities.length/5)+1];
		for(int i=0; i<sortedPredictionReliabilities.length; i++){
			if(i%5 == 0)
				sortedPredictionReliabilitiesSubSet[i/5]=sortedPredictionReliabilities[i]; 
		}
		errorsToFile(errorsSortedSubSet, "predictionReliabilities.txt");

		//relative error of right
		Double[] sortedRelativeErrorOfRight = relativeErrorOfRight.toArray(new Double[0]);
		Arrays.sort(sortedRelativeErrorOfRight);
		errorsToFile(sortedRelativeErrorOfRight, "sortedRelativeErrorOfRight.txt");
		
		for(double error : errors) {
			if(error > 1000) {
				error = error - 1000;
				errorVariance += ((error - meanError)*(error - meanError));
			}else{
				errorVarianceOfRight += ((error - meanError)*(error - meanError));
			}
		}
		errorVariance /= ((double)errors.size());
		System.out.println("predictability: "+(meanSquaredError/errorVariance));
		System.out.println("predictability 1: "+(meanSquaredErrorOfRight/errorVarianceOfRight));

		errorVariance = Math.sqrt(errorVariance);
		System.out.println("Error variance: "+errorVariance);
		coordinatesToFile(referencePositions,"referencePositions.txt");
		coordinatesToFile(wrongResults,"wrongResults.txt");
		coordinatesToFile(rightResults,"rightResults.txt");
*/	}
	
	private static List<Double> getErrorsSmaller(List<TestResult> results, double threshold){
		List<Double> errors = new ArrayList<Double>();
		for (TestResult result : results) {
			double relError = result.relativeError;
			SimpleMatrix pre = result.prediction;
			SimpleMatrix ref = result.referencePosition;
			pre = Preprocessor.projectData(pre);
			ref = Preprocessor.projectData(ref);
			if (result.reliability > 0) {
				if (result.error <= threshold) {
					errors.add(result.error);
				}
			}
		}
		return errors;
	}
	
	
	public static SimpleMatrix mean(List<SimpleMatrix> data, int start, int stop){
		SimpleMatrix mean = new SimpleMatrix(2,1);
		SimpleMatrix norm = new SimpleMatrix(2,1);
		double count=0;
		for (int i=start; i<stop; i++) {
			SimpleMatrix m = data.get(i);
			SimpleMatrix tmp = new SimpleMatrix(2,1);
			for(int j=0; j<m.numRows()/2; j++){
				tmp.set(0, 0, m.get(j*2, 0));
				tmp.set(1, 0, m.get(j*2+1, 0));
				mean = mean.plus(tmp);
				count++;
			}
		}
		norm.set(0,0,1d/count);
		norm.set(1,0,1d/count);
		mean = mean.elementMult(norm);
		return mean;
	}
	
	public static SimpleMatrix variance(List<SimpleMatrix> data, SimpleMatrix mean, int start, int stop){
		SimpleMatrix var = new SimpleMatrix(2,1);
		SimpleMatrix norm = new SimpleMatrix(2,1);
		double count=0;
		for (int i=start; i<stop; i++) {
			SimpleMatrix m = data.get(i);
			SimpleMatrix tmp = new SimpleMatrix(2,1);
			tmp.set(0, 0, m.get(0, 0));
			tmp.set(1, 0, m.get(1, 0));
			tmp = Preprocessor.projectData(tmp);
			var = var.plus(MatrixOps.elemPow(tmp.minus(mean),2));
			count++;
		}
		norm.set(0,0,1d/count);
		norm.set(1,0,1d/count);
		var = var.elementMult(norm);
		return var;
	}
	

	public static ArrayList<String> listFilesForFolder(final File folder) {
		ArrayList<String> fileNames = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				fileNames.add(fileEntry.getName());
				System.out.println(fileEntry.getName());
			}
		}
		return fileNames;
	}
	
	private static String readInfoFromFile(String filename) {
		BufferedReader br = null;
		String info = "";
		
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			info = br.readLine();
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
		return info;
	}
	private static ArrayList<TestResult> readFromFile(String filename, int order) {
		BufferedReader br = null;
		ArrayList<TestResult> resultArray = new ArrayList<TestResult>();

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			while (!data.equals("Start prediction...")) {
				data = br.readLine();
			}
			int k = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				while (sCurrentLine.startsWith("Loop") || sCurrentLine.length() == 0)
					sCurrentLine = br.readLine();
				if (sCurrentLine.startsWith("Time"))
					break;
				while (sCurrentLine != null && !sCurrentLine.contains("|"))
					sCurrentLine = br.readLine();
				if(sCurrentLine == null)
					break;
				String[] posData = sCurrentLine.split(" | ");
				SimpleMatrix predictedPosition = new SimpleMatrix(2,1);
				SimpleMatrix referencePosition = new SimpleMatrix(2,1);
				if(order == 2){
					referencePosition.set(0,0,Double.valueOf(posData[3]));
					referencePosition.set(1,0,Double.valueOf(posData[4]));
					predictedPosition.set(0,0,Double.valueOf(posData[6]));
					predictedPosition.set(1,0,Double.valueOf(posData[7]));
				} else if(order == 3){
					referencePosition.set(0,0,Double.valueOf(posData[6]));
					referencePosition.set(1,0,Double.valueOf(posData[7]));
					predictedPosition.set(0,0,Double.valueOf(posData[9]));
					predictedPosition.set(1,0,Double.valueOf(posData[10]));
				} else if(order == 4){
					referencePosition.set(0,0,Double.valueOf(posData[9]));
					referencePosition.set(1,0,Double.valueOf(posData[10]));
					predictedPosition.set(0,0,Double.valueOf(posData[12]));
					predictedPosition.set(1,0,Double.valueOf(posData[13]));
				} else if(order == 5){
					referencePosition.set(0,0,Double.valueOf(posData[12]));
					referencePosition.set(1,0,Double.valueOf(posData[13]));
					predictedPosition.set(0,0,Double.valueOf(posData[15]));
					predictedPosition.set(1,0,Double.valueOf(posData[16]));
				}
				/*m.set(6, 0, Double.valueOf(posData[6]));
				m.set(7, 0, Double.valueOf(posData[7]));
				m.set(0, 0, Double.valueOf(posData[3]));
				m.set(1, 0, Double.valueOf(posData[4]));*/
				String errors = br.readLine();
				String[] errorData = errors.split(" ");
				double error = Double.valueOf(errorData[errorData.length-1]);
				double lastDistance = Double.valueOf(errorData[errorData.length-2]);


				sCurrentLine = br.readLine();
				String prediction = sCurrentLine;
				posData = prediction.split(" ");
				double reliability = Double.valueOf(posData[3]);
				double relativeError = Double.valueOf(posData[0]);

				
				TestResult result = new TestResult(predictedPosition, referencePosition, reliability, error, lastDistance, relativeError);

				/*
				 * m.set(4, 0, Double.valueOf(posData[4])); m.set(5, 0,
				 * Double.valueOf(posData[5])); m.set(6, 0,
				 * Double.valueOf(posData[6]));
				 */
				resultArray.add(result);
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

		return resultArray;

	}
	
	private static ArrayList<SimpleMatrix> readTrainingData(String filename) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.trim();
				String[] posData = sCurrentLine.split(" ");
				SimpleMatrix m = new SimpleMatrix(posData.length,1);
				for(int i=0; i<posData.length; i++)
					m.set(i,0,Double.parseDouble(posData[i]));
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

	private static String readTrainingDataFileName(String filename) {
		String trainingDataFileName="";
		BufferedReader br = null;
		try {
			String sCurrentLine="";
			br = new BufferedReader(new FileReader(filename));
			while (!sCurrentLine.startsWith("Input data file: ")) {
				sCurrentLine = br.readLine();
			}
			trainingDataFileName = sCurrentLine.replace("Input data file: ", "");
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
		return trainingDataFileName;

	}

	private static void cumulativeErrorDistToFile(List<Double> errors, int samplingRate, String fileName) {
		// error distribution
		Double[] errorsSorted = errors.toArray(new Double[0]);
		Arrays.sort(errorsSorted);
		double[] errorsSortedSubSet = new double[(errorsSorted.length / samplingRate) + 1];
		for (int i = 0; i < errorsSorted.length; i++) {
			if (i % samplingRate == 0)
				errorsSortedSubSet[i / samplingRate] = errorsSorted[i];
		}
		errorsToFile(errorsSortedSubSet, fileName);
	}
	
	private static void spatialerrorDistToFile(List<SimpleMatrix> spatialErrorList, String fileName, double threshold) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(fileName);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			double cumulative = 0;
			pw.println("lon lat error");
			for (int i=0; i<spatialErrorList.size(); i++){
				SimpleMatrix error = spatialErrorList.get(i);
				if(error.get(2,0) <= threshold)
					pw.println(error.get(0,0)+" "+error.get(1,0)+" "+error.get(2,0));
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	
	private static void errorsToFile(double[] errors, String filename) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(filename);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			double cumulative = 0;
			pw.println("x y");
			for (int i=0; i<errors.length; i++){
				cumulative += 1d/(double)errors.length;
				pw.println(errors[i]+" "+cumulative);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	
	
	
	private static void errorsToFile(Double[] errors, String filename) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(filename);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			double cumulative = 0;
			pw.println("x y");
			for (int i=0; i<errors.length; i++){
				cumulative += 1d/(double)errors.length;
				pw.println(errors[i]+" "+cumulative);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	private static void predictionReliabilitiesToFile(double[] predictionReliabilities) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("predictionReliabilities.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			double cumulative = 0;
			for (int i=0; i<predictionReliabilities.length; i++){
				cumulative += 1d/(double)predictionReliabilities.length;
				pw.println(predictionReliabilities[i]+" "+cumulative);
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	
	private static void coordinatesToFile(List<SimpleMatrix> data, String file) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter(file);
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
		pw.close();
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
