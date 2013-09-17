package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.ucsf.base.Arguments;
import edu.ucsf.base.FilterMatrix;
import edu.ucsf.base.RarefySamples;

public class SDMUncompiledData_BetaDiversity extends SDMUncompiledData{

	public SDMUncompiledData_BetaDiversity(Arguments arg1){
		super(arg1);
	}
	
	/**
	 * Runs compiling specific to beta-diversity analysis
	 */
	
	public void runSpecificCompiling(){
		
		//formatting metadata for distances
		System.out.println("Finding covariate distances...");
		findCovariateDistances();
	}
	
	/**
	 * Rarefies data and appends to last column of rgsMetadata.
	 * @param rgsMetadata Metadata matrix.
	 * @param rgsSamples Samples matrix.
	 * @param iRareIterations Number of rarefaction iterations to perform.
	 * @param iTotalReads Total number of reads to rarefy to. 
	 */
	public void appendRarefactions(int iRarefactionIterations, int iTotalReads){
		
		//rar1 = RarefySamples object
		//mapAbundances(sSample) = returns a map of the number of reads belonging to each taxon
		//mapBrayCurtis(sSample1,sSample2) = returns total bray curtis (across rarefactions) for given pair of samples
		//mapPercentChangeRichness(sSample1,sSample2) = returns the total percent change in richness (across rarefactions) for a given pair of samples.  sSample1 is treated as the starting sample
		//mapPercentChangeShannon(sSample1,sSample2) = returns the total percent change in shannon (across rarefactions) for a given pair of samples.  sSample1 is treated as the starting sample
		//iCols = number of columns in metadata matrix
		//lst1 = list of samples with fewer than the total number of reads (filtered out)
		//rgs1 = current pair of samples
		//d1 = current total beta diversity
		//sSamplePair = current pair of samples
		//dMean = current mean value of bray-curtis
		
		int iCols;
		RarefySamples rar1;
		Map<String,Map<String,Integer>> mapAbundances;  Map<String,Double> mapBrayCurtis; Map<String,Double> mapPercentChangeRichness; Map<String,Double> mapPercentChangeShannon;
		String rgs1[];
		double d1; double dMean;
		String sSamplePair;
		
		//initializing rarefaction object
		rar1 = new RarefySamples(rgsSamples);
		
		//initializing beta diversity map
		mapBrayCurtis = new HashMap<String,Double>();
		mapPercentChangeRichness = new HashMap<String,Double>();
		mapPercentChangeShannon = new HashMap<String,Double>();
		for(int i=1;i<rgsMetadata.length;i++){
			mapBrayCurtis.put(rgsMetadata[i][iSampleIDCol], 0.);
			mapPercentChangeRichness.put(rgsMetadata[i][iSampleIDCol], 0.);
			mapPercentChangeShannon.put(rgsMetadata[i][iSampleIDCol], 0.);
		}
	
		//iterating
		for(int k=0;k<iRarefactionIterations;k++){
			
			//updating progress
			System.out.println("Rarefaction iteration " + (k+1) + " of " + iRarefactionIterations + "...");
			
			//loading current estimates
			rar1.rarefy(iTotalReads, "abundance",false);
			mapAbundances = rar1.getAbundances();
			
			//looping through pairs of samples
			for(String s:mapBrayCurtis.keySet()){
				
				//loading current pair of samples
				rgs1 = s.split(",");
				
				//checking for error
				if(!mapAbundances.containsKey(rgs1[0])){
					mapBrayCurtis.put(s, -9999.);
					continue;
				}
				if(!mapAbundances.containsKey(rgs1[1])){
					mapBrayCurtis.put(s, -9999.);
					continue;
				}
				
				//loading current bray curtis
				d1 = mapBrayCurtis.get(s);
				
				//updating
				d1+=this.calculateBrayCurtis(mapAbundances.get(rgs1[0]), mapAbundances.get(rgs1[1]));
				
				//saving
				mapBrayCurtis.put(s, d1);
				
				//loading current percent change
				d1 = mapPercentChangeRichness.get(s);
				
				//updating
				d1+=this.calculatePercentChangeRichness(mapAbundances.get(rgs1[0]), mapAbundances.get(rgs1[1]));
				
				//saving
				mapPercentChangeRichness.put(s, d1);
				
				//loading current percent change
				d1 = mapPercentChangeShannon.get(s);
				
				//updating
				d1+=this.calculatePercentChangeShannon(mapAbundances.get(rgs1[0]), mapAbundances.get(rgs1[1]));
				
				//saving
				mapPercentChangeShannon.put(s, d1);
			}
		}
		
		//finding number of metadata columns
		iCols = rgsMetadata[0].length;
		
		//appending means
		rgsMetadata[0][iCols-1]+=",NumberOfReads,NumberOfRarefactions,BrayCurtis,LBrayCurtis,PercentChangeRichness,PercentChangeShannon";
		for(int i=1;i<rgsMetadata.length;i++){
			sSamplePair = rgsMetadata[i][iSampleIDCol];
			
			//checking for error
			if(mapBrayCurtis.get(sSamplePair)==-9999){
				rgsMetadata[i][iCols-1]+="," + iTotalReads + "," + iRarefactionIterations + ",-9999,-9999,-9999,-9999";
				continue;
			}
			
			//saving results
			dMean = mapBrayCurtis.get(sSamplePair)/((double) iRarefactionIterations);
			rgsMetadata[i][iCols-1]+="," + iTotalReads + "," + iRarefactionIterations + "," + dMean + "," + Math.log(dMean/(1.-dMean)) + "," + mapPercentChangeRichness.get(sSamplePair)/((double) iRarefactionIterations) + "," + mapPercentChangeShannon.get(sSamplePair)/((double) iRarefactionIterations);
		}
		
		//filtering no data
		System.out.println("Filtering locations with too few sequences...");
		this.filterNoData();
	}
	
	/**
	 * Filters out rows of metadata matrix with no data values in last column.
	 */
	private void filterNoData(){
		
		//set1 = metadata matrix in set form
		//iCols = number of columns of metadata
		//rgs1 = filtered metadata
		//iRow = current output row
		
		Set<String[]> set1;
		int iRow; int iCols;
		String rgs1[][];
		
		
		//loading number of columns
		iCols = rgsMetadata[0].length;
		
		//initializing metadata array in set format
		set1 = new HashSet<String[]>();
		for(int i=1;i<rgsMetadata.length;i++){
			if(!rgsMetadata[i][iCols-1].endsWith(",-9999")){
				set1.add(rgsMetadata[i]);
			}
		}
		
		//writing output
		rgs1 = new String[set1.size()+1][rgsMetadata[0].length];
		rgs1[0] = rgsMetadata[0];
		iRow=1;
		for(String[] rgs:set1){
			rgs1[iRow]=rgs;
			iRow++;
		}
		
		//saving output
		rgsMetadata = rgs1;
	}
	
	/**
	 * Calculates the percent change between two communities
	 * @param mapAbundance1 Keys are taxon names, values are taxon abundances (first community)
	 * @param mapAbundance2 Keys are taxon names, values are taxon abundances (second community)
	 * @return Percent Change
	 */
	private double calculatePercentChangeRichness(Map<String,Integer> mapAbundance1, Map<String,Integer> mapAbundance2){
		
		//dNew = number of new species in second set
		//dOriginal = original number of species
		
		double dNew; double dOriginal;
		
		//checking if mapAbundance1 is empty
		if(mapAbundance1.size()==0){
			return 1000.;
		}
		
		//loading number of new species
		dNew = (double) mapAbundance2.size();
		
		//loading original number of species
		dOriginal = (double) mapAbundance1.size();
		
		//returning result
		return 100.*(dNew - dOriginal)/dOriginal;
	}
	
	/**
	 * Calculates the percent change between two communities
	 * @param mapAbundance1 Keys are taxon names, values are taxon abundances (first community)
	 * @param mapAbundance2 Keys are taxon names, values are taxon abundances (second community)
	 * @return Percent Change
	 */
	private double calculatePercentChangeShannon(Map<String,Integer> mapAbundance1, Map<String,Integer> mapAbundance2){
		
		//dTotal = total number of individuals
		//dNum = numerator
		//dDen = denominator
		//dValue = current value
		
		double dTotal; double dNum; double dDen; double dValue;
		
		//checking if mapAbundance1 is empty
		if(mapAbundance1.size()==0){
			return 1000.;
		}
		
		//looping through species in first set
		dTotal = 0.;
		dDen = 0.;
		for(String s:mapAbundance1.keySet()){
			dValue = mapAbundance1.get(s);
		
			//**********************
			//System.out.println(dValue);
			//**********************
			
			dDen += dValue * Math.log(dValue);
			dTotal += dValue;
		}
		
		//looping through species in second set
		dNum = 0.;
		for(String s:mapAbundance2.keySet()){
			dValue = mapAbundance2.get(s);
			
			//******************************
			//System.out.println(dValue);
			//******************************
			
			dNum += dValue * Math.log(dValue);
		}
		
		//***************************
		//System.out.println(- 100. + 100.*(dNum - dTotal*Math.log(dTotal))/(dDen - dTotal*Math.log(dTotal)));
		//***************************
		
		//outputting result
		return - 100. + 100.*(dNum - dTotal*Math.log(dTotal))/(dDen - dTotal*Math.log(dTotal));
	}

	/**
	 * Calculates the bray curtis diversity between two communities
	 * @param mapAbundance1 Keys are taxon names, values are taxon abundances (first community)
	 * @param mapAbundance2 Keys are taxon names, values are taxon abundances (second community)
	 * @return Bray Curtis diversity
	 */
	private double calculateBrayCurtis(Map<String,Integer> mapAbundance1, Map<String,Integer> mapAbundance2){
		
		//dNum = numerator
		//dDen = denominator
		//d1 = current first value
		//d2 = current second value
		
		double dNum=0; double dDen=0; double d1; double d2;
		
		//checking for empty sets
		if(mapAbundance1.size()==0 || mapAbundance2.size()==0){
			return 0;
		}
		
		//looping through entries in first map
		for(String s:mapAbundance1.keySet()){
			
			//loading first value
			d1 = mapAbundance1.get(s);
			
			//loading second value
			if(mapAbundance2.containsKey(s)){
				d2 = mapAbundance2.get(s);
			}else{
				d2=0;
			}
			
			//updating numerator and denominator
			dNum+=Math.abs(d1-d2);
			dDen+=d1+d2;
		}
		
		//looping through entries in second map
		for(String s:mapAbundance2.keySet()){
			
			//updating if necessary
			if(!mapAbundance1.containsKey(s)){
				d2 = mapAbundance2.get(s);
				dNum+=d2;
				dDen+=d2;
			}
		}
		
		
		//outputting result
		//returning result
		if(dDen==0){
			return 0.;
		}else{
			return dNum/dDen;
		}
	}
	
	/**
	 * Finds distances between pairs of covariates
	 */
	private void findCovariateDistances(){
		
		//rgsMetadata2 = formatted metadata
		//iRow = current output row
		//rgs1 = current data from first sample in split form
		//rgs2 = current data from second sample in split form
		//sbl1 = current distance string
		//d1 = first metadata value
		//d2 = second metadata value
		
		int iRow;
		String rgsMetadata2[][]; String rgs1[]; String rgs2[];
		StringBuilder sbl1;
		double d1; double d2;
		
		//initializing distances
		rgsMetadata2 = new String[(rgsMetadata.length-1)*(rgsMetadata.length-2)/2+1][rgsMetadata[0].length];
		
		//loading headers
		for(int j=0;j<rgsMetadata[0].length;j++){
			if(j==iSampleIDCol){
				rgsMetadata2[0][j]="SampleID1,SampleID2";
			}else{
				rgs1 = rgsMetadata[0][j].split(",");
				rgsMetadata2[0][j]="";
				for(int k=0;k<rgs1.length;k++){
					if(k>0){
						rgsMetadata2[0][j]+=",";
					}
					rgsMetadata2[0][j]+=(rgs1[k] + "_Diff," + rgs1[k] + "_AbsDiff," + rgs1[k] + "_Max," + rgs1[k] + "_Min");
				}
				//rgsMetadata2[0][j]=rgsMetadata[0][j];
			}
		}
		
		//looping through pairs of rows
		iRow=1;
		for(int i=2;i<rgsMetadata.length;i++){
			for(int k=1;k<i;k++){
				
				//looping through columns
				for(int j=0;j<rgsMetadata[0].length;j++){
					
					//checking if sample column
					if(j==iSampleIDCol){
						rgsMetadata2[iRow][j]=rgsMetadata[i][j] + "," + rgsMetadata[k][j];
					}else{
						
						//loading data
						rgs1 = rgsMetadata[i][j].split(",");
						rgs2 = rgsMetadata[k][j].split(",");
						
						//finding distances
						sbl1 = new StringBuilder();
						for(int l=0;l<rgs1.length;l++){
							
							//appending comma
							if(l>0){
								sbl1.append(",");
							}
							
							//loading values
							try{
								d1 = Double.parseDouble(rgs1[l]);
								d2 = Double.parseDouble(rgs2[l]);
							}catch(Exception e){
								d1 = -9999;
								d2 = -9999;
							}
							
							//saving value
							if(d1!=-9999 && d2!=-9999){
		
								//sbl1.append(Math.abs(d1-d2));
								sbl1.append((d1-d2) + "," + Math.abs(d1-d2) + "," + Math.max(d1,d2) + ","  + Math.min(d1,d2));
							}else{
								sbl1.append("-9999,-9999,-9999,-9999");
							}
						}
						
						//saving
						rgsMetadata2[iRow][j]=sbl1.toString();
					}
				}
				
				//updating row
				iRow++;
			}
		}
		
		//updating metadata
		this.rgsMetadata = rgsMetadata2;
	}
}
