/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.prepos.Transformation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.prepos.utility.Measurement;

public class DelayEmbedder {

	/**
	 * This method does the actual data transformation. Positional time series data is mapped to
	 * n-dimensional space to encode conditional dependency.
	 * 
	 * @param data
	 * @param order
	 * @return
	 */
	public static LinkedList<SimpleMatrix> embed(List<Measurement> data, int order, int stepSize, int tolerance,
			int dataPointsNeeded, double minTravelDistance) {
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is
			// ended
			int k = i;
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(k).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > (stepSize - tolerance) && Math.abs(timeDiff) < (stepSize + tolerance)) {
					batch[batchIndex++] = data.get(j);
					k = j;
				} else if (Math.abs(timeDiff) > (stepSize + tolerance))
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null)) {
				double travelDistanceSum = differenceVector(batch);
				if (travelDistanceSum > minTravelDistance)
					transformedData.add(measurementsToSimpleMatrix(batch));
				if (transformedData.size() >= dataPointsNeeded)
					return transformedData;
			}
		}
		return transformedData;
	}

	private static double differenceVector(Measurement[] m) {
		SimpleMatrix differenceVector = new SimpleMatrix(m.length - 1, 1);
		for (int i = 0; i < m.length - 1; i++) {
			differenceVector.set(i, 0, m[i].distanceInMeters(m[i + 1]));
		}
		double travelDistanceSum = 0;
		travelDistanceSum = differenceVector.elementSum();
		return travelDistanceSum;
	}

	private static SimpleMatrix measurementsToSimpleMatrix(Measurement[] measurements) {
		SimpleMatrix matrix = new SimpleMatrix(measurements.length * 2, 1);
		double lat, lng;
		int row = 0;
		for (int i = 0; i < measurements.length; i++) {
			Measurement m = measurements[i];
			// if(i==0){
			lat = m.getLat();
			lng = m.getLng();
			/*
			 * } else{ lng = m.getLat(); lat = m.getLng(); }
			 */
			matrix.set(row++, 0, lat);
			matrix.set(row++, 0, lng);
		}
		return matrix;
	}

}
