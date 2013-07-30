package de.tuhh.luethke.PrePos.Transformation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.utility.Measurement;

public class PositionalTSTransformer {

	/**
	 * Maximum time difference between two data points: If time difference is
	 * smaller the data points are considered to be conditionally dependent.
	 * */
	private static final long MAX_TIME_DIFF_SEC = 290; // 5 minutes
	private static final long MIN_TIME_DIFF_SEC = 260; // 4 minutes

	/**
	 * This method does the actual data transformation. Positional time series
	 * data is mapped to n-dimensional space to encode dependency.
	 * 
	 * @param data
	 * @param order
	 * @return
	 */
	public static LinkedList<SimpleMatrix> transformTSData(List<Measurement> data, int order) {
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement tmp = null;
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(i).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > MIN_TIME_DIFF_SEC && Math.abs(timeDiff) < MAX_TIME_DIFF_SEC) {
					batch[batchIndex++] = data.get(j);
					/*
					 * if(order == 2 && batchIndex==1 && timeDiff<0){ //swap
					 * Measurement swap = batch[1]; batch[1] = batch[0];
					 * batch[0] = swap; }
					 */
				} else if (Math.abs(timeDiff) > MAX_TIME_DIFF_SEC)
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null))
				transformedData.add(measurementsToSimpleMatrix(batch));
		}
		return transformedData;
	}
	
	public static LinkedList<SimpleMatrix> transformTSData1(List<Measurement> data, int order) {
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement tmp = null;
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			int k = i;
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(k).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > MIN_TIME_DIFF_SEC && Math.abs(timeDiff) < MAX_TIME_DIFF_SEC) {
					batch[batchIndex++] = data.get(j);
					k=j;
					/*
					 * if(order == 2 && batchIndex==1 && timeDiff<0){ //swap
					 * Measurement swap = batch[1]; batch[1] = batch[0];
					 * batch[0] = swap; }
					 */
				} else if (Math.abs(timeDiff) > MAX_TIME_DIFF_SEC)
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null))
				transformedData.add(measurementsToSimpleMatrix(batch));
		}
		return transformedData;
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
	
	private static SimpleMatrix measurementsToSimpleMatrix1(Measurement[] measurements) {
		SimpleMatrix matrix = new SimpleMatrix(measurements.length * 2+1, 1);
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
		matrix.set(row,Math.abs(measurements[1].getDate()-measurements[0].getDate()));
		return matrix;
	}

}
