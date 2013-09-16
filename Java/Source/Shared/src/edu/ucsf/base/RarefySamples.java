package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.random.RandomDataImpl;

/**
 * This class rarefies samples to a specified sampling depth.
 * @author jladau
 */
public class RarefySamples {
	
	//mapIndices(sSample) = returns the list of rows in rgsSamples that have data for specified sample
	//mapTotal(sSample) = returns the total number of reads in given sample
	//rgsSamples = Data.  See constructor for information.
	//mapAbundance(sSample) = returns a map giving the taxa and their abundances in the given sample
	//mapRichnessExact(sSample) = expected rarefied richness in given sample
	//mapRichnessSim(sSample) = simulated rarefied richness in given sample
	//mapShannon(sSample) = rarefied shannon diversity in given sample
	//mapChao(sSample) = rarefied chao richness estimate in given sample
	//mapUnclassified(sSample) = number of unclassified reads in sample
	//iUnclassIndex = current unclassified read index
	
	private String[][] rgsSamples;
	private Map<String,ArrayList<Integer>> mapIndices;
	private Map<String,Integer> mapTotal;
	private Map<String,Map<String,Integer>> mapAbundance;
	private Map<String,Double> mapShannon;
	private Map<String,Double> mapRichnessExact;
	private Map<String,Double> mapRichnessSim;
	private Map<String,Double> mapChao;
	private Map<String,Double> mapUnclassified;
	private int iUnclassIndex;
	
	/**
	 * Constructor.
	 * @param rgsSamples Data: first column with sample name, second column with taxon name, third column with number of reads.  Each row is assumed to correspond to a unique sample-taxon combination.
	 */
	public RarefySamples(String rgsSamples[][]){
		
		//lst1 = list of rows having data for current sample
		//i2 = current total number of reads
		//sSample = current sample
		
		String sSample;
		ArrayList<Integer> lst1;
		int i2;
		
		//loading data
		this.rgsSamples = rgsSamples;
		
		//initializing maps
		mapIndices = new HashMap<String,ArrayList<Integer>>();
		mapTotal = new HashMap<String,Integer>();
		
		//looping through data to load map1
		for(int i=1;i<rgsSamples.length;i++){
			
			//loading current sample id
			sSample = rgsSamples[i][0];
			
			//checking if sample has been included and loading current variables
			if(mapTotal.containsKey(sSample)){
				i2 = mapTotal.get(sSample);
				lst1 = mapIndices.get(sSample);
			}else{
				i2 = 0;
				lst1 = new ArrayList<Integer>();
			}
			
			//updating total number of reads
			i2+=Integer.parseInt(rgsSamples[i][2]);
			mapTotal.put(sSample, i2);
			
			//updating list of indices
			lst1.add(i);
			mapIndices.put(sSample, lst1);
		}
		
		//**************************
		//for(String s:mapTotal.keySet()){
		//	System.out.println(s + "," + mapTotal.get(s));
		//}
		//System.out.println("HERE");
		//**************************		
	}
	
	/**
	 * Rarefies samples.
	 * @param iTotal Number of reads per sample for rarefaction.
	 * @param sOutputType Either "diversity" or "abundance" for diversity measurements or a sample-by-sample list of rarefied abundances, respectively.
	 * @param bExact True if run exact rarefaction; false otherwise.
	 */
	public void rarefy(int iTotal, String sOutputType, boolean bExact){
	
		//sSample = current samples
		//rgd1 = current rarefied abundance vector
		//dTotal = total number of reads in double format (for calculating shannon diversity)
		//iRichness = current richness
		//dShannon = current shannon diversity
		//dF1 = current count of singletons
		//dF2 = current count of taxa occurring twice
		//d1 = current relative abundance
		//lstIndices = list of row indices in data matrix that are for sample of interest.
		//sTaxon = current taxon
		//dUnclass = current unclassified count
		//map1(sTaxon) = returns abundance for given taxon
		
		int iRichness;
		double d1; double dTotal; double dShannon; double dF1; double dF2; double dUnclass;
		String sSample; String sTaxon;
		double rgd1[];
		ArrayList<Integer> lstIndices;
		Map<String,Integer> map1;
		
		//loading dTotal
		dTotal = (double) iTotal;
		
		//initializing appropriate map
		if(sOutputType.equals("diversity")){
			mapShannon = new HashMap<String,Double>();
			mapRichnessSim = new HashMap<String,Double>();
			mapRichnessExact = new HashMap<String,Double>();
			mapChao = new HashMap<String,Double>();
			mapUnclassified = new HashMap<String,Double>();
		}else{
			mapAbundance = new HashMap<String,Map<String,Integer>>();
		}
		
		//looping through samples
		for(String s : mapTotal.keySet()){
			
			//loading sample name
			sSample = s;
				
			//rarefying current sample
			rgd1 = this.rarefySample(sSample, iTotal);
		
			//checking that rarefaction successful
			if(rgd1[0]==-9999){
				continue;
			}
			
			//updating diversity maps
			if(sOutputType.equals("diversity")){
				
				//clearing richness and shannon
				iRichness = 0;
				dShannon = 0;
				dF1 = 0;
				dF2 = 0;
				dUnclass = 0;
				
				//looping through samples to find current diversity
				for(int i=0;i<rgd1.length;i++){
					
					//checking if some reads were observed for taxon
					if(rgd1[i]>0){
						iRichness++;
						d1 = rgd1[i]/dTotal;
						dShannon-=d1*Math.log(d1);
						if(rgd1[i]==1){
							dF1++;
						}else if(rgd1[i]==2){
							dF2++;
						}
						if(i==iUnclassIndex){
							dUnclass = rgd1[i];
						}
					}
				}
				
				//saving current diversity
				if(bExact==false){
					mapShannon.put(sSample, dShannon);
					mapRichnessSim.put(sSample, (double) iRichness);
					if(dF2>0){
						mapChao.put(sSample, ((double) iRichness) + dF1*dF1/(2.*dF2));
					}else{
						mapChao.put(sSample, ((double) iRichness));
					}
					mapUnclassified.put(sSample, dUnclass);
				}else{
					mapRichnessExact.put(sSample, rarefySampleRichness(sSample,(double) iTotal));	
				}
			}else if(sOutputType.equals("abundance")){
				
				//loading list of rows
				lstIndices = mapIndices.get(sSample);
				
				//initializing taxon map
				map1 = new HashMap<String,Integer>();
				
				//looping through samples
				for(int i=0;i<rgd1.length;i++){
					if(rgd1[i]>0){
						
						//loading current taxon
						sTaxon = rgsSamples[lstIndices.get(i)][1];
						
						//updating map
						map1.put(sTaxon, (int) rgd1[i]);	
					}
				}
				
				//saving taxon map
				mapAbundance.put(sSample, map1);
				
			}
		}
		
		//clearing matrices as appropriate
		if(sOutputType.equals("diversity")){
			if(bExact==true){
				mapShannon.clear();
				mapRichnessSim.clear();
				mapChao.clear();
				mapUnclassified.clear();
			}else{
				mapRichnessExact.clear();	
			}
		}
	}
	
	/**
	 * Gets richnesses for rarefied matrix (calculated using closed form expression).
	 * @return Map: keys are sampling locations, values are number of taxa at locations.
	 */
	public Map<String,Double> getRichnessExact(){
		return mapRichnessExact;
	}
	
	/**
	 * Gets richnesses for rarefied matrix (from resampling simulation).
	 * @return Map: keys are sampling locations, values are number of taxa at locations.
	 */
	public Map<String,Double> getRichnessSim(){
		return mapRichnessSim;
	}
	
	/**
	 * Gets shannon diversities for rarefied matrix.
	 * @return Map: keys are sampling locations, values are shannon diversities at locations.
	 */
	public Map<String,Double> getShannon(){
		return mapShannon;
	}
	
	/**
	 * Gets the total number of reads in each sample.
	 * @return Map: keys are samplings locations, values are number of reads at locations.
	 */
	public Map<String, Integer> getTotalReads(){
		return mapTotal;
	}
	
	/**
	 * Gets unclassified count for rarefied matrix.
	 * @return Map: keys are sampling locations, values are unclassified counts at locations.
	 */
	public Map<String,Double> getUnclassified(){
		return mapUnclassified;
	}
	
	/**
	 * Gets chao diversities for rarefied matrix.
	 * @return Map: keys are sampling locations, values are chao diversities at locations.
	 */
	public Map<String,Double> getChao(){
		return mapChao;
	}
	
	/**
	 * Gets occurrences for rarefied matrix.
	 * @return Map: keys are sampling location - read id combinations, values are number of reads at given combination
	 */
	public Map<String,Map<String,Integer>> getAbundances(){	
		return mapAbundance;
	}
	
	/**
	 * Rarefies given sample so that total of entries equals specified total.  Uses closed form solution and outputs expected richness
	 * @param sSample Name of sample being rarefied.
	 * @param dTotal Total number of reads to rarefy to.
	 * @return Expected richness.
	 */
	private double rarefySampleRichness(String sSample, double dTotal){
		
		//lst1 = list of abundances
		//lstIndices = list of row indices in data matrix that are for sample of interest.
		//dN = total number of reads
		//dOut = output
		
		ArrayList<Double> lst1; ArrayList<Integer> lstIndices;
		double dN; double dOut;
		
		//checking total
		if(mapTotal.get(sSample)<dTotal){
			return -9999;
		}
		
		//loading list of reads
		lst1 = new ArrayList<Double>();
		dN=0;
		lstIndices = mapIndices.get(sSample);
		for(int i=0;i<lstIndices.size();i++){
			lst1.add(Double.parseDouble(rgsSamples[lstIndices.get(i)][2]));
			dN+=Double.parseDouble(rgsSamples[lstIndices.get(i)][2]);
		}
		
		//finding result
		dOut = (double) lstIndices.size();
		for(int i=0;i<lst1.size();i++){
			dOut-=findPr(dN,lst1.get(i),dTotal);
		}
		
		//outputting result
		return dOut;
	}
	
	/**
	 * Finds The probability that a taxon does not occur in the sample. 
	 * @param dN Total number of reads in sample.
	 * @param dNi Total number of reads for taxon.
	 * @param dn Number of reads in rarefied sample.
	 * @return Aforementioned probability.
	 */
	private double findPr(double dN, double dNi, double dn){
		
		//d1 = output
		
		double d1;
		
		//checking initial condition
		if(dn>(dN-dNi)){
			return 0;
		}
		
		//computing probability
		d1=0;
		for(double d=dN-dn-dNi+1;d<=dN-dn;d++){
			d1+=Math.log(d);
		}
		for(double d=dN-dNi+1;d<=dN;d++){
			d1-=Math.log(d);
		}
		
		//returning result
		return Math.exp(d1);
	}
	
	/**
	 * Rarefies given sample so that total of entries equals specified total
	 * @param sSample Name of sample being rarefied.
	 * @param iTotal Total number of reads to rarefy to.
	 * @return Vector of counts giving numbers of reads after rarefaction.  Rows of vectors correspond to rows listed in entry of mapIndices corresponding to current sample.
	 */
	private double[] rarefySample(String sSample, int iTotal){
		
		//rgdOut = output
		//lst1 = list of reads (identified by row in lstIndices) that are to be chosen from
		//rnd1 = random data object
		//rgo1 = object array with list of included reads
		//lstIndices = list of row indices in data matrix that are for sample of interest.
		//iReads = number of reads for current taxon
		//rgdOut = output
		
		ArrayList<Integer> lst1; ArrayList<Integer> lstIndices;
		RandomDataImpl rnd1 = null;
		Object rgo1[];
		int iReads;
		double rgdOut[];
		
		//checking total
		if(mapTotal.get(sSample)<iTotal){
			return new double[]{-9999};
		}
		
		//loading list of reads and unclassified index
		iUnclassIndex = -9999;
		lst1 = new ArrayList<Integer>(mapTotal.get(sSample));
		lstIndices = mapIndices.get(sSample);
		for(int i=0;i<lstIndices.size();i++){
			iReads = Integer.parseInt(rgsSamples[lstIndices.get(i)][2]);
			for(int k=0;k<iReads;k++){
				lst1.add(i);
			}
			if(rgsSamples[lstIndices.get(i)][1].equals("unclassified")){
				iUnclassIndex = i;
			}
		}
		
		//generating sample
		rnd1 = new RandomDataImpl();
		rgo1 = rnd1.nextSample(lst1,iTotal);
		
		//writing output
		rgdOut = new double[lstIndices.size()];
		for(int i=0;i<rgo1.length;i++){
			rgdOut[((Number) rgo1[i]).intValue()]++;
		}
		
		//++++++++++++++++++++++
		//printing phylum count list
		//printPhylumAbundances(sSample,lstIndices,rgdOut);
		//++++++++++++++++++++++
		
		//outputting results
		return rgdOut;
	}
	
	private void printPhylumAbundances(String sSample, ArrayList<Integer> lstIndices, double[] rgdAbundances){
		
		//mapCount(sPhylum) = returns number reads belonging to phylum
		//iCount = current count
		//rgs1 = current OTU string in split format
		//sTaxon = current taxon
		
		int iCount;
		HashMap<String,Integer> mapCount;
		String rgs1[];
		String sTaxon;
		
		//initializing count map
		mapCount = new HashMap<String,Integer>();
		
		//looping through taxa
		for(int i=0;i<rgdAbundances.length;i++){
			if(rgdAbundances[i]>0){
				
				//loading taxon
				sTaxon = "unclassified";
				rgs1 = rgsSamples[lstIndices.get(i)][1].split(";");
				for(int k=0;k<rgs1.length;k++){
					if(rgs1[k].trim().startsWith("p__")){
						sTaxon = rgs1[k].trim();
						if(sTaxon.equals("p__")){
							sTaxon="unclassified";
						}
						break;
					}
				}
				
				//updating count
				if(mapCount.containsKey(sTaxon)){
					iCount = mapCount.get(sTaxon);
				}else{
					iCount = 0;
				}
				iCount+=rgdAbundances[i];
				mapCount.put(sTaxon, iCount);
			}
		}
		
		//outputting results
		for(String s:mapCount.keySet()){
			System.out.println(sSample + "," + s + "," + mapCount.get(s));
		}
	}
}
