package de.tuhh.luethke.PrePos.Testing;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.oKDE.model.SampleModel;

public class EvaluationWorker implements Callable<Double> {

	private int[] condDim;
	private SimpleMatrix point;
	private SampleModel model;
	
	public EvaluationWorker(SimpleMatrix point, int[]condDim, SampleModel model) {
		this.point =point;
		this.condDim = condDim;
		try {
			this.model = new SampleModel(model);
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Double call() throws Exception {
		return model.evaluateConditional(point,condDim);
	}


}
