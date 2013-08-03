package de.tuhh.luethke.PrePos.Testing;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.Transformation.Preprocessor;

public class TestProjection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] dxVector = new double[6][1];
		dxVector[0][0] = 37.80516;
		dxVector[1][0] = -122.41015;
		dxVector[2][0] = 37.71418;
		dxVector[3][0] = -122.39734;
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		System.out.println(pointVector.get(0,0)+" "+pointVector.get(1,0));
		SimpleMatrix pointVector1 = Preprocessor.projectData6(pointVector);
		pointVector1=Preprocessor.projectDataBack(pointVector1);
		System.out.println(pointVector1.get(0,0)+" "+pointVector1.get(1,0));
	}

}
