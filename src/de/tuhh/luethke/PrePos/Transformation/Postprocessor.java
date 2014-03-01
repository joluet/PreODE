/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Transformation;

import java.util.ArrayList;
import java.util.Arrays;

public class Postprocessor {

	private static final double MIN = 1E-30;
	
	public static ArrayList<int[]> medianFilter(double[][] pdfSamples) {
		// first calculate median
		ArrayList<Double> tmpList = new ArrayList<Double>();
		for(int row=0; row < pdfSamples.length; row++) {
			for(int col=0; col < pdfSamples[0].length; col++) {
				if(pdfSamples[row][col] > MIN)
					tmpList.add(pdfSamples[row][col]);
			}
		}
		if(tmpList.size() == 0)
			return null;
		Double[] tmp = tmpList.toArray(new Double[1]);
		Arrays.sort(tmp);
		double median = tmp[tmp.length/2];
		tmp = null;
		
		//now use median to perform filtering
		ArrayList<int[]> result = new ArrayList<int[]>();
		for(int row=0; row < pdfSamples.length; row++) {
			for(int col=0; col < pdfSamples[0].length; col++) {
				if((median) < pdfSamples[row][col]) {
					int[] point =  {row, col};
					result.add(point);
				}
			}
		}
		return result;
	}

}
