SpeciesDistributionModeling.PrairieSoils
========================================

Code and data for running niche modeling of tallgrass prairie microbiome.

###Dependencies: 
Java runtime environment 1.6 or later.
Maven 1.0 ( if you want to compile from source )
git ( to download from github )

###Installation
```
cd <root directory for download>
git clone git@github.com:jladau/SpeciesDistributionModeling.PrairieSoils.git
```

###Usage
To run this code, call the shell script 'Bash/SpeciesDistributionModeling.PRAIRIE.SingleThread.sh' :

```
bash '<path to download>/PrairieSpeciesDistributionModeling/Bash/SpeciesDistributionModeling.PRAIRIE.SingleThread.sh'
```

The output will be written to the 'Output' directory.   The 'Output' directory should be empty prior to running the script to avoid naming conflicts.

The computationally expensive step in the analysis is running the all-subsets model selection, which occurs when 'Java/Binaries/SelectModel.jar' is called.  The computational expense of this step increases with the number of candidate predictors being considered and the maximum allowed model size.  Thus, to run the full analysis described in the paper, it is necessary to parallelize this step.  However, the bash script included here is designed to run in a single thread: it only considers models with 4 or fewer predictors and 5 candidate rasters of environmental conditions.   If you are interested in running the full analysis presented in the paper, please contact the authors for a complete set of rasters of environmental conditions and tips on paralellizing the bash script.  Otherwise, the code and compiled binaries presented here are identical to those used for the full analysis in the paper.

A description of the files is as follows:

'Bash/SpeciesDistributionModeling.PRAIRIE.SingleThread.sh': Bash script for running the analysis.

'Java/Binaries':  Compiled Java jar files for running doing the computational parts of the analysis.  These jar files are called by the aforementioned bash script.

'Java/External_Libraries': Contains third party libraries necessary for the analysis.

'Java/src': Contains Java source files.

'Data/Community_Samples/Tallgrass_Prairie_Soils_Community_Samples.csv':  OTU table in flat format for the sampled locations.

'Data/Community_Samples/Tallgrass_Prairie_Soils_Community_Samples_Metadata.csv':  Metadata for the sampled locations.

'Data/Filters/Prairie_Soil_Filters.txt':  Filters for removing unused samples (none implemented).

'Data/Original_Tallgrass_Prairie_Extent_MapTallgrassPrairie_Above31N.shp.txt':  Map of the original extent of the tallgrass prairie.

'Data/Rasters': Contains a subset of the rasters that were used as candidates in the analysis.  The ones that are included are those that were found to be important predictors.  Rasters are in NetCDF format (http://www.unidata.ucar.edu/software/netcdf).
