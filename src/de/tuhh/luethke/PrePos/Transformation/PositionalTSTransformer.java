package de.tuhh.luethke.PrePos.Transformation;

import java.util.Iterator;
import java.util.LinkedList;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.utility.Measurement;

public class PositionalTSTransformer {
    
    /**
     * Maximum time difference between two data points:
     * If time difference is smaller the data points are considered to be conditionally dependent.
     * */
    private static final long MAX_TIME_DIFF_SEC = 420; //7 minutes
    private static final long MIN_TIME_DIFF_SEC = 180; //3 minutes

    
    /**
     * This method does the actual data transformation. Positional time series data is mapped to
     * n-dimensional space to encode dependency.
     *
     * @param data
     * @param order
     * @return
     */
    public static LinkedList<SimpleMatrix> transformTSData(LinkedList<Measurement> data, int order) {
	LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
	Measurement tmp = null;
	Measurement[] batch = new Measurement[order];
	int batchIndex;
	for(int i=0; i<data.size(); i++){
	    batchIndex = 0;
	    for(int j=i; j<data.size() && batchIndex<order; j++){
		// calculate time difference
		double timeDiff = data.get(i).timeDiffInSeconds(data.get(j));
		// if time difference in bounds: add measurement to batch
		if(timeDiff > MIN_TIME_DIFF_SEC && timeDiff < MAX_TIME_DIFF_SEC) {
		    batch[batchIndex++] = data.get(j);
		}else if(timeDiff > MAX_TIME_DIFF_SEC)
		    // when time difference gets to big --> break!
		    break;
	    }
	    transformedData.add(measurementsToSimpleMatrix(batch));
	}
	return transformedData;
    }
    
    private static SimpleMatrix measurementsToSimpleMatrix(Measurement[] measurements) {
	SimpleMatrix matrix = new SimpleMatrix(measurements.length*2,1);
	double lat, lng;
	int row = 0;
	for(Measurement m : measurements){
	    lat = m.getLat10E6();
	    lng = m.getLng10E6();
	    matrix.set(row++, 0, lat);
	    matrix.set(row++, 0, lng);	    
	}
	return matrix;
    }
    
}
