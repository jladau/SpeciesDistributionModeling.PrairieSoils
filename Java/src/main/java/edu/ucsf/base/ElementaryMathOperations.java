package edu.ucsf.base;

import java.util.ArrayList;

/**
 * Implements elementary math operations
 * @author jladau
 */

public class ElementaryMathOperations {

	/**
	 * Rounds given number to the specified number of places
	 * @param dValue Value to be rounded
	 * @param dPlaces Places after the desimal to round to
	 * @return Rounded number
	 */
	public static double round(double dValue,double dPlaces){
		
		//d1 = multiplier/divisor
		//dOut = output
		
		double d1; double dOut;
		
		d1 = Math.pow(10., dPlaces);
		dOut = dValue*d1;
		dOut = Math.round(dOut);
		dOut = dOut/d1;
		return dOut;
	}
	
	/**
	 * Calculates R^2 value
	 * @param lstPredicted List of predicted values
	 * @param lstObserved List of observed values
	 * @return R^2
	 */
	public static double calculateR2(ArrayList<Double> lstPredicted, ArrayList<Double> lstObserved){
		
		//dSSError = sum of squares due to error
		
		double dSSError;
		
		//loading sum of squares due to error
		dSSError = 0;
		for(int i=0;i<lstPredicted.size();i++){
			dSSError+=Math.pow(lstPredicted.get(i)-lstObserved.get(i), 2.);
		}
		
		//returning result
		return 1.-dSSError/calculateSumSquares(lstObserved);
	}
	
	/**
	 * Calculates the correlation between two data sets
	 * @param lstX X-values
	 * @param lstY Y-Values
	 * @return R
	 */
	public static double calculatePearsonCorrelation(ArrayList<Double> lstX, ArrayList<Double> lstY){
		return calculateSumSquares(lstX,lstY)/(Math.sqrt(calculateSumSquares(lstX))*Math.sqrt(calculateSumSquares(lstY)));
	}
	
	/**
	 * Calculates the mean of a list of values
	 * @param lst1 List of values
	 * @return Mean
	 */
	public static double calculateMean(ArrayList<Double> lst1){
		
		//d1 = total
		
		double d1;
		
		d1=0;
		for(int i=0;i<lst1.size();i++){
			d1+=lst1.get(i);
		}
		return d1/((double) lst1.size());
	}
	
	
	
	/**
	 * Calculates the sum of squares of a set of observations.
	 * @param lst1 Observations
	 * @return Sum of squares
	 */
	public static double calculateSumSquares(ArrayList<Double> lst1){
		
		//dS1 = sum of observations
		//dS2 = sum of observations squared
		//dN = total number of observations
		
		double dS1; double dS2; double dN;
		
		dN = (double) lst1.size();
		dS1=0; dS2=0;
		for(int i=0;i<lst1.size();i++){
			dS1+=lst1.get(i);
			dS2+=lst1.get(i)*lst1.get(i);
		}
		return dS2-dS1*dS1/dN;
	}
	
	/**
	 * Calculates the sum of squares of a set of observations (S1*S2).
	 * @param lst1 first set of observations
	 * @param lst2 second set of observations
	 * @return Sum of squares
	 */
	public static double calculateSumSquares(ArrayList<Double> lst1, ArrayList<Double> lst2){
		
		//dS1 = sum of observations from list 1
		//dS2 = sum of observations from list 2
		//dS12 = sum of product of observations from two lists
		//dN = total number of observations
		
		double dS1; double dS2; double dS12; double dN;
		
		dN = (double) lst1.size();
		dS1=0; dS2=0; dS12 = 0;
		for(int i=0;i<lst1.size();i++){
			dS1+=lst1.get(i);
			dS2+=lst2.get(i);
			dS12+=lst1.get(i)*lst2.get(i);
		}
		return dS12-dS1*dS2/dN;
	}
}
