package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.FileIO;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public abstract class SDMArguments {
	
	//rgsCandidatePredictors = list of candidate predictors (for sample coverage analysis)
	//iMaxVars = maximum number of covariates in model selection
	//sTransform = transform to use
	//sDirRasters = directory with rasters
	//sDirOutput = output directory
	//sPathData = path to data
	//sPathIndependentData = path to independent data set
	//sPathIndependentValidation = path to independent validation
	//sPathCoefficients = path to file with coefficients
	//sPathLog = path to log file
	//rgsPredictors = predictors (string array format)
	//lstPredictorFiles = list of predictor files
	//sResponse = response variable
	//lstElevationsCDF = list of elevations (no duplicates: for initializing cdf writer)
	//lstTimesCDF = list of times (no duplicates: for initializing cdf writer)
	//lstElevations = list of elevations
	//lstTimes = list of times
	//lstVert = list of elevation values
	//lstTime = list of time values
	//rgsFiles = array of files
	//dMESSCutoff = maximum tolerable proportion of area with mess score less than -20
	
	public String[] rgsCandidatePredictors;
	public int iMaxVars;
	public String sTransform;
	public String sDirRasters;
	public String sDirOutput;
	public String sPathData;
	public String sPathIndependentData;
	public String sPathIndependentValidation;
	public String sPathGlobalTopography;
	public String sPathCoefficients;
	public String sPathLog;
	public String[] rgsPredictors; 
	public ArrayList<String> lstPredictorFiles;
	public String sResponse;
	public ArrayList<Double> lstElevationsCDF; 
	public ArrayList<Double> lstTimesCDF;
	public ArrayList<Double> lstElevations; 
	public ArrayList<Double> lstTimes;
	public String[] rgsFiles;
	public ArrayList<Double> lstTime;
	public ArrayList<Double> lstVert;
	public String sLocation;
	public String sMESSPath;
	public String sModelPath;
	public String sVarsPath;
	public String sMapPath;
	public String sCrossvalidationPath;
	public double dMESSCutoff = 0.005;
	
	/**
	 * Constructor
	 */
	public SDMArguments(){
		
		//initializing variable lists
		lstPredictorFiles = new ArrayList<String>();
		lstTime = new ArrayList<Double>();
		lstVert = new ArrayList<Double>();
	}
	
	/**
	 * Loads argument (value is string)
	 * @param sName Name of argument
	 * @param sValue Value of argument
	 */
	public void loadArgument(String sName, String sValue){
		
		//rgs1 = temporary split matrix
		//rgs2 = temporary secondary split matrix
		
		String rgs1[]; String rgs2[];
		
		if(sName.equals("raster directory") || sName.equals("sRasterDir")){
			
			//loading raster directory
			sDirRasters = sValue;
			
			//loading list of files
			rgsFiles = FileIO.getFileList(sDirRasters);
		}else if(sName.equals("sPredictors")){
			rgsPredictors = sValue.split(",");
			this.loadPredictorList(sValue);
		//else if(sName.equals("sCandidatePredictors")){
		//	rgsCandidatePredictors = sValue.split(",");
		}else if(sName.equals("location") || sName.equals("sLocation")){
			sLocation = sValue;
		}else if(sName.equals("dMESSCutoff")){
			dMESSCutoff = Double.parseDouble(sValue);
		}else if(sName.equals("mess summary path") || sName.equals("sMESSSummaryPath")){
			sMESSPath = sValue;
		}else if(sName.equals("maximum number of covariates") || sName.equals("iMaximumCovariates")){
			
			//loading maximum number of covariates
			iMaxVars = Integer.parseInt(sValue);
		}else if(sName.equals("output directory") || sName.equals("sOutputDir")){
			sDirOutput = sValue;
		}else if(sName.equals("topography file") || sName.equals("sGlobalTopographyPath")){
			sPathGlobalTopography = sValue;
		}else if(sName.equals("data file") || sName.equals("sDataPath_0")){	
			
			//loading data path
			sPathData = sValue;
		}else if(sName.equals("sDataPath")){
			
			//loading IO files and directories
			sDirOutput = FileIO.getFileDirectory(sValue);
			sPathData=sValue;
			sPathIndependentData = sValue.replace(".data",".idat");
			sPathIndependentValidation = sValue.replace(".data",".indp");	
			sMESSPath=sValue.replace(".data",".mess");
			sModelPath=sValue.replace(".data",".modl");
			sVarsPath=sValue.replace(".data",".vars");
			sMapPath=sValue.replace(".data",".nc");
			sCrossvalidationPath=sValue.replace(".data",".cval");
			sPathCoefficients=sValue.replace(".data",".coef");
			sPathLog=sValue.replace(".data",".log");
			
			//loading model selection data
			if(FileIO.checkFileExistence(sModelPath)){
				loadPredictors("sSelectModelPath",sModelPath + ":3");
			}
		}else if(sName.equals("predictor values") || sName.equals("sPredictorValues")){	
			
			//loading fixed predictors
			rgs1 = sValue.split("-");
			
			//loading list of elevations and times
			loadElevationsTimes(rgs1);
			
			//loading lists
			for(int i=0;i<rgs1.length;i++){
				rgs2 = rgs1[i].split(",");
				lstVert.add(Double.parseDouble(rgs2[0].split(":")[1]));
				lstTime.add(Double.parseDouble(rgs2[1].split(":")[1]));
			}
		}else if(sName.equals("predictors") || sName.equals("sSelectModelPath")){	
			
			//loading predictors
			loadPredictors(sName,sValue);
		}else if(sName.equals("response variable") || sName.equals("sResponseVariable")){	

			//loading response variable
			sResponse = sValue;
		}else if(sName.equals("response variable transform") || sName.equals("sResponseTransform")){	
			
			//loading transform
			sTransform = sValue;
		}	
	}
	
	private void loadPredictors(String sName, String sValue){
		
		//rgs1 = select model file
		//sPredictors = predictors string
		//iCol = column with data
		
		String rgs1[][];
		String sPredictors = null;
		int iCol;
		
		//checking type of data
		if(sName.equals("predictors")){
		
			//loading predictors
			sPredictors = sValue;
			
		}else if(sName.equals("sSelectModelPath")){
			
			//loading select model output file
			rgs1 = FileIO.readFile(sValue.split(":")[0], ",");
			
			//loading column
			iCol = Integer.parseInt(sValue.split(":")[1]);					
			
			//looping until correct model found
			for(int i=1;i<rgs1.length;i++){
				
				//removing quotes
				rgs1[i][iCol]=rgs1[i][iCol].replace("\"", "");
				
				//checking if selected model
				if(rgs1[i][iCol].startsWith("-->")){
					sPredictors = rgs1[i][iCol];
					sPredictors = sPredictors.replace("--> ", "");
					sPredictors = sPredictors.replace(";", ",");
					
					//loading response variable
					sResponse = rgs1[0][iCol].split("_")[1];
					
					break;
				}
			}
		}
	
		//loading predictors
		rgsPredictors = sPredictors.split(",");
		
		//loading list of predictors
		this.loadPredictorList(sPredictors);	
	}
	
	/**
	 * Loads lists of times and elevations
	 * @param rgsPredValues Times-elevations string in matrix format
	 */
	private void loadElevationsTimes(String rgsPredValues[]){
		
		//rgs1 = current predictor values in split format
		//rgsPredValues = predictor values
		//dElevation = current elevation
		//dTime = current time
		
		double dElevation; double dTime;
		String rgs1[];
		
		//loading list of elevations and times
		lstElevationsCDF = new ArrayList<Double>();
		lstTimesCDF = new ArrayList<Double>();
		lstElevations = new ArrayList<Double>();
		lstTimes = new ArrayList<Double>();
		for(int i=0;i<rgsPredValues.length;i++){
			if(rgsPredValues[i].contains(";")){
				rgs1 = rgsPredValues[i].split(";")[1].split(",");
			}else{
				rgs1 = rgsPredValues[i].split(",");
			}
			for(int j=0;j<rgs1.length;j++){
				if(rgs1[j].startsWith("vert")){
					dElevation = Double.parseDouble(rgs1[j].split(":")[1]);
					if(!lstElevationsCDF.contains(dElevation)){	
						lstElevationsCDF.add(dElevation);
					}
					lstElevations.add(dElevation);
				}else if(rgs1[j].startsWith("time")){
					dTime = Double.parseDouble(rgs1[j].split(":")[1]);
					if(!lstTimesCDF.contains(dTime)){
						lstTimesCDF.add(dTime);
					}
					lstTimes.add(dTime);
				}
			}
		}
	}

	/**
	 * Loads current predictors in list format
	 */
	protected abstract void loadPredictorList(String sValue);
}
