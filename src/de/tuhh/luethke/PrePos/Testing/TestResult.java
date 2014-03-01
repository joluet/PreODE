/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Testing;

import org.ejml.simple.SimpleMatrix;

public class TestResult {

	public SimpleMatrix prediction;
	public SimpleMatrix referencePosition;
	public double reliability;
	public double error;
	public double lastDistance;
	public double relativeError;
	
	public TestResult(SimpleMatrix prediction, SimpleMatrix referencePosition, double reliability, double error, double lastDistance, double relativeError) {
		super();
		this.prediction = prediction;
		this.referencePosition = referencePosition;
		this.reliability = reliability;
		this.error = error;
		this.lastDistance = lastDistance;
		this.relativeError = relativeError;

	}
	
	

}
