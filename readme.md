PreODE
=========
Predction based on Online Density Estimation in the Delay Embedding Space

This is the java implementation of an algorithm that estimates
a model of human mobility and exploits the estimated model to predict future locations
of human individuals. The algorithm was developed as part of a master's thesis with the topic
*Location Prediction Based on Mobility Patterns in Location Histories*.


*******************************

1. [Location Prediction Based on Mobility Patterns in Location Histories](#thesis)
2. [Build Instructions](#build)
3. [Quickstart](#start)
4. [External Libraries Used](#ext_libs)
5. [License](#license)

*******************************




<a name="thesis">
##Master's thesis: Location Prediction Based on Mobility Patterns in Location Histories
</a>
The prediction algorithm that is implemented in this project is described in detail in my master's thesis.
Furthermore, the proposed algorithm was tested using mobility traces of taxis. The test results are listed and analyzed in my thesis as well.  

Download the complete thesis as pdf [here](https://www.dropbox.com/s/xqoo2pugqzeyd2o/mastersthesis_luthke.pdf?dl=1).

**Abstract**  
Human individuals generally tend to follow several habits during the course of the day.
This fact intuitively allows predicting human behavior to a certain degree based on previous observations.
This thesis focuses on the mobility of human individuals. To be specific, a generic algorithm that uses
*kernel density estimation* and *quadratic optimization* to provide location predictions is proposed.
There are several imaginable fields of application for such an algorithm, like for example location based services or commercials.
The proposed algorithm was implemented and tested using mobility traces of taxis.
The test results clearly indicate that the algorithm can extract and exploit patterns in the data to predict future locations.
For instance, the algorithm achieves an accuracy better than 1000m in approximately 32% of the executed tests using a prediction interval of six minutes.
Moreover, in 13% of these tests the prediction error is smaller than 500m. In addition,
the test results show that the algorithm is able to estimate the reliability of its predictions with an accuracy of up to 98.75%.
As expected, the test results also clearly demonstrate that the prediction capability of the algorithm strongly depends on the properties
of the given location data and the underlying stochastic process.



<a name="build">
## Build Instructions

Just execute ant in project root to compile the project:

**$ ant**

Afterwards, the packed jar file can be found in the *dist*-folder.



<a name="start">
## Quickstart





<a name="ext_libs">
## External Libraries Used

The following libraries are used in this project:  
 *  [EJML v0.24](https://code.google.com/p/efficient-java-matrix-library/),
 	a linear algebra library for manipulating dense matrices  
 	License: [Apache v2.0](http://www.apache.org/licenses/LICENSE-2.0)  
 *	[SimpleLatLng](https://code.google.com/p/simplelatlng/),
 	Provides a simple, lightweight library for common latitude and longitude calculation needs in Java.  
 	License: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)  
 *	[Jcoord](http://www.jstott.me.uk/jcoord/)
 	A library that helps to easily convert between latitude/longitude,
 	Universal Transverse Mercator (UTM) and Ordnance Survey (OSGB) references  
 	License: [GNU General Public License (GPL)](http://www.jstott.me.uk/gpl/)  
 *	[Java API for KML](https://code.google.com/p/javaapiforkml/),
 	Provides Java interfaces for easy access to KML (Keyhole Markup Language) data.  
 	License: [New BSD License](http://opensource.org/licenses/BSD-3-Clause)  
 *	[okde-java](https://github.com/joluet/okde-java),  
 	A Java implementation of the oKDE algorithm proposed by Matej Kristan  
	([oKDE](http://www.vicos.si/Research/Multivariate_Online_Kernel_Density_Estimation)).
