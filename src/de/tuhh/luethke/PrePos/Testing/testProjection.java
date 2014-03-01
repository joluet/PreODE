/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Testing;

import de.tuhh.luethke.projection.Ellipsoid;
import de.tuhh.luethke.projection.ObliqueMercator;
import de.tuhh.luethke.projection.PVector;

public class testProjection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ObliqueMercator merc = new ObliqueMercator(new Ellipsoid(Ellipsoid.WGS_84));
		PVector p = new PVector(53.569000f,9.926000f);
		System.out.println(merc.transformCoords(p));
		p=new PVector(53.54f,9.98f);
		System.out.println(merc.transformCoords(p));
		p=new PVector(53.56765f,9.92473f);
		PVector coord1 = merc.transformCoords(p);
		PVector p1=new PVector(53.567587f,9.929268f);
		PVector coord2 = merc.transformCoords(p1);
		System.out.println("Distance: "+coord1.dist(coord2));
		
		p=new PVector(46.761492f,7.618867f);
		 coord1 = merc.transformCoords(p);
		 p1=new PVector(46.761345f,7.618627f);
		 coord2 = merc.transformCoords(p1);
		System.out.println("Distance, swiss: "+coord1.dist(coord2));
	}

}
