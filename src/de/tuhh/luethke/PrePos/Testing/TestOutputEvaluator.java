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

import de.tuhh.luethke.PrePos.Transformation.PositionalTSTransformer;
import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.CabDataParser;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.utility.Compression.Compressor;
import de.tuhh.luethke.oKDE.utility.Matrices.MatrixOps;

class SimpleMatrixComp implements Comparator<SimpleMatrix> {
	@Override
	public int compare(SimpleMatrix m, SimpleMatrix m1) {
		return Double.compare(m.get(2, 0), m1.get(2, 0));
	}
}

public class TestOutputEvaluator {

	public static void main(String[] args) {
		// calculate sigma
		// LinkedList<Measurement> testData =
		// CabDataPaser.parse("test_data/new_ofikco.txt");
		// Preprocessor.processTestData(testData);
		// System.out.println(testData.size());
		// List<SimpleMatrix> testDataVectors =
		// PositionalTSTransformer.transformTSData1(testData, 3, 600, 30, 380);

		// Preprocessor.projectData(testDataVectors);

		// calculate autocorrelation for tau in {300,600,900,1200,1500,1800}
		/*
		 * for(int tau=3; tau<100; tau+=1){ List<SimpleMatrix> testDataVectors2
		 * = PositionalTSTransformer.transformTSData1(testData, 2, tau, 30,
		 * 450); Preprocessor.projectData(testDataVectors2); double sigma = 0;
		 * SimpleMatrix mu = new SimpleMatrix(2,1); SimpleMatrix tmp = new
		 * SimpleMatrix(2,1); double N = testDataVectors2.size(); // calc mu
		 * for(SimpleMatrix vector: testDataVectors2) {
		 * tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); mu = mu.plus(1/N,tmp);
		 * } // calc sigma for(SimpleMatrix vector: testDataVectors2) {
		 * tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); sigma +=
		 * ((tmp.minus(mu).transpose()).mult(tmp.minus(mu))).elementSum() *
		 * (1/N); //sigma.plus(MatrixOps.elemPow(tmp.minus(mu),2).scale(1/N)); }
		 * double squareMu = 0; // calc squareMu for(SimpleMatrix vector:
		 * testDataVectors2) { tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); squareMu +=
		 * tmp.transpose().mult(tmp).elementSum() * (1/N);
		 * //squareMu.plus(MatrixOps.elemPow(tmp,2).scale(1/N)); } double corr =
		 * 0; SimpleMatrix tmp1 = new SimpleMatrix(2,1); // calc squareMu
		 * for(SimpleMatrix vector: testDataVectors2) {
		 * tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0));
		 * tmp1.set(0,0,vector.get(vector.numRows()-4,0));
		 * tmp1.set(1,0,vector.get(vector.numRows()-3,0)); corr +=
		 * tmp.transpose().mult(tmp1).elementSum() * (1/N);
		 * //corr.plus((tmp.elementMult(tmp1)).scale(1/N)); } double ac = (corr-
		 * squareMu)/sigma; System.out.println("ac, tau="+tau+": "+ac); }
		 */

		/**
		 * SimpleMatrix sigma = new SimpleMatrix(2,1); SimpleMatrix mu = new
		 * SimpleMatrix(2,1); double N = testDataVectors.size(); // calc mu
		 * SimpleMatrix tmp = new SimpleMatrix(2,1); for(SimpleMatrix vector:
		 * testDataVectors) { tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); mu =
		 * mu.plus(tmp.scale(1/N)); } // calc sigma double sigma1 = 0;
		 * for(SimpleMatrix vector: testDataVectors) {
		 * tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); sigma =
		 * sigma.plus(MatrixOps.elemPow(tmp.minus(mu),2).scale(1/N)); sigma1 +=
		 * Compressor.euclidianDistance(tmp,mu)*(1/N); } double sigmaLen =
		 * Math.sqrt((sigma.get(0,0)*sigma.get(0,0)) +
		 * (sigma.get(1,0)*sigma.get(1,0)));
		 */
		// euclidian
		// calc mu
		/*
		 * tmp = new SimpleMatrix(2,1); double mu1 = 0; for(SimpleMatrix vector:
		 * testDataVectors) { tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); LatLng point1 = new
		 * LatLng(tmp.get(0,0), tmp.get(0,1)); LatLng point2 = new
		 * LatLng(mu.get(0,0), mu.get(0,1)); double distance =
		 * LatLngTool.distance(point1, point2, LengthUnit.METER); mu1 += (1/N) *
		 * distance; } // calc sigma double sigma1 = 0; for(SimpleMatrix vector:
		 * testDataVectors) { tmp.set(0,0,vector.get(vector.numRows()-2,0));
		 * tmp.set(1,0,vector.get(vector.numRows()-1,0)); LatLng point1 = new
		 * LatLng(tmp.get(0,0), tmp.get(0,1)); LatLng point2 = new
		 * LatLng(mu.get(0,0), mu.get(0,1)); double distance =
		 * LatLngTool.distance(point1, point2, LengthUnit.METER); sigma1 +=
		 * distance * distance * (1/N); }
		 */
		double k = 0;
		double meanRightAll = 0;
		double avgBetterThan1000=0;
		double probBetterThan1000=0;

		double avg02 = 0;
		ArrayList<Double> errors = new ArrayList<Double>();
		ArrayList<Double> predictionReliabilities = new ArrayList<Double>();
		ArrayList<Double> relativeErrorOfRight = new ArrayList<Double>();

		ArrayList<SimpleMatrix> wrongResults = new ArrayList<SimpleMatrix>();
		ArrayList<SimpleMatrix> rightResults = new ArrayList<SimpleMatrix>();
		ArrayList<SimpleMatrix> referencePositions = new ArrayList<SimpleMatrix>();



		final File folder = new File("/home/jonas/Dokumente/Masterarbeit/AWS/results/3_30/tmp/");
		ArrayList<String> fileNames = listFilesForFolder(folder);
		
		
		SimpleMatrix mse = new SimpleMatrix(2, 1);
		SimpleMatrix pre = new SimpleMatrix(2, 1);
		SimpleMatrix act = new SimpleMatrix(2, 1);
		
		
		
		for (int i = 0; i < fileNames.size(); i++) {
			String cabFileName = readInfoFromFile("/home/jonas/Dokumente/Masterarbeit/AWS/results/3_30/tmp/" + fileNames.get(i));
			System.out.println(cabFileName);
			ArrayList<SimpleMatrix> results = readFromFile("/home/jonas/Dokumente/Masterarbeit/AWS/results/3_30/tmp/" + fileNames.get(i));
			String trainingDataFileName = readTrainingDataFileName("/home/jonas/Dokumente/Masterarbeit/AWS/results/3_30/tmp/" + fileNames.get(i));
//			if(trainingDataFileName.contains("1"))
//				trainingDataFileName = trainingDataFileName.replace("1", "");
			ArrayList<Double> RelMESList = new ArrayList<Double>();

//			List<SimpleMatrix> trainingDataVectors = PositionalTSTransformer.transformTSData1(trainingData, 4, 900, 30, 4500);
			// calc mu
/*			SimpleMatrix tmp = new SimpleMatrix(2, 1);
			SimpleMatrix mu = new SimpleMatrix(2, 1);
			int N = (trainingDataVectors.size()-450);
			for (int j=0; j<N;j++) {
				tmp.set(0, 0, trainingDataVectors.get(j).get(0, 0));
				tmp.set(1, 0, trainingDataVectors.get(j).get(1, 0));
				tmp = Preprocessor.projectData(tmp);
				mu = mu.plus(tmp.scale((1d / (double)N)));
			}
*/
			int countSm1000 = 0;
			double mse1 = 0;
			double meanRight = 0;

			double count1 = 0;
			

			ArrayList<Double> relativeErrorDistribution =new ArrayList<Double>();
			for (int j = 0; j < 10; j++) {
				relativeErrorDistribution.add(0d);
			}
			//double variance = 0;
			
			trainingDataFileName = "/home/jonas/Dokumente/Masterarbeit/AWS/testinput/tmp_30"+trainingDataFileName.substring(trainingDataFileName.lastIndexOf('/'));
			ArrayList<SimpleMatrix> trainingData = readTrainingData(trainingDataFileName);
			SimpleMatrix mean = mean(trainingData,0,2000);
			mean = Preprocessor.projectData(mean);
			SimpleMatrix var = variance(trainingData,mean,0,2000);
			System.out.println("var: "+var);
			System.out.println("|var|: "+Math.sqrt(var.elementSum()));
			
			SimpleMatrix mean_tst = mean(trainingData,2000,2200);
			mean_tst = Preprocessor.projectData(mean_tst);
			SimpleMatrix var_tst = variance(trainingData,mean_tst,2000,2200);
			System.out.println("var of test set: "+var_tst);
			System.out.println("|var_tst|: "+Math.sqrt(var_tst.elementSum()));

			for (SimpleMatrix m : results) {
				double relError = m.get(2, 0);
				pre.set(0, 0, m.get(0, 0));
				pre.set(1, 0, m.get(1, 0));
				act.set(0, 0, m.get(6, 0));
				act.set(1, 0, m.get(7, 0));
				referencePositions.add(new SimpleMatrix(act));

				pre = Preprocessor.projectData(pre);
				act = Preprocessor.projectData(act);
				if (m.get(5, 0) > 0.0 /* && m.get(3,0)>=0.01 */) {
					k++;

					if(Compressor.euclidianDistance(act,mean)<=1000){
						meanRight++;
						meanRightAll++;
					}
					
					// mse = mse.plus( MatrixOps.elemPow(pre.minus(act),2) );
					double addToMSE = Compressor.euclidianDistance(pre, act)*Compressor.euclidianDistance(pre, act);

//					double norm = Compressor.euclidianDistance(act, mu)* Compressor.euclidianDistance(act, mu);
					mse1 += addToMSE;
//					variance += norm;

//					RelMESList.add((addToMSE / norm));
					predictionReliabilities.add(m.get(5, 0));
					probBetterThan1000+=m.get(5, 0);
					errors.add(Compressor.euclidianDistance(pre, act));
					if (Compressor.euclidianDistance(pre, act) <= 1000) {
						countSm1000++;
						relativeErrorOfRight.add(relError);
						rightResults.add(m);
					}else
						wrongResults.add(m);
					
					for (int j = 0; j < 10; j++) {

						if (relError < ((0.01 * (double) j)+0.005) && relError >= ((0.01 * (double) j)-0.005)/* && m.get(3,0)>=0.01 */) {
							double error = relativeErrorDistribution.get(j);
							error++;
							relativeErrorDistribution.set(j, error);
						}
					}
					count1++;
				}

			}
			for (int j = 0; j < 10; j++) {
				String percent = String.valueOf((relativeErrorDistribution.get(j) / count1));
				if (percent.length() > 5)
					percent = percent.substring(0, 5);
				System.out.println(String.valueOf((0.1 * (double) j)).substring(0, 3) + " : " + relativeErrorDistribution.get(j) + "/" + count1 + " = " + percent + "%");
				if (j == 4)
					avg02 += ((double) relativeErrorDistribution.get(j) / count1);
			}
			if(RelMESList.size() > 0){
				Double[] MSEMedian = RelMESList.toArray(new Double[0]);
				Arrays.sort(MSEMedian);
				System.out.println("mse1 median: " + MSEMedian[MSEMedian.length / 2]);
			}
			mse1 /= (double)results.size();
			//variance /= (double)results.size();
			//System.out.println("mse1: " + (mse1/variance));
			System.out.println("below X: " + countSm1000);
			if(count1 > 0)
				avgBetterThan1000 += ((double)countSm1000)/((double)count1)*(1/((double)fileNames.size()));
			System.out.println(((double)countSm1000)/((double)count1));
			System.out.println(((double)meanRight)/((double)count1));
			if(((double)countSm1000)/((double)count1) > 2*((double)meanRight)/((double)count1))
				System.out.println("ssssssssssssssssssssssssss");

			System.out.println("---------------------------------------------------------");
		}
		avg02 /= fileNames.size();
		System.out.println(avg02);
		System.out.println("avg better than 1km: "+avgBetterThan1000);
		System.out.println("predicted: "+(probBetterThan1000/k) + "("+k+")");
		System.out.println("avg better using mean "+(meanRightAll/k) + "("+k+")");


		//System.out.println("avg better using mean: "+(meanRight/k));
		double meanError = 0;
		double meanErrorOfRight = 0;
		double errorOfRightCount = 0;
		double meanSquaredErrorOfRight = 0;
		double meanSquaredError = 0;
		double errorCount = 0;
		ArrayList<Double> realErrorList = new ArrayList<Double>();
		for(double error : errors) {
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
		/*double[] sortedRelativeErrorOfRightSubSet = new double[(sortedRelativeErrorOfRight.length/5)+1];
		for(int i=0; i<sortedRelativeErrorOfRight.length; i++){
			if(i%5 == 0)
				sortedRelativeErrorOfRightSubSet[i/5]=sortedRelativeErrorOfRight[i]; 
		}*/
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
	private static ArrayList<SimpleMatrix> readFromFile(String filename) {
		BufferedReader br = null;
		ArrayList<SimpleMatrix> dataArray = new ArrayList<SimpleMatrix>();

		try {
			String data = "";
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			while (!data.equals("Start prediction...")) {
				data = br.readLine();
			}
			int k = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				SimpleMatrix m = new SimpleMatrix(8, 1);
				while (sCurrentLine.startsWith("Loop") || sCurrentLine.length() == 0)
					sCurrentLine = br.readLine();
				if (sCurrentLine.startsWith("Time"))
					break;
				while (sCurrentLine != null && !sCurrentLine.contains("|"))
					sCurrentLine = br.readLine();
				if(sCurrentLine == null)
					break;
				String[] posData = sCurrentLine.split(" | ");
				m.set(6, 0, Double.valueOf(posData[6]));
				m.set(7, 0, Double.valueOf(posData[7]));
				m.set(0, 0, Double.valueOf(posData[9]));
				m.set(1, 0, Double.valueOf(posData[10]));
				
				/*m.set(6, 0, Double.valueOf(posData[6]));
				m.set(7, 0, Double.valueOf(posData[7]));
				m.set(0, 0, Double.valueOf(posData[3]));
				m.set(1, 0, Double.valueOf(posData[4]));*/
				br.readLine();
				sCurrentLine = br.readLine();
				String prediction = sCurrentLine;
				posData = prediction.split(" ");
				// k++;
				// System.out.println(k+sCurrentLine);
				m.set(2, 0, Double.valueOf(posData[0]));
				m.set(3, 0, Double.valueOf(posData[1]));
				m.set(4, 0, Double.valueOf(posData[2]));
				m.set(5, 0, Double.valueOf(posData[3]));
				/*
				 * m.set(4, 0, Double.valueOf(posData[4])); m.set(5, 0,
				 * Double.valueOf(posData[5])); m.set(6, 0,
				 * Double.valueOf(posData[6]));
				 */
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
