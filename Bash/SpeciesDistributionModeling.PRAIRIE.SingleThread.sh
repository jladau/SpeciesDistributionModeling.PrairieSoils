#!/bin/bash

sOutDir=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Output

sPathFilters=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Filters/Prairie_Soil_Filters.txt

sJavaPath=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Java/Binaries

sRasterDir=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Rasters

sPathMask=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Original_Tallgrass_Prairie_Extent_Map/TallgrassPrairie_Above31N.shp.txt

sGlobalTopographyPath=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Original_Tallgrass_Prairie_Extent_Map/TallgrassPrairie_Above31N.shp.txt

sPathMetadata=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Community_Samples/Tallgrass_Prairie_Soils_Community_Samples_Metadata.csv

sPathSamples=/home/jladau/Desktop/PrairieSpeciesDistributionModeling/Data/Community_Samples/Tallgrass_Prairie_Soils_Community_Samples.csv

iTotalReads=940

sAnalysisMode=alpha-diversity

sPredictorValues=vert:0,time:7

iTotalTasks=10
#note: this variable is set to 250 when running on the cluster

sPredictors=dtrMaxCRU,frsMinCRU,preAntotalCRU,preMinCRU,cmiAnmeanCBI
#note: full set of predictors is sPredictors=cloudfracAnmeanNASA,cloudfracMaxNASA,cloudfracMinNASA,cloudfracMomeanNASA,cloudfracRangeNASA,cmiAnmeanCBI,daylengthMomeanMARINEMS,dtrAnmeanCRU,dtrMaxCRU,dtrMinCRU,dtrMomeanCRU,dtrRangeCRU,dustAnmeanMARINEMS,dustMomeanMARINEMS,elevationAnmeanJAVADIR,frsAntotalCRU,frsMaxCRU,frsMinCRU,frsMomeanCRU,frsRangeCRU,insolationAnmeanNASA,insolationMaxNASA,insolationMinNASA,insolationMomeanNASA,insolationRangeNASA,lightningAnmeanGHCC,lightningMaxGHCC,lightningMomeanGHCC,ndviAnmeanNASA,ndviMaxNASA,ndviMinNASA,ndviMomeanNASA,ndviRangeNASA,nppAnmeanNASA,nppAnmeanSEDAC,nppMaxNASA,nppMinNASA,nppMomeanNASA,nppRangeNASA,preAntotalCRU,preMaxCRU,preMinCRU,preMomeanCRU,preRangeCRU,rainfallAnmeanNASA,rainfallMaxNASA,rainfallMinNASA,rainfallMomeanNASA,rainfallRangeNASA,surfacetempdayAnmeanNASA,surfacetempdayMaxNASA,surfacetempdayMinNASA,surfacetempdayMomeanNASA,surfacetempdayRangeNASA,surfacetempnightAnmeanNASA,surfacetempnightMaxNASA,surfacetempnightMinNASA,surfacetempnightMomeanNASA,surfacetempnightRangeNASA,tmnAnmeanCRU,tmnMaxCRU,tmnMinCRU,tmnMomeanCRU,tmnRangeCRU,tmpAnmeanCRU,tmpMaxCRU,tmpMinCRU,tmpMomeanCRU,tmpRangeCRU,tmxAnmeanCRU,tmxMaxCRU,tmxMinCRU,tmxMomeanCRU,tmxRangeCRU,wetAntotalCRU,wetMaxCRU,wetMinCRU,wetMomeanCRU,wetRangeCRU

iMaxCovariates=4
#note: with full set of rasters iMaxCovariates was set to 6

rgsTaxon=(all all all all p__Verrucomicrobia)
#note: full set of taxa is rgsTaxon=(all all all all p__Verrucomicrobia all all all all all all f__Bradyrhizobiaceae p__Planctomycetes p__Actinobacteria p__Acidobacteria p__Bacteroidetes p__Chloroflexi p__Proteobacteria c__Thaumarchaeota c__Alphaproteobacteria c__Betaproteobacteria c__Deltaproteobacteria c__Gammaproteobacteria)

rgsResponseVar=(ShannonDiversity PcoA1 PcoA1Metagen ShannonMetagen LogitRelAbundance)
#note: full set of response vars is rgsResponseVar=(ShannonDiversity PcoA1 PcoA1Metagen ShannonMetagen LogitRelAbundance LFructoseMannoseRelAbund LCellDivisionRelAbund LNitrogenRelAbund LPeroxisomeRelAbund LPentosePhosphateRelAbund LogRichness LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance LogitRelAbundance)

for i in {0..4}
#note for full set of taxa use for i in {0..22}
do

	#loading short taxon name
	sTaxon=${rgsTaxon[i]/__/}

	#loading response variable
	sResponseVar=${rgsResponseVar[i]}

	#making output directory	
	mkdir $sOutDir/$sTaxon.$sResponseVar

	#loading data path
	sDataPath=$sOutDir/$sTaxon.$sResponseVar/$sTaxon.$sResponseVar.alpha.data	

	echo here

	#compiling data
	java -jar $sJavaPath/CompileObservations.jar --sAnalysisMode=$sAnalysisMode --sTaxonInclude=${rgsTaxon[i]} --sPathMetadata=$sPathMetadata --sPathSamples=$sPathSamples --sRasterDir=$sRasterDir --sPathFilters=$sPathFilters --iRarefactionIterations=1 --iTotalReads=$iTotalReads --sOutputPath=$sDataPath

	echo there

	#analyzing sample coverage
	java -jar $sJavaPath/AnalyzeSampleCoverage.jar --sAnalysisMode=$sAnalysisMode --sRasterDir=$sRasterDir --sGlobalTopographyPath=$sGlobalTopographyPath --sLocation=terrestrial --bMESSPlots=false --sPredictorValues=$sPredictorValues --sDataPath=$sDataPath --sPredictors=$sPredictors

	#selecting model
	for j in {0..10}
	do
		java -Xmx4g -jar $sJavaPath/SelectModel.jar --sAnalysisMode=alpha-diversity --iTotalTasks=$iTotalTasks --iTaskID=$j --sDataPath=$sDataPath --sResponseVariable=$sResponseVar --iMaximumCovariates=$iMaxCovariates --dMESSCutoff=0.05
	done

	#merging select model output
	java -jar $sJavaPath/MergeSelectModelOutput.jar --iTotalTasks=$iTotalTasks --sDataPath=$sDataPath

	#drawing map and running validation
	java -jar $sJavaPath/DrawMap.jar --sAnalysisMode=$sAnalysisMode --sRasterDir=$sRasterDir --sResponseTransform=identity --sPredictorValues=$sPredictorValues --iTaskID=-9999 --iTotalTasks=-9999 --bPrintCrossValidation=true --sDataPath=$sDataPath

	#masking and interpolating map
	java -jar $sJavaPath/MaskAndInterpolateMap.jar --dResolution=0.1 --sPathMask=$sPathMask --sDataPath=$sDataPath

	#deleting completion files
	cd $sOutDir/$sTaxon.$sResponseVar
	rm *.complete
done

#summarizing results
java -jar $sJavaPath/SummarizeResults.jar --sIODir=$sOutDir

