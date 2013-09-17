package edu.ucsf.sdm;

import java.util.ArrayList;

import edu.ucsf.base.FileIO;

/**
 * Community observation data for beta diversity analysis
 * @author jladau
 */
public abstract class SDMArguments0 {

	//sTransform = transform to use
	//sDirRaster = directory with rasters
	//sPathData = path to data
	//sPathIndependentData = path to independent data set
	//sPathIndependentValidation = path to independent validation
	//sPathCoefficients = path to file with coefficients
	//sPathLog = path to log file
	//lstPredictors = list of predictors (string array format)
	//lstPredictorsList = list of predictors (list format)
	//lstSuffixes = list of suffixes
	//lstResponses = list of response variables
	//lstTransform = list of response variable transforms
	//lstElevationsCDF = list of elevations (no duplicates: for initializing cdf writer)
	//lstTimesCDF = list of times (no duplicates: for initializing cdf writer)
	//lstElevations = list of elevations
	//lstTimes = list of times
	//rgsFixedPredictors = array of fixed predictors
	//lstFiles = list of files
	//rgsFiles = array of files
	//lstVert = list of elevation values
	//lstTime = list of time values
	//iModels = number of models
	//sDirOutput = output directory
	//sPathTopography = path to global topography
	//iMaxVars = maximum number of covariates in model selection
	//lstCandidateVars = list of candidate covariates
	//sLocation = "marine" or "terrestrial"
	//sMESSPath = path to mess summary file
	//sModelPath = path to model selection file
	//sVarsPath = path to file containing candidate variables used in model selection
	//sMapPath = path to map (output)
	//sCrossvalidationPath = path to crossvalidation (output)
	//rgsCandidatePredictors = list of candidate predictors (for sample coverage analysis)
	
	public String[] rgsCandidatePredictors;
	public int iMaxVars;
	public int iModels=0;
	public String sTransform;
	public String sDirRasters;
	public String sDirOutput;
	public String sPathData;
	public String sPathIndependentData;
	public String sPathIndependentValidation;
	public String sPathGlobalTopography;
	public String sPathCoefficients;
	public String sPathLog;
	public ArrayList<String[]> lstPredictors; 
	public ArrayList<ArrayList<String>> lstPredictorFiles; 
	public ArrayList<String> lstSuffixes; 
	public ArrayList<String> lstTransforms; 
	public ArrayList<String> lstResponses;
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
	
	/**
	 * Constructor
	 */
	public SDMArguments0(){
		
		//initializing variable lists
		lstPredictors = new ArrayList<String[]>();
		lstPredictorFiles = new ArrayList<ArrayList<String>>();
		lstSuffixes = new ArrayList<String>();
		lstTransforms = new ArrayList<String>();
		lstResponses = new ArrayList<String>();
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
		}else if(sName.equals("sCandidatePredictors")){
			rgsCandidatePredictors = sValue.split(",");
		}else if(sName.equals("location") || sName.equals("sLocation")){
			sLocation = sValue;
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
		}else if(sName.equals("output suffix") || sName.equals("sOutputSuffix")){
			
			//loading output suffix
			lstSuffixes.add(sValue);	
		}else if(sName.equals("response variable") || sName.equals("sResponseVariable")){	

			//loading response variable
			lstResponses.add(sValue);
			
			//updating total number of models
			iModels++;
		}else if(sName.equals("response variable transform") || sName.equals("sResponseTransform")){	
			
			//loading transform
			lstTransforms.add(sValue);
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
					lstResponses.add(rgs1[0][iCol].split("_")[1]);
					
					//updating total number of models
					iModels++;
					
					break;
				}
			}
		}
	
		//loading predictors
		lstPredictors.add(sPredictors.split(","));
		
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
