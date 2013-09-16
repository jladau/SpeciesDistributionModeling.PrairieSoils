package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.regression.*;

/**
 * Fits linear model using apache math module.
 * @author jladau
 */

public class FitLM_Apache{

	//ols1 = apache multiple regresssion object
	//mapColumn(sVariable) = returns the column in the original data matrix with the given variable
	//mapColumnX(sVariable) = returns the column in the x-matrix with the given variable
	//rgdData = data in double format
	//iRows = number of rows of data
	//rgdX = previous predictor matrix
	//rgdY = previous response matrix
	//rgsPredictors = current predictors
	//sResponse = current response
	//rgdCoefficients = current coefficients
	//rgdStErrors = standard errors of current coefficients
	
	private OLSMultipleLinearRegression ols1;
	private Map<String,Integer> mapColumn;
	private Map<String,Integer> mapColumnX;
	private double rgdData[][];
	private double[] rgdCoefficients;
	private double[] rgdStErrors;
	private int iRows;
	private double rgdX[][];
	private double rgdY[];
	private String rgsPredictors[];
	private String sResponse;
	
	/**
	 * Constructor
	 * rgsData Data in string format.
	 */
	public FitLM_Apache(String rgsData[][]){
		
		//d1 = current data value
		
		double d1;
		
		//initializing multiple regression object
		ols1 = new OLSMultipleLinearRegression();
		
		//loading data
		rgdData = new double[rgsData.length-1][rgsData[0].length];
		mapColumn = new HashMap<String,Integer>();
		for(int j=0;j<rgsData[0].length;j++){
			
			//loading column for current data name
			mapColumn.put(rgsData[0][j], j);
			
			for(int i=1;i<rgsData.length;i++){
				
				//loading data in double format
				try{
					d1 = Double.parseDouble(rgsData[i][j]);
				}catch(Exception e){
					d1=-9999;
				}
				rgdData[i-1][j]=d1;
				if(d1==-9999){
					mapColumn.remove(rgsData[0][j]);
				}
			}
		}
		
		//loading number of rows of data
		iRows = rgdData.length;
		
		//initializing variables
		rgsPredictors = new String[0];
		sResponse = "";
		
	}
	
	/**
	 * Constructor
	 * rgsData Data in string format.
	 */
	public FitLM_Apache(ArrayList<String[]> lstData){
		
		//d1 = current data value
		
		double d1;
		
		//initializing multiple regression object
		ols1 = new OLSMultipleLinearRegression();
		
		//loading data
		rgdData = new double[lstData.size()-1][lstData.get(0).length];
		mapColumn = new HashMap<String,Integer>();
		for(int j=0;j<lstData.get(0).length;j++){
			
			//loading column for current data name
			mapColumn.put(lstData.get(0)[j], j);
			
			for(int i=1;i<lstData.size();i++){
				
				//loading data in double format
				try{
					d1 = Double.parseDouble(lstData.get(i)[j]);
				}catch(Exception e){
					d1=-9999;
				}
				rgdData[i-1][j]=d1;
				if(d1==-9999){
					mapColumn.remove(lstData.get(0)[j]);
					
					//*******************
					System.out.println("removed: " + lstData.get(0)[j]);
					//*******************
					
				}
			}
		}
		
		//loading number of rows of data
		iRows = rgdData.length;
		
		//initializing variables
		rgsPredictors = new String[0];
		sResponse = "";
	}
	
	/**
	 * Constructor
	 * rgdData Data in double format
	 * mapColumn Column map
	 */
	public FitLM_Apache(ArrayList<double[]> lstData, Map<String,Integer> mapColumn){
		
		//initializing multiple regression object
		ols1 = new OLSMultipleLinearRegression();
		
		//loading data
		rgdData = new double[lstData.size()-1][lstData.get(0).length];
		this.mapColumn = mapColumn;
		for(int i=1;i<lstData.size();i++){
			
			//loading data in double array format
			rgdData[i-1]=lstData.get(i);		
		}
	
		//loading number of rows of data
		iRows = rgdData.length;
		
		//initializing variables
		rgsPredictors = new String[0];
		sResponse = "";
	}
	
	/**
	 * Checks if VIF is below threshold. Returns true if it is below threshold, false otherwise
	 * @param rgsPredictors Predictors to check.
	 * @param dThreshold Threshold.
	 * @return 
	 */
	public boolean checkVIF(double dThreshold){
		
		//d1 = threshold in R^2
		//sResponse = current response variable
		//rgdX = matrix of predictor variables
		//rgdY = response variable
		//iCol = current column
		//i1 = current predictors column
		//ols2 = regression object
		//iPredictorsCols = number of predictor columns
		
		double d1;
		String sResponse;
		int iPredictorCols; int iCol; int i1;
		double rgdX[][]; double rgdY[];
		OLSMultipleLinearRegression ols2;
		
		//loading R^2 threshold
		d1 = 1.-1./dThreshold;
		
		//checking if predictors are valid
		for(int i=0;i<rgsPredictors.length;i++){
			if(!mapColumn.containsKey(rgsPredictors[i])){
				System.out.println("ERROR: invalid predictor variable " + rgsPredictors[i] + ".");
				return true;
			}
		}
		
		//initializing regression object
		ols2 = new OLSMultipleLinearRegression();
		
		//looping through predictors
		for(int k=0;k<rgsPredictors.length;k++){
		
			//loading current response
			sResponse = rgsPredictors[k];	
			
			//loading number of predictors
			iPredictorCols=0;
			for(int j=0;j<rgsPredictors.length;j++){
							
				//checking if response variable
				if(rgsPredictors[j].equals(sResponse)){
					continue;
				}
				
				//checking if both variables allowed (e.g. different powers)
				if(rgsPredictors[j].contains(sResponse) || sResponse.contains(rgsPredictors[j])){
					continue;
				}
				
				//adding to predictor count
				iPredictorCols++;
			}
				
			//checking if at least one predictor
			if(iPredictorCols==0){
				continue;
			}
			
			//loading predictors
			rgdX = new double[iRows][iPredictorCols];
			i1=0;
			for(int j=0;j<rgsPredictors.length;j++){
				
				//checking if response variable
				if(rgsPredictors[j].equals(sResponse)){
					continue;
				}
				
				//checking if both variables allowed (e.g. different powers)
				if(rgsPredictors[j].contains(sResponse) || sResponse.contains(rgsPredictors[j])){
					continue;
				}
				
				iCol = mapColumn.get(rgsPredictors[j]);
				for(int i=0;i<iRows;i++){
					rgdX[i][i1]=rgdData[i][iCol];
				}
				i1++;
			}
			
			//loading response variable
			rgdY = new double[iRows];
			iCol = mapColumn.get(sResponse);
			for(int i=0;i<iRows;i++){
				rgdY[i]=rgdData[i][iCol];
			}
			
			//loading data
			ols2.newSampleData(rgdY, rgdX);
			
			//checking R^2
			if(ols2.calculateRSquared()>d1){
				return false;
			}
		
		}
		return true;
	}
	
	public Map<String,Integer> getColumnMap(){
		return mapColumn;
	}
	
	public double[][] findPredictors(int iRow, String[] rgsPredictors, String sResponse){
		
		//rgdX = output
		//i1 = current output column
		//iCol = current column
		
		int i1; int iCol;
		double[][] rgdX;
		
		//loading predictors
		rgdX = new double[1][rgsPredictors.length];
		i1=0;
		for(int j=0;j<rgsPredictors.length;j++){
			if(rgsPredictors[j].equals(sResponse)){
				continue;
			}
			iCol = mapColumn.get(rgsPredictors[j]);
			rgdX[0][i1]=rgdData[iRow-1][iCol];
			i1++;
		}
		
		//returning result
		return rgdX;
	}
	
	public double[][] findPredictors(int iRow){
		
		//rgdX = output
		//i1 = current output column
		//iCol = current column
		
		int i1; int iCol;
		double[][] rgdX;
		
		//loading predictors
		rgdX = new double[1][rgsPredictors.length];
		for(int j=0;j<rgsPredictors.length;j++){
			if(rgsPredictors[j].equals(sResponse)){
				continue;
			}
			iCol = mapColumn.get(rgsPredictors[j]);
			i1 = mapColumnX.get(rgsPredictors[j]);
			rgdX[0][i1]=rgdData[iRow-1][iCol];
		}
		
		//returning result
		return rgdX;
	}
	
	public double findObservation(int iRow, String sResponse){
		return rgdData[iRow-1][mapColumn.get(sResponse)];
	}
	
	public double findObservation(int iRow){
		return rgdData[iRow-1][mapColumn.get(sResponse)];
	}
	
	/**
	 * Fits model with given response variable and predictors. Data are loaded first for given model.   
	 * @param sResponse Response variable name.
	 * @param rgsPredictors Predictor names.
	 */
	public void fitModel(String sResponse, String[] rgsPredictors){
		
		//iCol = current column
		
		int iCol;
		
		//checking if predictors are valid
		for(int i=0;i<rgsPredictors.length;i++){
			if(!mapColumn.containsKey(rgsPredictors[i])){
				System.out.println("ERROR: invalid predictor variable " + rgsPredictors[i] + ".");
				return;
			}
		}
		
		//checking if response is valid
		if(!mapColumn.containsKey(sResponse)){
			System.out.println("ERROR: invalid response variable.");
			return;
		}
		
		//loading predictor matrix
		if(rgsPredictors.length==this.rgsPredictors.length){
			
			//loading predictors
			for(int j=0;j<rgsPredictors.length;j++){
				if(rgsPredictors[j].equals(this.rgsPredictors[j])){
					continue;
				}
				iCol = mapColumn.get(rgsPredictors[j]);
				mapColumnX.put(rgsPredictors[j], j);
				for(int i=0;i<iRows;i++){
					rgdX[i][j]=rgdData[i][iCol];
				}
			}
			
		}else{
			
			//initializing
			rgdX = new double[iRows][rgsPredictors.length];
			mapColumnX = new HashMap<String,Integer>();
			
			//loading predictors
			for(int j=0;j<rgsPredictors.length;j++){
				iCol = mapColumn.get(rgsPredictors[j]);
				for(int i=0;i<iRows;i++){
					rgdX[i][j]=rgdData[i][iCol];
				}
				mapColumnX.put(rgsPredictors[j], j);
			}
		}
		
		//loading response variable
		if(!sResponse.equals(this.sResponse)){
			rgdY = new double[iRows];
			iCol = mapColumn.get(sResponse);
			for(int i=0;i<iRows;i++){
				rgdY[i]=rgdData[i][iCol];
			}
		}
		
		//updating variables
		this.sResponse = sResponse;
		this.rgsPredictors = rgsPredictors;
		
		//loading data
		this.loadData(rgdX, rgdY);
	}
	
	/**
	 * Finds R^2 of current model.
	 * @return R^2 value
	 */
	public double findRSquared(){
		return ols1.calculateRSquared();
	}
	
	/**
	 * Finds Adjusted R^2 of current model.
	 * @return Adjusted R^2 value
	 */
	public double findAdjustedRSquared(){
		return ols1.calculateAdjustedRSquared();
	}
	
	/**
	 * Finds the total sum of squares
	 * @param sResponse Response variable
	 * @return Total sum of squares
	 */
	public double findTSS(String sResponse){
		
		//iCol = current column
		//dMean = mean value
		//dOut = output
		
		double dMean; double dOut;
		int iCol;
		
		//checking if response is valid
		if(!mapColumn.containsKey(sResponse)){
			System.out.println("ERROR: invalid response variable.");
			return -9999;
		}
		
		//loading response variable
		iCol = mapColumn.get(sResponse);
		dMean = 0;
		for(int i=0;i<iRows;i++){
			dMean+=rgdData[i][iCol];
		}
		dMean=dMean/((double) rgdData.length);
		dOut = 0;
		for(int i=0;i<iRows;i++){
			dOut+=Math.pow(rgdData[i][iCol]-dMean,2);
		}
		return dOut;
	}
	
	/**
	 * Finds predicted values for new set of predictors
	 * @param rgdX Set of new predictors.
	 * @return
	 */
	public double[] findPrediction(double rgdX[][]){
		
		//rgd1 = estimated coefficients
		//rgdOut = output
		
		double rgd1[]; double rgdOut[];
		
		//loading estimated coefficients
		rgd1 = ols1.estimateRegressionParameters();
		
		//loading output
		rgdOut = new double[rgdX.length];
		for(int i=0;i<rgdX.length;i++){
			rgdOut[i]=rgd1[0];
			for(int j=0;j<rgdX[0].length;j++){
				rgdOut[i]+=rgdX[i][j]*rgd1[j+1];
			}
		}
		
		//outputting results
		return rgdOut;
	}
	
	/**
	 * Finds prediction for given list of predictors.
	 * @param lstPredValues predictor values.
	 * @return predicted values.
	 */
	
	public double[] findPrediction(ArrayList<String> lstPredValues){
		
		//rgdOut = output
		//rgdX = values of predictors
		//i1 = current output column
		//rgs1 = current predictors in split format
		//rgs2 = current predictor
		
		double rgdOut[]; double rgdX[][];
		int i1;
		String rgs1[]; String rgs2[];
		
		//initializing predictor matrix
		rgdX = new double[lstPredValues.size()][rgsPredictors.length];
		
		//looping through predictors
		for(int i=0;i<lstPredValues.size();i++){
			
			//looping through predictors
			rgs1 = lstPredValues.get(i).split(",");
			for(int k=0;k<rgs1.length;k++){
				rgs2 = rgs1[k].split(":");
				i1 = mapColumnX.get(rgs2[0]);
				rgdX[i][i1]=Double.parseDouble(rgs2[1]);
			}	
		}
		
		//loading output
		rgdOut = new double[rgdX.length];
		for(int i=0;i<rgdX.length;i++){
			rgdOut[i]=rgdCoefficients[0];
			for(int j=0;j<rgdX[0].length;j++){
				rgdOut[i]+=rgdX[i][j]*rgdCoefficients[j+1];
			}
		}
		
		//outputting results
		return rgdOut;
	}
	
	/**
	 * Finds prediction for given predictors
	 * @param sPredValues predictor values.
	 * @return predicted values.
	 */
	
	public double findPrediction(String sPredValues){
		
		//dOut = output
		//rgdX = values of predictors
		//i1 = current output column
		//rgs1 = current predictors in split format
		//rgs2 = current predictor
		
		double dOut; 
		double rgdX[][];
		int i1;
		String rgs1[]; String rgs2[];
		
		//initializing predictor matrix
		rgdX = new double[1][rgsPredictors.length];
		
		//looping through predictors
		rgs1 = sPredValues.split(",");
		for(int k=0;k<rgs1.length;k++){
			rgs2 = rgs1[k].split(":");
			i1 = mapColumnX.get(rgs2[0]);
			rgdX[0][i1]=Double.parseDouble(rgs2[1]);
		}	
		
		//loading output
		dOut = 0;
		dOut=rgdCoefficients[0];
		for(int j=0;j<rgdX[0].length;j++){
			dOut+=rgdX[0][j]*rgdCoefficients[j+1];
		}
		
		//outputting results
		return dOut;
	}
	
	/**
	 * Prints fitted model.
	 * @return Fitted model.
	 */
	public ArrayList<String> printFittedModel(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		//loading standard errors if necessary
		if(rgdStErrors==null){
			rgdStErrors=ols1. estimateRegressionParametersStandardErrors();
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		
		//coefficient values
		lstOut.add("PREDICTOR,COEFFICIENT ESTIMATE,STANDARD ERROR");
		lstOut.add("(Intercept)," + rgdCoefficients[0] + "," + rgdStErrors[0]);
		for(int j=0;j<rgsPredictors.length;j++){
			lstOut.add(rgsPredictors[j] + "," + rgdCoefficients[j+1] + "," + rgdStErrors[j+1]);
		}
		lstOut.add("");
		lstOut.add("RESPONSE VARIABLE: " + sResponse);
		
		//returning result
		return lstOut;
	}
	
	public double findPRESS(){
		
		//rgdResiduals = residuals
		//rgmHat = hat matrix
		//d1 = output
		//d2 = current hat matrix value
		
		double rgdResiduals[];
		RealMatrix rgmHat;
		double d1; double d2;
		
		//loading residuals and hat matrix
		rgdResiduals = ols1.estimateResiduals();
		rgmHat = ols1.calculateHat();
		
		//loading output
		d1 = 0;
		for(int i=0;i<rgdResiduals.length;i++){
			d2 = rgmHat.getEntry(i, i);
			if(d2==1){
				if(Math.abs(rgdResiduals[i])>0.000000001){
					System.out.println("ERROR: hat matrix entry equal to 1.");
				}
			}else{
				d1+=Math.pow(rgdResiduals[i]/(1.-d2),2);
			}
		}
		
		//returning result
		return d1;
	}

	public void loadCoefficients(){
		this.rgdCoefficients = ols1.estimateRegressionParameters();
		this.rgdStErrors = null;
	}
	
	/**
	 * Loads predictor variables
	 * @param rgdX Predictor variables.
	 * @param rgdY Response variables.
	 */
	private void loadData(double rgdX[][], double rgdY[]){
		ols1.newSampleData(rgdY, rgdX);
	}
}
