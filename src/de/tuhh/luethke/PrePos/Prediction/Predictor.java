/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Prediction;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;
import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.okde.model.ConditionalDistribution;
import de.tuhh.luethke.okde.model.SampleModel;
import de.tuhh.luethke.okde.utility.Optimization.Optimization;
import de.tuhh.luethke.okde.utility.Optimization.SearchResult;

/**
 * This class calls quadratic optimization methods from the oKDE package to
 * provide a prediction.
 * 
 * @author Jonas Luethke
 * 
 */
public class Predictor {

	private SampleModel mSampleModel;

	/**
	 * Creates a new predictor object using a given sample model.
	 * 
	 * @param model
	 *            The kde model to use for prediction.
	 */
	public Predictor(SampleModel model) {
		mSampleModel = model;
	}

	/**
	 * This method calculates the coordinates of a two dimensional vector. The
	 * returned point lies on a circle around the given center with radius r at
	 * the angle alpha = ( k * (circleSegment/r) ) k = {0,...,(2*pi*r /
	 * circleSegment)}
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

	/**
	 * This method calculates the coordinates of points that are distributed a
	 * concentric circle around the given center point. The points are equally
	 * spaced. The number of points is defined by the parameter
	 * noOfCircleSegments.
	 * 
	 * @param center
	 *            Center point of circle.
	 * @param radius
	 *            Radius of the circle.
	 * @param noOfCircleSegments
	 *            The number of segments to divide the circle into.
	 * @return A list containing the points related to each the circle segments.
	 */
	private ArrayList<SimpleMatrix> getPointsOnConcentricCircle(SimpleMatrix center, double radius,
			int noOfCircleSegments) {
		double distance = 2 * Math.PI * radius / ((double) noOfCircleSegments);
		ArrayList<SimpleMatrix> pointsOnSubCircle = new ArrayList<SimpleMatrix>(noOfCircleSegments);
		for (int j = 0; j < noOfCircleSegments; j++) {
			SimpleMatrix p = getPointOnCircle(radius, distance, j, center);
			pointsOnSubCircle.add(p);
		}
		return pointsOnSubCircle;
	}

	/**
	 * This method is used to define the search area for the mode finding
	 * algorithm. It calculates the coordinates of points that are distributed
	 * on N concentric circles around the given center point. The distance
	 * between each circle is defined by the given parameter. On each circle the
	 * points are again spaced using the distance parameter.
	 * 
	 * @param center
	 *            Center point of circles.
	 * @param radius
	 *            Radius of largest circle.
	 * @param distance
	 *            Distance between circles and points on circles.
	 * @return A list with sub lists containing the points related to each
	 *         concentric circle.
	 */
	private ArrayList<ArrayList<SimpleMatrix>> getPointsOnConcentricCircles(SimpleMatrix center, double radius,
			double distance) {
		// define points on concentric circles around last measured position
		// that are used as starting points for maximum search
		ArrayList<ArrayList<SimpleMatrix>> circlePoints = new ArrayList<ArrayList<SimpleMatrix>>();
		double circleSegment = distance;
		int N = (int) (radius / circleSegment);
		for (int i = 1; i <= N; i++) {
			double currentRadius = circleSegment * i;
			int noOfCircleSegments = (int) (2 * Math.PI * currentRadius / circleSegment);
			circlePoints.add(getPointsOnConcentricCircle(center, currentRadius, noOfCircleSegments));
		}
		return circlePoints;
	}

	/**
	 * Calculates the volume of the regular n-gon that is defined by the given
	 * parameters.
	 * 
	 * @param centerHeight
	 *            The height of the center point of the n-gon.
	 * @param sourroundingHeights
	 *            The heigths of all sorrounding points of the n-gon.
	 * @param incircleRadius
	 *            The incircle radius of the n-gon.
	 * @param edgeLength
	 *            The length of an edge of the n-gon.
	 * @return The volume of the n-gon.
	 */
	private double volumeOfNGon(double centerHeight, double[] sourroundingHeights, double incircleRadius,
			double edgeLength) {
		// calculate the volume of each sub prism and sum them up
		double volume = 0;
		for (int i = 0; i < (sourroundingHeights.length - 1); i++) {
			double avgHeight = (centerHeight + sourroundingHeights[i] + sourroundingHeights[i + 1]) / 3;
			double base = 0.5 * incircleRadius * edgeLength;
			volume += (base * avgHeight);
		}
		double avgHeight = (centerHeight + sourroundingHeights[0] + sourroundingHeights[sourroundingHeights.length - 1]) / 3;
		volume += (0.5 * incircleRadius * edgeLength * avgHeight);
		return volume;
	}

	/**
	 * This method predicts a future location based on the model that is
	 * associated with this Predictor object. The prediction is based on the
	 * conditional distribution of the model.
	 * 
	 * @param measurements
	 *            Latest measured positions to base the prediction on.
	 * @param searchRadius
	 *            Radius used to search for maxima in conditional distribution.
	 * @param searchSegmentDistance
	 *            Distance that defines how fine the search grid is.
	 * @param accuracyRadius
	 *            Radius used when estimating the cumulative probability of a
	 *            future location.
	 * @param predictionSegments
	 *            Determines the accuracy of probability calculation.
	 * @return A prediction object containig the predicted location as well as
	 *         the estimated probability.
	 */
	public Prediction predict(Measurement[] measurements, double searchRadius, double searchSegmentDistance,
			double accuracyRadius, int predictionSegments, int UTMZoneNo, char UTMZoneLetter,
			boolean useAdditionalInformation) {
		// put last measured position into vector
		SimpleMatrix lastPositionVector = new SimpleMatrix(2, 1);
		int steps = measurements.length;
		lastPositionVector.set(0, 0, measurements[steps - 1].getLat());
		lastPositionVector.set(1, 0, measurements[steps - 1].getLng());
		lastPositionVector = Preprocessor.projectData(lastPositionVector);

		// define points on concentric circles around last measured position
		// that are used as starting points for maximum search
		ArrayList<ArrayList<SimpleMatrix>> circlePoints = getPointsOnConcentricCircles(lastPositionVector,
				searchRadius, searchSegmentDistance);

		long time = System.currentTimeMillis();

		// put given previous measured positions into one vector
		SimpleMatrix measuredPositions = null;
		if (!useAdditionalInformation) {
			measuredPositions = new SimpleMatrix(2 * steps, 1);
			for (int k = 0; k < steps; k++) {
				measuredPositions.set(2 * k, 0, measurements[k].getLat());
				measuredPositions.set(2 * k + 1, 0, measurements[k].getLng());
			}
		} else {
			measuredPositions = new SimpleMatrix(2 * steps + 3, 1);
			measuredPositions.set(0, 0, measurements[0].getLat());
			measuredPositions.set(1, 0, measurements[0].getLng());
			measuredPositions.set(2, 0, measurements[0].getSpeed());
			measuredPositions.set(3, 0, measurements[0].getTimeOfDay());
			measuredPositions.set(4, 0, measurements[0].getmDirection());
		}
		// project vector using UTM
		if (useAdditionalInformation)
			measuredPositions = Preprocessor.projectDataFO(measuredPositions);
		else
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
		int add = 0;
		if (useAdditionalInformation)
			add = 3;
		for (int i = 0; i < circlePoints.size(); i++) {
			// for each sub circle
			ArrayList<SimpleMatrix> subCirclePoints = circlePoints.get(i);
			for (int j = 0; j < subCirclePoints.size(); j++) {
				SimpleMatrix pointOnCircle = subCirclePoints.get(j);
				// define starting point for maxima search
				SimpleMatrix startPoint = new SimpleMatrix(2 * steps + 2 + add, 1);
				for (int k = 0; k < measuredPositions.numRows(); k++) {
					startPoint.set(k, 0, measuredPositions.get(k, 0));
				}
				startPoint.set(2 * steps + add, 0, pointOnCircle.get(0, 0));
				startPoint.set(2 * steps + 1 + add, 0, pointOnCircle.get(1, 0));
				// start search for each point on sub circle
				SearchResult result = Optimization.gradQuadrSearch(startPoint, conditionalDist.conditionalMeans,
						conditionalDist.conditionalCovs, conditionalDist.conditionalWeights, mSampleModel);
				double probability = result.probability;
				if (probability > maxProbability) {
					// calculate cumulative probability in regular n-gon around
					// maximum
					ArrayList<SimpleMatrix> fineCirclePoints = getPointsOnConcentricCircle(result.point,
							accuracyRadius, predictionSegments);
					double h[] = new double[fineCirclePoints.size()];
					for (int l = 0; l < fineCirclePoints.size(); l++) {
						h[l] = mSampleModel.evaluate(fineCirclePoints.get(l), conditionalDist.conditionalMeans,
								conditionalDist.conditionalCovs, conditionalDist.conditionalWeights);
					}
					double s = 2 * accuracyRadius * Math.sin(Math.PI / predictionSegments);
					double baseHeight = accuracyRadius * Math.cos(Math.PI / predictionSegments);
					widerProbability = volumeOfNGon(probability, h, baseHeight, s);
					if (widerProbability > maxWiderProbability) {
						maxWiderProbability = widerProbability;
						maxProbability = probability;
						maxPoint = result.point;
					}
				}

			}
		}
		// calculate marginal probability for prediction point
		ConditionalDistribution marginalDistribution = mSampleModel.getMarginalDistribution(steps * 2 + add);
		double marginal = mSampleModel.evaluate(measuredPositions, marginalDistribution.conditionalMeans,
				marginalDistribution.conditionalCovs, marginalDistribution.conditionalWeights);

		System.out.println("Loop time: " + (System.currentTimeMillis() - time));
		Prediction p = null;
		if (maxPoint != null) {
			SimpleMatrix prediction = Preprocessor.projectDataBack(maxPoint, UTMZoneNo, UTMZoneLetter);
			p = new Prediction(prediction.get(0, 0), prediction.get(1, 0), marginal, maxProbability,
					maxWiderProbability, accuracyRadius);
		}
		return p;
	}

}
