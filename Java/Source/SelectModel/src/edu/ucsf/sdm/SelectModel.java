package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.Map;
import edu.ucsf.base.*;

/**
 * Object for running model selection.
 * @author jladau
 */

public class SelectModel {

	//mapBestModelPRESS(iVars) = returns the best model (model with lowest PRESS) with given number of variables
	//mapPRESS(sModel) = returns the press value of given model
	//mapBestModelR2(iVars) = returns the best model (model with greatest R^2) with given number of variables
	//mapR2(sModel) = returns the r^2 value of given model
	//dObservations = total number of observations (for adjusted R^2 calculation)
	
	private Map<Integer,String> mapBestModelPRESS;
	private Map<String,Double> mapPRESS;
	private Map<Integer,String> mapBestModelR2;
	private Map<String,Double> mapR2;
	private double dObservations;
	
	/**
	 * Constructor.
	 */
	public SelectModel(SDM sdm1, boolean bTerminateEarly, int iTaskID, int iTotalTasks){
		
		//sblLM = current LM call
		//rgi1 = indices of variables currently being considered
		//cmb1 = current CombinatoricAlgorithms object
		//dPRESS = current press value
		//dPRESSMin = minimum PRESS value for current number of variables
		//dR2 = current r^2 value
		//dR2Max = maximum r^2 for current number of variables
		//sBestModel2 = current best model
		//sBestModel1 = previous best model
		//sBestModel0 = best model with zero parameters
		//dCompletedIterations = total number of completed iterations
		//iVars = number of variables
		//lTimeStart = starting time
		//dElapsedTime = elapsed time in minutes
		//itr1 = ClusterIterator
		
		ClusterIterator itr1;
		StringBuilder sblLM;
		int rgi1[];
		CombinatoricAlgorithms cmb1;
		double dPRESS; double dPRESSMin; double dR2; double dR2Max; double dElapsedTime = 0; double dCompletedIterations = 0;
		String sBestModel2; String sBestModel1; String sBestModel0;
		int iVars = 0;
		long lTimeStart;
		
		//loading number of observations
		dObservations = (double) (sdm1.obs1.rgsData.length-1);
		
		//initializing maps
		mapBestModelPRESS = new HashMap<Integer,String>();
		mapPRESS = new HashMap<String,Double>();
		mapBestModelR2 = new HashMap<Integer,String>();
		mapR2 = new HashMap<String,Double>();
		
		//initializing stringbuilder object
		sblLM = new StringBuilder();
		
		//loading intercept only model
		
		//*****************
		//System.out.println(sdm1.arg1.lstResponses.get(iModel));
		//*****************
		
		sdm1.mdl1.runCrossValidation(sdm1.arg1.sResponse, null);
		mapBestModelPRESS.put(0, "(Intercept Only)");
		mapPRESS.put("(Intercept Only)",sdm1.mdl1.findTSS(sdm1.arg1.sResponse));
		mapBestModelR2.put(0, "(Intercept Only)");
		mapR2.put("(Intercept Only)",0.);
		
		//*********************
		//System.out.println(rgsVars.length + " variables");
		//*********************
		
		//looping through numbers of variables
		for(int k=1;k<=sdm1.arg1.iMaxVars;k++){
			
			//loading number of variables
			iVars = k;
			
			//updating progress
			System.out.println("Analyzing models with " + k + " parameters...");
			
			//initializing minimum press value and maximum R^2 value
			dPRESSMin = 99999999999999999999999.;
			dR2Max = -9999;
			
			//initializing combinatoric algorithms object
			cmb1 = new CombinatoricAlgorithms();
			rgi1 = cmb1.NEXKSB(sdm1.obs1.rgsCandidateVars.length, k);
		
			//initializing counter
			//iCounter = -1;
			
			//initializing completed iterations counter
			dCompletedIterations = 0;
			
			//loading start time
			lTimeStart = System.currentTimeMillis();
			
			//initializing iterator
			itr1 = new ClusterIterator(iTaskID,iTotalTasks);
			
			//looping through subsets
			do{
				//updating counter and checking iteration, if appropriate
				itr1.next();
				if(itr1.bInclude==true){
				//if(iTotalTasks == -9999 || ( iTotalTasks != -9999 && (iCounter % iTotalTasks) == (iTaskID-1))){
					
					//updating completed iterations counter
					dCompletedIterations++;
					
					//loading lm call
					sblLM = new StringBuilder();
					for(int i=0;i<rgi1.length;i++){
						sblLM.append(sdm1.obs1.rgsCandidateVars[rgi1[i]-1]);
						if(i<rgi1.length-1){
							sblLM.append(",");
						}
					}
					
					//*************************
					//System.out.println(sblLM.toString());
					//*************************
					
					try{	
					
						//fitting model
						sdm1.mdl1.runCrossValidation(sdm1.arg1.sResponse,sblLM.toString().split(","));
					
						//saving PRESS value if appropriate
						dPRESS = sdm1.mdl1.findPRESS();
						
						//**************************
						//System.out.println(dPRESS);
						//**************************
						
						if(dPRESS<dPRESSMin){
							
							//checking vif
							if(sdm1.mdl1.checkVIF(5)){
								mapBestModelPRESS.put(k, sblLM.toString());
								mapPRESS.put(sblLM.toString(), dPRESS);
								dPRESSMin = dPRESS;
							}
						}
						
						//saving R^2 value if appropriate
						dR2 = sdm1.mdl1.findRSquared();
						if(dR2>dR2Max){
							
							//checking vif
							if(sdm1.mdl1.checkVIF(5)){
								mapBestModelR2.put(k, sblLM.toString());
								mapR2.put(sblLM.toString(), dR2);
								dR2Max = dR2;
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
					
				//loading next subset
				rgi1 = cmb1.NEXKSB(sdm1.obs1.rgsCandidateVars.length, k);
				
			}while(rgi1[0]!=-9999);
			
			//loading elapsed time
			dElapsedTime = ((double) System.currentTimeMillis() - (double) lTimeStart)/60000.;
			
			//updating progress
			//System.out.println(Math.round(dCompletedIterations) + " iterations in " + (System.currentTimeMillis() - lTimeStart)/60000. + " min");
			
			//printing output
			System.out.println(mapBestModelPRESS.get(k));
			System.out.println("");
			
			//checking if improvement threshold met
			if(bTerminateEarly==true && k>0){
				sBestModel2 = mapBestModelPRESS.get(k);
				sBestModel1 = mapBestModelPRESS.get(k-1);
				sBestModel0 = mapBestModelPRESS.get(0);
				if((mapPRESS.get(sBestModel1)-mapPRESS.get(sBestModel2))/mapPRESS.get(sBestModel0)<0.025){
					break;
				}
			}
		}
		
		//outputting forecast times
		System.out.println("PROJECTED RUN TIMES");
		System.out.println("-------------------");
		for(int i=1;i<=10;i++){
			if(iTotalTasks!=-9999){	
				System.out.println((iVars + i) + " variables = " + findCombin((double) sdm1.obs1.rgsCandidateVars.length,(double) (iVars + i)) * dElapsedTime/(dCompletedIterations*(double) iTotalTasks) + " min");
			}else{
				System.out.println((iVars + i) + " variables = " + findCombin((double) sdm1.obs1.rgsCandidateVars.length,(double) (iVars + i)) * dElapsedTime/(dCompletedIterations) + " min");
			}
		}
		System.out.println("");
	}
	
	/**
	 * Finds dN choose dK
	 * @param dN
	 * @param dK
	 * @return
	 */
	private double findCombin(double dN, double dK){
		
		//d1 = output
		
		double d1;
		
		d1=0;
		for(double d=0;d<dK;d++){
			d1+=Math.log(dN-d);
			d1-=Math.log(d+1);
		}
		return Math.exp(d1);
	}
	
	
	/**
	 * Returns the best model (PRESS) with the given number of covariates.
	 * @param iCovariates Number of covariates.
	 * @return Model
	 */
	public String getBestModelPRESS(int iCovariates){
		
		if(!mapBestModelPRESS.containsKey(iCovariates)){
			return "-9999";
		}else{
			return mapBestModelPRESS.get(iCovariates);
		}
	}
	
	/**
	 * Returns the best model (R2) with the given number of covariates.
	 * @param iCovariates Number of covariates.
	 * @return Model
	 */
	public String getBestModelR2(int iCovariates){
		
		if(!mapBestModelR2.containsKey(iCovariates)){
			return "-9999";
		}else{
			return mapBestModelR2.get(iCovariates);
		}
	}
	
	/**
	 * Returns PRESS for given model
	 * @param sModel model of interest
	 * @return PRESS value
	 */
	public double getPRESS(String sModel){
		
		if(!mapPRESS.containsKey(sModel)){
			return -9999;
		}else{
			return mapPRESS.get(sModel);
		}
	}
	
	
	/**
	 * Returns R2 for given model
	 * @param sModel model of interest
	 * @return R2 value
	 */
	public double getR2(String sModel){
		
		if(!mapR2.containsKey(sModel)){
			return -9999;
		}else{
			return mapR2.get(sModel);
		}
	}
	
	/**
	 * Returns adjusted R2 for given model
	 * @param sModel model of interest
	 * @return Adjusted R2 value
	 */
	public double getAdjustedR2(String sModel, double dPredictors){
		
		if(!mapR2.containsKey(sModel)){
			return -9999;
		}else{
			return 1.-(1.-mapR2.get(sModel))*(dObservations-1.)/(dObservations-dPredictors-1);
		}
	}
	
	/**
	 * Returns the best model (Adjusted R2) with the given number of covariates.
	 * @param iCovariates Number of covariates.
	 * @return Model
	 */
	public String getBestModelAdjustedR2(int iCovariates){
		
		if(!mapBestModelR2.containsKey(iCovariates)){
			return "-9999";
		}else{
			return mapBestModelR2.get(iCovariates);
		}
	}
}
