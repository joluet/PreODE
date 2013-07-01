package de.tuhh.luethke.PrePos;

import java.util.LinkedList;

import de.tuhh.luethke.PrePos.Transformation.Postprocessor;
import de.tuhh.luethke.PrePos.utility.LatitudeHistoryParser;
import de.tuhh.luethke.PrePos.utility.Measurement;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
	LinkedList<Measurement> measurements = LatitudeHistoryParser.parse("test_big.kml");
	System.out.println(measurements.size());
	Postprocessor.processData(measurements);
    }

}
