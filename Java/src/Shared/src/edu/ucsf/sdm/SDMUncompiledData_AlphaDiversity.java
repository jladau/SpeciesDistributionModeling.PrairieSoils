package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.Map;

import edu.ucsf.base.Arguments;
import edu.ucsf.base.FilterMatrix;
import edu.ucsf.base.RarefySamples;
import edu.ucsf.sdm.SDM;

public class SDMUncompiledData_AlphaDiversity extends SDMUncompiledData{

	public SDMUncompiledData_AlphaDiversity(Arguments arg1){
		super(arg1);
	}
	
	/**
	 * Runs compiling specific to beta-diversity analysis
	 */
	
	public void runSpecificCompiling(){
	}
	
	/**
	 * Rarefies data and appends to last column of rgsMetadata.
	 * @param rgsMetadata Metadata matrix.
	 * @param rgsSamples Samples matrix.
	 * @param iRareIterations Number of rarefaction iterations to perform.
	 * @param iTotalReads Total number of reads to rarefy to. 
	 */
	public void appendRarefactions(int iRareIterations, int iTotalReads){
		
		//rar1 = RarefySamples object
		//mapShannon = contains current shannon diversity
		//mapChao = contains current chao diversity
		//mapRichness = contains current richness
		//mapUnclass = contains unclassified counts
		//mapTotal = contains total numbers of reads
		//rgdRichness = current total richnesses (rows correspond to rgsMetadata rows)
		//rgdShannon = current total shannon diversities (rows correspond to rgsMetadata rows)
		//rgdChao = current total chao diversities (rows correspond to rgsMetadata rows)
		//rgdUnclass = current total number of unclassified reads
		//rgdTotal = total numbers of reads (rows correspond to rgsMetadata rows)
		//iCols = number of columns in metadata matrix
		//lst1 = list of samples with fewer than the total number of reads (filtered out)
		//flt1 = FilterMatrix object (for removing samples with too few reads)
		//sbl1 = current string being written
		//d1 = fraction of reads classified (+1)
		
		FilterMatrix flt1;
		int iCols;
		RarefySamples rar1;
		Map<String,Integer> mapTotal; Map<String,Double> mapRichness; Map<String,Double> mapShannon; Map<String,Double> mapChao; Map<String,Double> mapUnclass;
		double rgdRichness[]; double rgdShannon[]; double rgdChao[]; double rgdUnclass[]; double rgdTotal[];
		ArrayList<String> lst1;
		StringBuilder sbl1;
		double d1;
		
		//initializing rarefaction object
		rar1 = new RarefySamples(rgsSamples);
		
		//initializing rgdRichness and rgdShannon
		rgdTotal = new double[rgsMetadata.length]; 
		rgdRichness = new double[rgsMetadata.length]; 
		rgdShannon = new double[rgsMetadata.length]; 
		rgdChao = new double[rgsMetadata.length]; 
		rgdUnclass = new double[rgsMetadata.length];
		
		//iterating
		for(int k=0;k<iRareIterations;k++){
			
			//updating progress
			System.out.println("Rarefaction iteration " + (k+1) + " of " + iRareIterations + "...");
			
			//loading current estimates
			rar1.rarefy(iTotalReads, "diversity",false);
			mapShannon = rar1.getShannon();
			mapChao = rar1.getChao();
			mapUnclass = rar1.getUnclassified();
			mapRichness = rar1.getRichnessSim();
			
			//updating shannon and chao diversities and unclassified data
			for(int i=1;i<rgsMetadata.length;i++){
				if(rgdShannon[i]!=-9999){
					if(mapShannon.containsKey(rgsMetadata[i][iSampleIDCol])){
						rgdShannon[i]+=mapShannon.get(rgsMetadata[i][iSampleIDCol]);
						rgdChao[i]+=mapChao.get(rgsMetadata[i][iSampleIDCol]);
						//rgdRichnessUnclassCorrect[i]+=mapRichness.get(rgsMetadata[i][iSampleIDCol])/(1.-mapUnclass.get(rgsMetadata[i][iSampleIDCol])/((double) iTotalReads));
						//rgdShannonUnclassCorrect[i]+=mapShannon.get(rgsMetadata[i][iSampleIDCol])/(1.-mapUnclass.get(rgsMetadata[i][iSampleIDCol])/((double) iTotalReads));
						rgdUnclass[i]+=mapUnclass.get(rgsMetadata[i][iSampleIDCol]);
					}else{
						rgdShannon[i]=-9999.;
						rgdChao[i]=-9999.;
						rgdUnclass[i]=-9999;
					}
				}
			}
		}
		
		//finding richness
		rar1.rarefy(iTotalReads, "diversity", true);
		mapRichness = rar1.getRichnessExact();
		for(int i=1;i<rgsMetadata.length;i++){
			if(rgdRichness[i]!=-9999){
				if(mapRichness.containsKey(rgsMetadata[i][iSampleIDCol])){
					rgdRichness[i]=mapRichness.get(rgsMetadata[i][iSampleIDCol]);
				}else{
					rgdRichness[i]=-9999.;
				}
			}
		}
		
		//finding total numbers of reads
		mapTotal = rar1.getTotalReads();
		for(int i=1;i<rgsMetadata.length;i++){
			if(rgdTotal[i]!=-9999){
				if(mapTotal.containsKey(rgsMetadata[i][iSampleIDCol])){
					rgdTotal[i]=mapTotal.get(rgsMetadata[i][iSampleIDCol]);
				}else{
					rgdTotal[i]=-9999.;
				}
			}
		}
		
		//finding number of metadata columns
		iCols = rgsMetadata[0].length;
		
		//appending means
		rgsMetadata[0][iCols-1]+=",OriginalNumberOfReads,NumberOfReads,NumberOfRarefactions,NumberOfUnclassifiedReads,ShannonDiversity,Richness,ChaoRichness,LogRichness,LogShannonDiversity,LogitRelAbundance";
		for(int i=1;i<rgsMetadata.length;i++){
			sbl1 = new StringBuilder();	
			sbl1.append("," + rgdTotal[i]);
			sbl1.append("," + iTotalReads);
			sbl1.append("," + iRareIterations);
			sbl1.append("," + rgdUnclass[i]/((double) iRareIterations));
			sbl1.append("," + rgdShannon[i]/((double) iRareIterations));
			sbl1.append("," + rgdRichness[i]);
			sbl1.append("," + rgdChao[i]/((double) iRareIterations));
			sbl1.append("," + Math.log10(rgdRichness[i]+1));
			sbl1.append("," + Math.log10(rgdShannon[i]/((double) iRareIterations)+0.1));
			
			//loading logit of relative abundance
			d1 = iTotalReads-rgdUnclass[i]/((double) iRareIterations);
			if(d1>iTotalReads-2){
				d1=iTotalReads-2;
			}
			d1+=1.;
			d1/=((double) iTotalReads);
			sbl1.append("," + Math.log(d1/(1.-d1)));
			rgsMetadata[i][iCols-1]+= sbl1.toString();
		}
		
		//filtering out locations with fewer than enough reads
		lst1 = new ArrayList<String>();
		for(int i=1;i<rgdRichness.length;i++){
			if(rgdRichness[i]==-9999.){
				lst1.add(rgsMetadata[i][iSampleIDCol]);
			}
		}
		for(int i=0;i<lst1.size();i++){
			flt1 = new FilterMatrix(rgsMetadata,rgsMetadata[0][iSampleIDCol] + ":notequals:" + lst1.get(i));
			rgsMetadata = flt1.getMatrix();
		}
	}
}
