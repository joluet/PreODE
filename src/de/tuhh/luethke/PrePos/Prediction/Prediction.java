/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Prediction;

/**
 * Container for a prediction result.
 * 
 * @author Jonas Luethke
 * 
 * 
 */
public class Prediction {
	public final double latitude;
	public final double longitude;
	public final double marginalProbability;
	public final double probability;
	public final double widerProbability;
	public final double acurracy;

	/**
	 * Create a new prediction result object.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param marginalProbability
	 * @param probability
	 * @param widerProbability
	 * @param acurracy
	 */
	public Prediction(double latitude, double longitude, double marginalProbability, double probability,
			double widerProbability, double acurracy) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.marginalProbability = marginalProbability;
		this.probability = probability;
		this.widerProbability = widerProbability;
		this.acurracy = acurracy;
	}

}
