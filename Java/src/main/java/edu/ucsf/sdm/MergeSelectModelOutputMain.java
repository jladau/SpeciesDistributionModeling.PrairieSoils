package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import edu.ucsf.base.*;

/**
 * This code merges files created by running SelectModelFast on the cluster
 * @author jladau
 */


public class MergeSelectModelOutputMain {
	public static void main(String rgsArgs[]){
		
		//mapResults(sTaxon) = returns an array with the results for the given taxon
		//sTaxon = current taxon
		//rgsFiles = list of files
		//rgsData = current data array
		//rgs1 = current data array
		//sTotalSequences = total number of sequences (for logit models)
		//rgsOut = output
		//iCol = current output column
		//arg1 = arguments
		//sOutPath = output path
		//rgsData = data file (for finding number of sequences)
		//bVars = checking if vars file is modified
		//sInputDir = input directory
		
		Map<String,String[][]> mapResults;
		String rgsOut[][]=null; String rgsFiles[]; String rgsData[][]=null; String rgs1[][];
		String sTaxon; String sTotalSequences="-9999"; String sOutPath=null; String sInputDir;
		int iCol;
		Arguments arg1;
		boolean bVars=false;
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		
		//loading total number of sequences
		rgsData = FileIO.readFile(arg1.getValueString("sDataPath"), ",");
		
		//waiting until all files are complete
		FileIO.checkAndWaitForCompletion(arg1.getValueString("sDataPath").replace(".data",".modl"), 1, arg1.getValueInt("iTotalTasks"));
		
		//loading list of files and input directory
		sInputDir = FileIO.getFileDirectory(arg1.getValueString("sDataPath"));
		rgsFiles = FileIO.getFileList(sInputDir);
		
		//initializing data map
		mapResults = new HashMap<String,String[][]>();
		for(int i=0;i<rgsFiles.length;i++){
			
			//checking if variables file
			if(rgsFiles[i].contains(".vars_") && !rgsFiles[i].endsWith(".complete")){
				if(bVars==false){
					rgsData = FileIO.readFile(sInputDir + "/" + rgsFiles[i], "jhkfhasjdfhslflksf");
					FileIO.writeFile(rgsData, sInputDir + "/" + rgsFiles[i].split("_")[0], "", 0, false);
				}
				
				//deleting file
				FileIO.deleteFile(sInputDir + "/" + rgsFiles[i]);
			}
				
			//checking if correct type of file
			if(!rgsFiles[i].contains(".modl_") || rgsFiles[i].endsWith(".complete")){
				continue;
			}
		
			//initializing output file if necessary
			if(sOutPath==null){
				sOutPath=sInputDir + "/" + rgsFiles[i].split("_")[0];
			}
			
			
			//loading data and name of current taxon/response variable
			rgsData = FileIO.readFile(sInputDir + "/" + rgsFiles[i], ",");
			sTaxon = rgsData[1][0];
			rgsData = reorganizeColumns(rgsData);
			
			//deleting file
			FileIO.deleteFile(sInputDir + "/" + rgsFiles[i]);
			
			//converting model names
			if(sTaxon.startsWith("L")){
				for(int k=1;k<rgsData.length;k++){
					if(!sTotalSequences.equals("-9999")){
						rgsData[k][0]=rgsData[k][0].replace(sTaxon, "I(log((" + sTaxon.replaceFirst("L", "") + "+1/" + sTotalSequences + ")/(1-" + sTaxon.replaceFirst("L", "") + "-1/" + sTotalSequences + ")))");
					}else{
						rgsData[k][0]=rgsData[k][0].replace(sTaxon, "I(log(" + sTaxon.replaceFirst("L", "") + "/(1-" + sTaxon.replaceFirst("L", "") + ")))");
					}
				}
			}
			
			//checking if taxon initialized
			if(!mapResults.containsKey(sTaxon)){
				
				//initializing
				mapResults.put(sTaxon, rgsData);
			}else{
				
				//***********************
				//if(rgsFiles[i].equals("SelectModel_Output_PcoA1_0.csv")){
				//	System.out.println("HERE");
				//}
				//***********************
				
				//updating
				rgs1 = mapResults.get(sTaxon);
				for(int k=1;k<rgs1.length;k++){
					if(Double.parseDouble(rgsData[k][3])!=-9999){	
						if(Double.parseDouble(rgs1[k][3])==-9999){
							for(int j=0;j<4;j++){
								rgs1[k][j]=rgsData[k][j];
							}
						}else{
							if(rgs1[k][2].equals("PRESS")){
								if(Double.parseDouble(rgsData[k][3])<Double.parseDouble(rgs1[k][3])){
									for(int j=0;j<4;j++){
										rgs1[k][j]=rgsData[k][j];
									}	
								}
							}else{
								if(Double.parseDouble(rgsData[k][3])>Double.parseDouble(rgs1[k][3])){
									for(int j=0;j<4;j++){
										rgs1[k][j]=rgsData[k][j];
									}	
								}
							}
						}
					}
				}
				mapResults.put(sTaxon,rgs1);	
			}
		}
		
		//outputting results
		iCol=2;
		for(String s:mapResults.keySet()){
			rgs1 = insertCVRSquared(mapResults.get(s));
			if(rgsOut==null){
				rgsOut = new String[rgs1.length][2+2*mapResults.size()];
				for(int i=0;i<rgs1.length;i++){
					for(int j=1;j<3;j++){
						rgsOut[i][j-1]=rgs1[i][j];
					}
				}
			}
			rgsOut[0][iCol]="VALUE_" + s;
			rgsOut[0][iCol+1]="MODEL_" + s;
			for(int i=1;i<rgs1.length;i++){
				if(!rgsOut[i][0].equals(rgs1[i][1])){
					System.out.println("ERROR");
				}
				if(!rgsOut[i][1].equals(rgs1[i][2])){
					System.out.println("ERROR");
				}
				rgsOut[i][iCol]=rgs1[i][3];
				rgsOut[i][iCol+1]=rgs1[i][0];
			}
			iCol+=2;
		}
		
		//outputting results
		FileIO.writeFile(rgsOut, sOutPath, ",", 0, false);
		
		//terminating
		FileIO.writeCompletionFile(sOutPath);
		arg1.printArguments(arg1.getValueString("sDataPath").replace(".data", ".log"), true, "MergeSelectModelOutput");
		System.out.println("Done.");
	}

	/**
	 * Inserts cross validation r^2 based on the PRESS statistic
	 * @param rgsOutput
	 */
	
	private static String[][] insertCVRSquared1(String rgsOutput[][]){
		
		//rgs1 = output
		//iRow = current output row
		//dCVRSquaredCurrent = current cross validation r^2
		//dCVRSquaredPrevious = previous cross validation r^2
		//iCVRSquaredPrevious = row for previous cross validation r^2
		//b1 = flag for whether model has been selected
		
		String rgs1[][];
		int iRow; int iCVRSquaredPrevious;
		double dCVRSquaredCurrent; double dCVRSquaredPrevious;
		boolean b1;
		
		//initializing output
		rgs1 = new String[rgsOutput.length + (rgsOutput.length-1)/3][4];
		rgs1[0]=rgsOutput[0];
		iRow=0;
		
		//looping through output
		dCVRSquaredPrevious = 0.;
		iCVRSquaredPrevious = -9999;
		b1=false;
		for(int i=1;i<rgsOutput.length;i++){
			
			iRow++;
			rgs1[iRow]=rgsOutput[i];
			if(rgsOutput[i][3].equals("PRESS")){
				iRow++;
				if(Double.parseDouble(rgsOutput[i][4])!=-9999){
					for(int j=0;j<3;j++){
						rgs1[iRow][j]=rgsOutput[i][j];
						
					}
					rgs1[iRow][3]="Cross Validation R^2";
					rgs1[iRow][4]=Double.toString(1.-Double.parseDouble(rgsOutput[i][4])/Double.parseDouble(rgsOutput[1][4]));
					dCVRSquaredCurrent = 1.-Double.parseDouble(rgsOutput[i][4])/Double.parseDouble(rgsOutput[1][4]);
					if(dCVRSquaredCurrent - dCVRSquaredPrevious<0.025 && i!=1 && b1==false){
						rgs1[iCVRSquaredPrevious][1]="--> " + rgs1[iCVRSquaredPrevious][1];
						b1=true;
					}
					dCVRSquaredPrevious = dCVRSquaredCurrent;
					iCVRSquaredPrevious=iRow;
				}else{
					rgs1[iRow]=rgsOutput[i];
					rgs1[iRow][2]="Cross Validation R^2";
				}
			}
		}
		
		//selecting last model if no model selected
		if(b1==false){
			for(int i=rgs1.length-1;i>=0;i--){
				if(rgs1[i][3].equals("Cross Validation R^2")){
					rgs1[i][1]="--> " + rgs1[i][1];
					break;
				}
			}
		}
		
		//returning result
		return rgs1;
	}
	
	private static String[][] reorganizeColumns(String rgsData[][]){
		
		//rgsOut = output
		
		String rgsOut[][];
		
		//initializing output
		rgsOut = new String[rgsData.length][rgsData[0].length];
		
		//shuffling columns
		for(int i=0;i<rgsData.length;i++){
			
			for(int j=1;j<5;j++){
				rgsOut[i][j-1]=rgsData[i][j];
			}
			rgsOut[i][4]=rgsData[i][0];
		}
		
		//returning result
		return rgsOut;
		
	}
	
	
	/**
	 * Inserts cross validation r^2 based on the PRESS statistic
	 * @param rgsOutput
	 */
	
	private static String[][] insertCVRSquared(String rgsOutput[][]){
		
		//rgs1 = output
		//iRow = current output row
		//dCVRSquaredCurrent = current cross validation r^2
		//dCVRSquaredPrevious = previous cross validation r^2
		//iCVRSquaredPrevious = row for previous cross validation r^2
		//b1 = flag for whether model has been selected
		
		String rgs1[][];
		int iRow; int iCVRSquaredPrevious;
		double dCVRSquaredCurrent; double dCVRSquaredPrevious;
		boolean b1;
		
		//initializing output
		rgs1 = new String[rgsOutput.length + (rgsOutput.length-1)/3][4];
		rgs1[0]=rgsOutput[0];
		iRow=0;
		
		//looping through output
		dCVRSquaredPrevious = 0.;
		iCVRSquaredPrevious = -9999;
		b1=false;
		for(int i=1;i<rgsOutput.length;i++){
			
			iRow++;
			for(int j=0;j<4;j++){
				rgs1[iRow][j]=rgsOutput[i][j];
			}
			if(rgsOutput[i][2].equals("PRESS")){
				iRow++;
				if(Double.parseDouble(rgsOutput[i][3])!=-9999){
					for(int j=0;j<2;j++){
						rgs1[iRow][j]=rgsOutput[i][j];
						
					}
					rgs1[iRow][2]="Cross Validation R^2";
					rgs1[iRow][3]=Double.toString(1.-Double.parseDouble(rgsOutput[i][3])/Double.parseDouble(rgsOutput[1][3]));
					dCVRSquaredCurrent = 1.-Double.parseDouble(rgsOutput[i][3])/Double.parseDouble(rgsOutput[1][3]);
					if(dCVRSquaredCurrent - dCVRSquaredPrevious<0.025 && i!=1 && b1==false){
						rgs1[iCVRSquaredPrevious][0]="--> " + rgs1[iCVRSquaredPrevious][0];
						b1=true;
					}
					dCVRSquaredPrevious = dCVRSquaredCurrent;
					iCVRSquaredPrevious=iRow;
				}else{
					for(int j=0;j<4;j++){
						rgs1[iRow][j]=rgsOutput[i][j];
						rgs1[iRow][2]="Cross Validation R^2";
					}
				}
			}
		}
		
		//selecting last model if no model selected
		if(b1==false){
			for(int i=rgs1.length-1;i>=0;i--){
				if(rgs1[i][2].equals("Cross Validation R^2")){
					rgs1[i][0]="--> " + rgs1[i][0];
					break;
				}
			}
		}
		
		//returning result
		return rgs1;
	}
}
