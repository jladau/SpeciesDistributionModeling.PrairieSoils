package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.Arguments;
import edu.ucsf.base.ElementaryMathOperations;
import edu.ucsf.base.FileIO;

/**
 * This code summarizes the results from modeling multiple taxa
 * @author jladau
 */
public class Main {
	
	public static void main(String rgsArgs[]){
		
		//rgsDirs = list of directories
		//arg1 = arguments object
		//lstOut = output list
		//rgsFiles = list of files
		//sDataPath = path to current data file
		//rgsFile = current file
		//s1 = current line being written to output
		
		Arguments arg1;
		String rgsDirs[]; String rgsFiles[]; String rgsFile[][];
		ArrayList<String> lstOut;
		String sDataPath; String s1;
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		
		//loading directories
		rgsDirs = FileIO.getFileList(arg1.getValueString("sIODir"));
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("ANALYSIS,NUMBER_OF_PREDICTORS,CROSS_VALIDATION_R2,PREDICTORS,FITTED_MODEL,INDEPENDENT_VALIDATION_PEARSON_R");
		
		//looping through directories
		for(int i=0;i<rgsDirs.length;i++){
		
			//checking if csv file
			if(rgsDirs[i].endsWith(".csv")){
				continue;
			}
			
			//loading current list of files
			rgsFiles = FileIO.getFileList(arg1.getValueString("sIODir") + "/" + rgsDirs[i]);
			
			//loading current data path
			sDataPath="-9999";
			for(int j=0;j<rgsFiles.length;j++){
				if(rgsFiles[j].endsWith(".data")){
					sDataPath=arg1.getValueString("sIODir") + "/" + rgsDirs[i] + "/" + rgsFiles[j];
					break;
				}
			}
			
			rgsFile = FileIO.readFile(sDataPath.replace(".data", ".modl"), ",");
			
			//continuing if null
			if(rgsFile==null){
				continue;
			}
			
			//loading current modeling output
			s1 = loadSelectedModel(rgsFile);
			
			//loading current coefficient file
			rgsFile = FileIO.readFile(sDataPath.replace(".data", ".coef"), ",");
			
			//continuing if null
			if(rgsFile==null){
				continue;
			}
			
			//loading current fitted model
			s1+="," + loadFittedModel(rgsFile);
			
			//loading independent validation r^2
			s1+="," + ElementaryMathOperations.round(findIndependentValidationCorrel(sDataPath,-0.1),5);
			
			//saving output
			lstOut.add(rgsDirs[i] + "," + s1);
		}
		
		//printing output
		FileIO.writeFile(lstOut, arg1.getValueString("sIODir") + "/Summary.csv", 0, false);
		
		//terminating
		System.out.println("Done.");
	}
	
	/**
	 * Loads selected model
	 * @return String giving the selected model
	 */
	private static String loadSelectedModel(String rgsModelFile[][]){
		
		//sOut = output
		
		String sOut;
		
		//looping through modeling file until selected model is found
		sOut="";
		for(int i=0;i<rgsModelFile.length;i++){
			if(rgsModelFile[i][3].contains("-->")){
				sOut=rgsModelFile[i][0];
				for(int j=2;j<4;j++){
					sOut+="," + rgsModelFile[i][j].replace("--> ", "");
				}
				break;
			}
		}
		
		//returning result
		return sOut;
	}
	
	private static String loadFittedModel(String rgsCoefFile[][]){
		
		//sOut = output
		//dCoeff = coefficient value
		
		String sOut;
		double dCoeff;
		
		sOut="";
		for(int i=1;i<rgsCoefFile.length-2;i++){
			dCoeff = ElementaryMathOperations.round(Double.parseDouble(rgsCoefFile[i][1]), 5);
			dCoeff=Math.abs(dCoeff);
			if(rgsCoefFile[i][0].equals("(Intercept)")){
				sOut = Double.toString(dCoeff);
			}else{
				if(rgsCoefFile[i][1].startsWith("-")){	
					sOut+= " - " + dCoeff;
				}else{
					sOut+= " + " + dCoeff;
				}
				sOut+= "*" + rgsCoefFile[i][0];
				
			}
		}
		sOut=rgsCoefFile[rgsCoefFile.length-1][0].replace("RESPONSE VARIABLE: ","") + " ~ " + sOut;
		return sOut;
	}
	
	private static double findIndependentValidationCorrel(String sDataPath, double dMESSCutoff){
		
		//rgsIndp = independent data
		//rgsData = data
		//iSampleIndp = sample column in independnet data file
		//lstSamples = list of samples occurring in data file
		//lstPredicted = list of predicted values
		//lstObserved = list of observed values
		
		String rgsIndp[][]; String rgsData[][];
		ArrayList<String> lstSamples; ArrayList<Double> lstPredicted; ArrayList<Double> lstObserved;
		
		//loading independent data
		rgsIndp = FileIO.readFile(sDataPath.replace(".data",".indp"), ",");
		
		//exiting if null
		if(rgsIndp==null){
			return -9999;
		}
		
		//loading data file
		rgsData = FileIO.readFile(sDataPath,",");
		
		//loading list of samples
		lstSamples = new ArrayList<String>();
		for(int j=0;j<rgsData[0].length;j++){
			if(rgsData[0][j].equals("SampleID")){
				for(int i=1;i<rgsData.length;i++){
					lstSamples.add(rgsData[i][j]);
				}
				break;
			}
		}
		
		//loading lists of observed and predicted values
		lstPredicted = new ArrayList<Double>();
		lstObserved = new ArrayList<Double>();
		for(int i=1;i<rgsIndp.length;i++){
			
			//******************
			//if(rgsIndp[i][0].equals("NutNet")){
			//	continue;
			//}
			//*******************
			
			//checking for output
			if(rgsIndp[i][2].equals("--") || rgsIndp[i][3].equals("--") || rgsIndp[i][4].equals("--")){
				continue;
			}
			
			//checking mess value
			if(Double.parseDouble(rgsIndp[i][2])<dMESSCutoff){
				continue;
			}
			
			//checking if sample is a training sample
			if(lstSamples.contains(rgsIndp[i][1])){
				continue;
			}
			
			//loading results
			lstPredicted.add(Double.parseDouble(rgsIndp[i][4]));
			lstObserved.add(Double.parseDouble(rgsIndp[i][3]));
		}
		
		//calculating r^2 value
		return ElementaryMathOperations.calculatePearsonCorrelation(lstPredicted, lstObserved);
	}
}