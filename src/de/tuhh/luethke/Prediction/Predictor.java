package de.tuhh.luethke.Prediction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.oKDE.model.ConditionalDistribution;
import de.tuhh.luethke.oKDE.model.SampleModel;
import de.tuhh.luethke.oKDE.utility.Optimization;
import de.tuhh.luethke.oKDE.utility.SearchResult;

public class Predictor {

	private SampleModel mSampleModel;

	public Predictor(SampleModel model) {
		mSampleModel = model;
	}

	
	private static void dataToHeatMapFile(List<SimpleMatrix> data) {
		PrintWriter pw = null;
		try {
			Writer fw = new FileWriter("dataForHeatMap_weighted.txt");
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (SimpleMatrix m : data){
				SimpleMatrix tmp = Preprocessor.projectDataBack(new SimpleMatrix(m));
				pw.println(tmp.get(0, 0) + " " + tmp.get(1, 0));
			}
		}

		catch (IOException e) {
			System.err.println("Error creating file!");
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	/**
	 * Calculates the coordinates of a two dimensional vector. The returned
	 * point lies on a circle around the given center with radius r at the angle
	 * alpha = ( k * (circleSegment/r) ) k = {0,...,(2*pi*r / circleSegment)}
	 * 
	 * @param r
	 *            The radius of the circle.
	 * @param circleSegment
	 *            The length of the circle segments the circle is subdivided
	 *            into.
	 * @param k
	 *            The index that determines how many circle segments to use.
	 * @param center
	 *            The center point of the circle.
	 * @return The coordinates of the point in Cartesian system.
	 */
	private SimpleMatrix getPointOnCircle(double r, double circleSegment, int k, SimpleMatrix center) {
		// calculate angle in radians
		double alpha = ((double) k) * (circleSegment / r);
		// transform to polar coordinates using given center and calculated
		// angle
		double x1 = center.get(0, 0) + r * Math.cos(alpha);
		double x2 = center.get(1, 0) + r * Math.sin(alpha);
		// put coordinates into vector
		SimpleMatrix point = new SimpleMatrix(2, 1);
		point.set(0, 0, x1);
		point.set(1, 0, x2);
		return point;
	}

	private double volumeOver(double h1, double[] h23, double h, double a) {
		double volume = 0;
		for (int i = 0; i < (h23.length-1); i++) {
			double avgHeight = (h1 + h23[i] + h23[i+1])/3;
			double base = 0.5 * h * a;
			volume += (base * avgHeight);
		}
		double avgHeight = (h1 + h23[0] + h23[h23.length-1])/3;
		volume += (0.5 * h * a * avgHeight);
		return volume;
	}

	private ArrayList<ArrayList<SimpleMatrix>> getNGonPointsAroundCenter(SimpleMatrix center, double radius, double distance) {
		// define points on concentric circles around last measured position
		// that are used as starting points for maximum search
		ArrayList<ArrayList<SimpleMatrix>> circlePoints = new ArrayList<ArrayList<SimpleMatrix>>();
		double circleSegment = distance;
		int N = (int) (radius / circleSegment);
		for (int i = 1; i <= N; i++) {
			double currentRadius = circleSegment * i;
			int noOfCircleSegments = (int) (2 * Math.PI * currentRadius / circleSegment);
			/*ArrayList<SimpleMatrix> pointsOnSubCircle = new ArrayList<SimpleMatrix>(noOfCircleSegments);
			for (int j = 0; j < noOfCircleSegments; j++) {
				SimpleMatrix p = getPointOnCircle(currentRadius, circleSegment, j, center);
				pointsOnSubCircle.add(p);
			}
			circlePoints.add(pointsOnSubCircle);*/
			circlePoints.add(getNGonPointsAroundCenterSingle(center, currentRadius, noOfCircleSegments));
		}
		return circlePoints;
	}
	
	private ArrayList<SimpleMatrix> getNGonPointsAroundCenterSingle(SimpleMatrix center, double radius, int noOfCircleSegments) {
		double distance =2 * Math.PI * radius / ((double)noOfCircleSegments);
		ArrayList<SimpleMatrix> pointsOnSubCircle = new ArrayList<SimpleMatrix>(noOfCircleSegments);
		for (int j = 0; j < noOfCircleSegments; j++) {
			SimpleMatrix p = getPointOnCircle(radius, distance, j, center);
			pointsOnSubCircle.add(p);
		}
		return pointsOnSubCircle;
	}
	
	//private double cumulativeConditional()

	public Prediction predict(Measurement[] measurements, double maxRadius, double gridSegmentSize, int outputProbSquareWidth,
			int outputProbSquareSegments) {
		double accuracyRadius = 800;
		// put last measured position into vector
		SimpleMatrix lastPositionVector = new SimpleMatrix(2, 1);
		int steps = measurements.length;
		lastPositionVector.set(0, 0, measurements[steps - 1].getLat());
		lastPositionVector.set(1, 0, measurements[steps - 1].getLng());
		lastPositionVector = Preprocessor.projectData(lastPositionVector);

		// define points on concentric circles around last measured position
		// that are used as starting points for maximum search
		ArrayList<ArrayList<SimpleMatrix>> circlePoints = getNGonPointsAroundCenter(lastPositionVector, maxRadius, gridSegmentSize);
		

		long time = System.currentTimeMillis();

		// put given previous measured positions into one vector
		SimpleMatrix measuredPositions = new SimpleMatrix(2 * steps, 1);
		for (int k = 0; k < steps; k++) {
			measuredPositions.set(2 * k, 0, measurements[k].getLat());
			measuredPositions.set(2 * k + 1, 0, measurements[k].getLng());
		}
		// project vector using UTM
		measuredPositions = Preprocessor.projectData(measuredPositions);

		// obtain conditional distribution from mixture model using measured
		// positions as condition
		ConditionalDistribution conditionalDist = mSampleModel.getConditionalDistribution(measuredPositions);

		// start search for maxima on each previously defined point on
		// concentric circles around last measured position
		double maxProbability = 0;
		double widerProbability = 0;
		double maxWiderProbability = 0;
		SimpleMatrix maxPoint = null;
		for (int i = 0; i < circlePoints.size(); i++) {
			// for each sub circle
			ArrayList<SimpleMatrix> subCirclePoints = circlePoints.get(i);
			for (int j = 0; j < subCirclePoints.size(); j++) {
				SimpleMatrix pointOnCircle = subCirclePoints.get(j);
				// define starting point for maxima search
				SimpleMatrix startPoint = new SimpleMatrix(2 * steps + 2, 1);
				for (int k = 0; k < measuredPositions.numRows(); k++) {
					startPoint.set(k, 0, measuredPositions.get(k, 0));
				}
				startPoint.set(2 * steps, 0, pointOnCircle.get(0, 0));
				startPoint.set(2 * steps + 1, 0, pointOnCircle.get(1, 0));
				//  start search for each point on sub circle
				SearchResult result = Optimization.gradQuadrSearch(startPoint, conditionalDist.conditionalMeans, conditionalDist.conditionalCovs,
						conditionalDist.conditionalWeights, mSampleModel);
				double probability = result.probability;
				if (probability > maxProbability) {
					// calculate cumulative probability in regular n-gon around
					// maximum
					ArrayList<SimpleMatrix> fineCirclePoints = getNGonPointsAroundCenterSingle(result.point, accuracyRadius, 8);
					double h[] = new double[fineCirclePoints.size()];
					for(int l=0; l<fineCirclePoints.size(); l++) {
						h[l] = mSampleModel.evaluate(fineCirclePoints.get(l), conditionalDist.conditionalMeans, conditionalDist.conditionalCovs, conditionalDist.conditionalWeights);
					}
					double s = 2*accuracyRadius*Math.sin(Math.PI/7);
					double baseHeight = accuracyRadius*Math.cos(Math.PI/7);
					widerProbability = volumeOver(probability, h, baseHeight, s);
					if(widerProbability > maxWiderProbability){
						maxWiderProbability = widerProbability;
						maxProbability = probability;
						maxPoint = result.point;
					}
				}

			}
		}
		// calculate cumulative marginal probability around prediction point
		ArrayList<SimpleMatrix> fineCirclePoints = getNGonPointsAroundCenterSingle(maxPoint, accuracyRadius, 8);
		mSampleModel.marginal(maxPoint, margDimensions)
		double h[] = new double[fineCirclePoints.size()];
		for(int l=0; l<fineCirclePoints.size(); l++) {
			h[l] = mSampleModel.evaluate(fineCirclePoints.get(l), conditionalDist.conditionalMeans, conditionalDist.conditionalCovs, conditionalDist.conditionalWeights);
		}
		double s = 2*accuracyRadius*Math.sin(Math.PI/7);
		double baseHeight = accuracyRadius*Math.cos(Math.PI/7);
		widerProbability = volumeOver(probability, h, baseHeight, s);
		if(widerProbability > maxWiderProbability){
			maxWiderProbability = widerProbability;
			maxProbability = probability;
			maxPoint = result.point;
		}
		
		System.out.println("Loop time: " + (System.currentTimeMillis() - time));

		SimpleMatrix prediction = maxPoint;

		
		double marginal = mSampleModel.cummulativeMarginal(measuredPositions, prediction, outputProbSquareWidth, outputProbSquareSegments,
				outputProbSquareSegments);
		prediction = Preprocessor.projectDataBack(maxPoint);
		Prediction p = new Prediction(prediction.get(0, 0), prediction.get(1, 0), marginal, maxProbability, maxWiderProbability, 200);
		return p;
	}

	
}
