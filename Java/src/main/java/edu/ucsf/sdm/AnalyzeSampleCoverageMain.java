package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.*;

/**
 * This code runs diagnostics to check sample coverage for beta-diversity mapping.
 * @author jladau
 */
public class AnalyzeSampleCoverageMain {
	public static void main(String rgsArgs[]){
		
		//sdm1 = SDM object
		//cdfWriter = output cdf
		//rgsProportion = proportion below threshold output
		//lst1 = list of output strings
		//dMinAreaCoverage = minimum area of coverage
		//dMaxProportion = maximum proportion
		//dValue = current test value
		//arg1 = arguments
		
		Arguments arg1;
		SDM sdm1 = null;
		double dMinAreaCoverage; double dMaxProportion; double dValue;
		ArrayList<String> lst1;
		NetCDF_IO cdfWriter = null;
		String rgsProportion[][];
		
		//loading arguments
		arg1 = new Arguments(rgsArgs);
		
		//initializing sdm object
		if(arg1.getValueString("sAnalysisMode").equals("beta-diversity")){
			sdm1 = new SDM_BetaDiversity();
		}else if(arg1.getValueString("sAnalysisMode").equals("alpha-diversity")){
			sdm1 = new SDM_AlphaDiversity();
		}
		
		//loading arguments
		for(String s:arg1.mapAllArguments.keySet()){
			sdm1.arg1.loadArgument(s,arg1.mapAllArguments.get(s));
		}
		
		//waiting for completion of data file
		//FileIO.checkAndWaitForCompletion(sdm1.arg1.sPathData);
		
		//loading observations object
		sdm1.loadObservationalData();
		sdm1.obs1.loadRanges();
		
		//loading RasterData object
		sdm1.loadRasterData();
		
		//loading mess object
		sdm1.loadMESS();
		
		//initializing output
		rgsProportion = new String[1][1];
		rgsProportion[0][0]="VARIABLE,MIN_AREA_NOT_COVERED,MAX_PROPORTION_BELOW_THRESHOLD";
		//FileIO.writeFile(rgsProportion, sdm1.arg1.sDirOutput + "/MESSProportion.csv", ",", 0, false);
		//FileIO.writeFile(rgsProportion, arg1.getValueString("sPathArguments").replace(".args",".mess"), ",", 0, false);
		FileIO.writeFile(rgsProportion, sdm1.arg1.sMESSPath, ",", 0, false);
		
		//looping through sets of predictors (models)
		//for(int i=0;i<sdm1.arg1.iModels;i++){
		for(int i=0;i<sdm1.arg1.rgsPredictors.length;i++){
		
			//initializing writer
			if(arg1.getValueBoolean("bMESSPlots")==true){
				cdfWriter = new NetCDF_IO(sdm1.arg1.sDirOutput + "/MESS_" + sdm1.arg1.rgsCandidatePredictors[i] + ".nc","writing");
				cdfWriter.initializeWriter(0.5, "Meters", sdm1.arg1.lstElevationsCDF, "Month", sdm1.arg1.lstTimesCDF, "MESS", "");
			}
				
			//updating progress
			System.out.println("Variable: " + sdm1.arg1.rgsPredictors[i]);
			
			//initializing output list
			lst1 = new ArrayList<String>();
			
			//looping through times
			for(int k=0;k<sdm1.arg1.lstTimes.size();k++){
				
				//loading current mess grid
				sdm1.mss1.loadMESSMap(k,i);
				
				//writing grid
				if(arg1.getValueBoolean("bMESSPlots")==true){
					cdfWriter.writeGrid(sdm1.mss1.rgdMap, sdm1.arg1.lstElevations.get(k), sdm1.arg1.lstTimes.get(k));
				}
				
				//updating progress
				System.out.println("Analyzing time " + (k+1) + " of " + sdm1.arg1.lstTimes.size() + "...");
				
				//loading result
				rgsProportion = RasterOperations.findProportionBelowThreshold(sdm1.arg1.rgsPredictors[i], k, sdm1.mss1.rgdMap, arg1.getValueString("sLocation"), -20, sdm1.arg1.sPathGlobalTopography);
				
				//saving result
				lst1.add(rgsProportion[1][0]);
				
				//checking if static variable
				if(!sdm1.arg1.rgsPredictors[0].contains("Momean")){
					break;
				}
			}
			
			//finding extremes
			dMinAreaCoverage = 9999; dMaxProportion = -9999;
			for(int k=0;k<lst1.size();k++){
				
				//checking area covered
				dValue = Double.parseDouble(lst1.get(k).split(",")[3])/Double.parseDouble(lst1.get(k).split(",")[5]);
				if(dValue<dMinAreaCoverage){
					dMinAreaCoverage = dValue;
				}
				
				//checking proportion below threshold
				dValue = Double.parseDouble(lst1.get(k).split(",")[4]);
				if(dValue>dMaxProportion){
					dMaxProportion = dValue;
				}
			}
			
			//outputting results
			//rgsProportion = new String[2][1];
			//rgsProportion[0][0]="VARIABLE,MIN_AREA_NOT_COVERED,MAX_PROPORTION_BELOW_THRESHOLD";
			//rgsProportion[1][0]=sdm1.arg1.lstSuffixes.get(i) + "," + dMinAreaCoverage + "," + dMaxProportion;
			//FileIO.writeFile(rgsProportion, sdm1.arg1.sDirOutput + "/MESSProportion_" + sdm1.arg1.lstSuffixes.get(i) + ".csv", ",", 0, false);
			rgsProportion = new String[1][1];
			rgsProportion[0][0]=sdm1.arg1.rgsPredictors[i] + "," + dMinAreaCoverage + "," + dMaxProportion;
			//FileIO.writeFile(rgsProportion, sdm1.arg1.sDirOutput + "/MESSProportion.csv", ",", 0, true);
			FileIO.writeFile(rgsProportion, sdm1.arg1.sMESSPath, ",", 0, true);
			
			//closing writer
			if(arg1.getValueBoolean("bMESSPlots")==true){
				cdfWriter.closeWriter();
			}
		}
		
		//terminating
		//FileIO.writeCompletionFile(sdm1.arg1.sMESSPath);
		arg1.printArguments(sdm1.arg1.sPathLog, true, "AnalyzeSampleCoverage");
		System.out.println("Done.");
	}
}
