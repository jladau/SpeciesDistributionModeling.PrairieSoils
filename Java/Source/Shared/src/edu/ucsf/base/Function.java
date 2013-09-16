package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Applies specified function
 * @author jladau
 *
 */

public class Function {

	//sFunction = name of function
	//mapPolynomial(dDegree) = returns coefficient for given degree of polynomial
	//mapPolnomialDerivative(dDegree) = returns coefficient for given degree of derviative of polynomial
	//dPolynomialDegree = degree of polynomial (for polynomial functions only)
	
	private double dPolynomialDegree = -9999;
	private String sFunction;
	private Map<Double,Double> mapPolynomial = null;
	private Map<Double,Double> mapPolynomialDerivative = null;
	
	/**
	 * Constructor
	 * @param sFunction Function to apply.
	 */
	public Function(String sFunction){
		this.sFunction = sFunction;
	}
	
	/**
	 * Loads polynomial term
	 * @param dDegree Degree of term
	 * @param dCoefficient Coefficient for term
	 */
	public void loadPolynomialTerm(double dDegree, double dCoefficient){
		
		//initializing maps if appropriate
		if(mapPolynomial==null){
			mapPolynomial = new HashMap<Double,Double>();
			mapPolynomialDerivative = new HashMap<Double,Double>();
		}
		
		//loading terms
		mapPolynomial.put(dDegree, dCoefficient);
		if(dDegree>=1){
			mapPolynomialDerivative.put(dDegree-1., dCoefficient*dDegree);
		}
		
		//updating degree of polynomial
		if(dDegree>dPolynomialDegree){
			dPolynomialDegree=dDegree;
		}
	}

	public Set<Double> solveAll(double dInitialGuessMin, double dInitialGuessMax, double dInitialGuessStep, double dRoundDigitsAfterZero, double dTolerance){
		
		//dSolution = current solution
		//set1 = output
		
		double dSolution;
		Set<Double> set1;
		
		//initializing output set
		set1 = new HashSet<Double>();
		
		//looping through initial guesses
		for(double d=dInitialGuessMin; d<=dInitialGuessMax; d+=dInitialGuessStep){
			
			//loading current solution
			dSolution = this.solve(d, dTolerance);
			
			//checking for error
			if(dSolution==-9999){
				continue;
			}
			
			//rounding
			dSolution = this.round(dSolution, dRoundDigitsAfterZero);
			
			//updating set of values if appropriate
			set1.add(dSolution);
			
			//checking if all solutions have been found
			if(set1.size()==dPolynomialDegree){
				return set1;
			}
			
		}
		
		//returning result
		return set1;
	}
	
	private double round(double dValue, double dDigitsAfterZero){
		
		//d1 = output
		//d2 = power of ten to use
		
		double d2; double d1;
		
		d2 = Math.pow(10.,dDigitsAfterZero);
		d1 = dValue*d2;
		d1 = Math.round(d1);
		d1 = d1/d2;
		return d1;
	}
	
	/**
	 * Finds root of function using the Newton-Raphson method
	 * @param dInitialGuess Starting point.
	 * @param dTolerance Tolerance of difference of function from zero
	 * @return Root or -9999 if no root found
	 */
	public double solve(double dInitialGuess,double dTolerance){
		
		//dX = current value of argument
		//dF = current value of function
		//dFPrime = current value of derivative
		
		double dX; double dF; double dFPrime;
		
		//loading initial values
		dX = dInitialGuess;
		dF = this.applyFcn(dX);
		
		for(int i=0;i<1000;i++){
		
			//loading value of derivative
			dFPrime = this.applyFcnDerivative(dX);
			
			if(dFPrime==0){
				return -9999;
			}
			
			//loading new value of argument
			dX = dX-dF/dFPrime;
		
			//loading new value of function
			dF = this.applyFcn(dX);
			
			//checking if done
			if(Math.abs(dF)<=dTolerance){
				return dX;
			}
		}
		
		//returning error: no root found in 1000 iterations
		return -9999;
	}
	
	/**
	 * Applies function
	 * @param d1 Function argument
	 * @return Value of function.
	 */
	public double applyFcn(double d1){
		
		//dOut = output
		
		double dOut;
		
		if(sFunction.equals("Polynomial")){
			dOut = 0;
			for(double d:mapPolynomial.keySet()){
				dOut+=mapPolynomial.get(d)*Math.pow(d1, d);
			}
			return dOut;
		}else{
			return -9999;
		}
	}

	/**
	 * Applies function derivative
	 * @param d1 Function argument
	 * @return Value of derivative.
	 */
	public double applyFcnDerivative(double d1){
		
		//dOut = output
		
		double dOut;
		
		if(sFunction.equals("Polynomial")){
			dOut = 0;
			for(double d:mapPolynomialDerivative.keySet()){
				dOut+=mapPolynomialDerivative.get(d)*Math.pow(d1, d);
			}
			return dOut;
		}else{
			return -9999;
		}
	}
	
	/**
	 * Applies function
	 * @param d1 Function argument 1
	 * @param d2 Function argument 2
	 * @return Value of function
	 */
	public double applyFcn(double d1, double d2){
		
		//checking for error
		if(d1==-9999 || d2==-9999){
			return -9999;
		}
		
		if(sFunction.equals("AbsDiff")){
			return Math.abs(d1-d2);
		}else if(sFunction.equals("Max")){
			return Math.max(d1,d2);
		}else if(sFunction.equals("Min")){
			return Math.min(d1,d2);
		}else if(sFunction.equals("Diff")){
			return d2-d1;
		}else{
			return -9999;
		}
	}
}