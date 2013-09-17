package edu.ucsf.ranges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.ucsf.base.*;

/**
 * Compiles list of species occurring at sampling locations.
 * @author jladau
 *
 */
public class CommunitySamples {

	//mapSpecies(iLocation) = returns the set of species observed at given location; location codes are sampling location rows (index 1) and columns (index 2)
	//dAreaTotal = total area in the sampling region
	//dPerimeterTotal = total perimeter in the sampling region
	//dDisjointRegionsTotal = total number of disjoint regions in sampling region
	//mapArea(sSpecies) = returns the range area within bounds for specified species
	//mapPerimeter(sSpecies) = returns the range perimeter within bounds for specified species
	//mapDisjointRegions(sSpecies) = returns the number of disjoint regions within bounds for specified species
	//sph1 = spherical geometry object
	//smr1 = SmoothedRanges object
	//slc1 = SamplingLocations object
	
	private SmoothedGeographicRanges smr1;
	private SamplingLocations slc1;
	private SphericalGeometry sph1;
	public Map<Integer,Map<Integer,Set<String>>> mapSpecies;
	public Map<String,Double> mapArea;
	public Map<String,Double> mapPerimeter;
	public Map<String,Double> mapDisjointRegions;
	public double dAreaTotal;
	public double dPerimeterTotal;
	public double dAreaMean;
	public double dPerimeterMean;
	public double dPerimeterAreaRatioMean;
	public double dPerimeterAreaTotalRatio;
	public double dDisjointRegionsTotal;
	
	/**
	 * Constructor
	 * @param smr1 Smoothed range object
	 * @param bds1 Geographic bounds
	 */
	public CommunitySamples(SmoothedGeographicRanges smr1, GeographicPointBounds bds1){

		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//loading perimeters and areas
		findRangeAttributes(smr1,bds1);		
	}
	
	/**
	 * Loads community samples
	 * @param smr1 SmoothedRange object
	 * @param slc1 SamplingLocations object
	 */
	public CommunitySamples(SmoothedGeographicRanges smr1, SamplingLocations slc1){
		this.loadCommunitySamples(smr1, slc1, true);
	}
	
	/**
	 * Loads community samples
	 * @param smr1 SmoothedRange object
	 * @param slc1 SamplingLocations object
	 * @param bLoadRangeAttributes True if load range attributes, false otherwise
	 */
	public CommunitySamples(SmoothedGeographicRanges smr1, SamplingLocations slc1, boolean bLoadRangeAttributes){
		this.loadCommunitySamples(smr1, slc1, bLoadRangeAttributes);
	}
	
	/**
	 * Loads community samples
	 * @param smr1 SmoothedRange object
	 * @param slc1 SamplingLocations object
	 */
	public void loadCommunitySamples(SmoothedGeographicRanges smr1, SamplingLocations slc1, boolean bLoadRangeAttributes){
		
		//iCounter = counter for updating progress
		
		int iCounter;
		
		//saving variables
		this.smr1=smr1;
		this.slc1=slc1;
		
		//initializing species map
		mapSpecies = new HashMap<Integer,Map<Integer,Set<String>>>();
		
		//initializing spherical geometry object
		sph1 = new SphericalGeometry();
		
		//checking if valid sampling points could be found
		if(slc1.rgdSamplingPoints!=null){
		
			//initializing counter
			iCounter = 0;
			
			//initializing set of species
			for(int i=0;i<slc1.rgdSamplingPoints.length;i++){
				
				//************************************
				//System.out.println(slc1.rgdSamplingPoints[i][1] + "," + slc1.rgdSamplingPoints[i][0]);
				//************************************
				
				mapSpecies.put(i, new HashMap<Integer,Set<String>>());
				mapSpecies.get(i).put(0, new HashSet<String>());
				
				//additional column for beta-diversity results, if requested
				if(slc1.sMode.equals("beta-diversity")){
					mapSpecies.get(i).put(1, new HashSet<String>());
				}
			}
			
			//looping through species
			for(String s:smr1.mapSmoothedRange.keySet()){
				
				//updating progress
				iCounter++;
				//System.out.println("Analyzing species " + iCounter + " of " + smr1.mapSmoothedRange.size() + "...");
				
				//checking if sampling bounds overlap range bounds
				if(sph1.doBoundsOverlap(smr1.mapSmoothedRange.get(s).getBounds(), slc1.bds1.rgdArray)==false){
					continue;
				}
				
				//looping through sampling locations for given species
				for(int i=0;i<slc1.rgdSamplingPoints.length;i++){
					
					//updating species set
					if(sph1.isPointPolygon(slc1.rgdSamplingPoints[i][0], slc1.rgdSamplingPoints[i][1], smr1.mapSmoothedRange.get(s), "even-odd")==1){
						mapSpecies.get(i).get(0).add(s);
					}
					
					//updating second sampling point set of species
					if(slc1.sMode.equals("beta-diversity") && sph1.isPointPolygon(slc1.rgdSamplingPoints[i][2], slc1.rgdSamplingPoints[i][3], smr1.mapSmoothedRange.get(s), "even-odd")==1){
						mapSpecies.get(i).get(1).add(s);
					}
				}
			}
		}
		
		//loading perimeters and areas
		if(bLoadRangeAttributes==true){
			findRangeAttributes(smr1,slc1.bds1);
		}
	}
	
	public void findRangeAttributes(){
		this.findRangeAttributes(smr1,slc1.bds1);
	}
	
	private void findRangeAttributes(SmoothedGeographicRanges smr1,GeographicPointBounds bds1){
		
		//dCounter = total number of observations
		//dCounterRatio = counter for perimeter area ratio mean calculation
		
		double dCounter; double dCounterRatio;
		
		//loading perimeters and areas
		dDisjointRegionsTotal=0;
		dAreaTotal=0;
		dPerimeterTotal=0;
		dPerimeterMean=0;
		dAreaMean=0;
		dPerimeterAreaRatioMean=0;
		dCounter=0;
		dCounterRatio=0;
		mapPerimeter = new HashMap<String,Double>();
		mapArea = new HashMap<String,Double>();
		mapDisjointRegions = new HashMap<String,Double>();
		for(String s:smr1.mapSmoothedRange.keySet()){
			
			//checking if bounds are input
			if(bds1==null){
				
				dDisjointRegionsTotal=-9999;
				dPerimeterTotal=-9999;
				dAreaTotal=-9999;
				
			}else{
			
				//checking if bounds overlap and updating perimeter and area accordingly
				if(sph1.doBoundsOverlap(smr1.mapSmoothedRange.get(s).getBounds(), bds1.rgdArray)==true){
					
					mapPerimeter.put(s, smr1.mapSmoothedRange.get(s).findPerimeter(bds1.rgdArray));
					mapArea.put(s, smr1.mapSmoothedRange.get(s).findAreaMonteCarlo(bds1.rgdArray,"even-odd"));
					mapDisjointRegions.put(s, smr1.mapSmoothedRange.get(s).findNumberOfDisjointRegions(bds1));
					dPerimeterTotal+=mapPerimeter.get(s);
					dAreaTotal+=mapArea.get(s);
					dDisjointRegionsTotal+=mapDisjointRegions.get(s);
					dCounter++;
					if(mapArea.get(s)!=0){
						dPerimeterAreaRatioMean+=mapPerimeter.get(s)/mapArea.get(s);
						dCounterRatio++;
					}
				}else{
					mapDisjointRegions.put(s, 0.);
					mapPerimeter.put(s, 0.);
					mapArea.put(s, 0.);
				}
			}
		}
		
		//updating means
		dPerimeterAreaTotalRatio=-9999;
		if(dCounter>0){
			dPerimeterMean=dPerimeterTotal/dCounter;
			dAreaMean=dAreaTotal/dCounter;
			if(dAreaTotal>0){
				dPerimeterAreaTotalRatio=dPerimeterTotal/dAreaTotal;
			}
		}
		if(dCounterRatio!=0){	
			dPerimeterAreaRatioMean=dPerimeterAreaRatioMean/dCounterRatio;
		}else{
			dPerimeterAreaRatioMean=-9999;
		}
		
	}
	
	/**
	 * Returns map; keys are numbers of intersections, values are mean areas of intersection
	 * @param set1
	 * @return
	 */
	public Map<Double,Double> getRangeIntersectionAreas(Set<String> set1, int iIterations){
		
		//set2 = set of polygons for each species
		//s1 = random species for running findAreaIntersection function
		//bInBounds = flag for whether range within bounds is found
		//mapOut = output map
		
		Map<Double,Double> mapOut;
		boolean bInBounds;
		HashSet<Polygon> set2;
		String s1 = null;
		
		//checking if bounds overlap and updating perimeter and area accordingly
		bInBounds = false;
		for(String s:set1){
			if(sph1.doBoundsOverlap(smr1.mapSmoothedRange.get(s).getBounds(), slc1.bds1.rgdArray)==true){
				bInBounds=true;
				break;
			}
		}
		
		//checking if range within bounds found
		if(bInBounds==false){
			mapOut = new HashMap<Double,Double>();
			for(int i=1;i<=set1.size();i++){
				mapOut.put((double) i, 0.);
			}
			return mapOut;
		}
		
		//loading set of polygons and finding intersection area
		set2 = new HashSet<Polygon>();
		for(String s:set1){
			set2.add(smr1.mapSmoothedRange.get(s));
			if(s1==null){	
				s1 = s;
			}
		}
		
		//returning area of intersection
		return smr1.mapSmoothedRange.get(s1).findAreaIntersections(slc1.bds1.rgdArray,"even-odd", iIterations, set2);
	}
	
	/**
	 * Returns array giving areas of intersection corresponding to the rows in lstSubsets
	 * @param lstSubsets List of subsets of species to consider
	 * @param iIterations Number of iterations
	 * @return Map: keys are sets of species, values are areas of intersection
	 */
	public Map<HashSet<String>,Double> getRangeIntersectionAreas(ArrayList<HashSet<String>> lstSubsets, int iIterations){
		
		//s1 = random species for running findAreaIntersection function
		//bInBounds = flag for whether range within bounds is found
		//mapPolygon(sSpecies) = returns the polygon for the given species
		
		Map<String,Polygon> mapPolygon;
		boolean bInBounds; 
		String s1 = null;
		
		//loading map of polygons
		mapPolygon = new HashMap<String,Polygon>();
		for(int i=0;i<lstSubsets.size();i++){
			for(String s:lstSubsets.get(i)){
				if(!mapPolygon.containsKey(s)){
					mapPolygon.put(s, smr1.mapSmoothedRange.get(s));
				}
			}
		}
		
		//checking if bounds overlap and updating perimeter and area accordingly
		bInBounds = false;
		for(String s:mapPolygon.keySet()){
			if(s1==null){
				s1=s;
			}
			if(sph1.doBoundsOverlap(smr1.mapSmoothedRange.get(s).getBounds(), slc1.bds1.rgdArray)==true){
				bInBounds=true;
				break;
			}
		}
			
		//checking if range within bounds found
		if(bInBounds==false){
			return null;
		}
		
		//returning area of intersection
		return smr1.mapSmoothedRange.get(s1).findAreaIntersections(slc1.bds1.rgdArray,"even-odd", iIterations, mapPolygon, lstSubsets);
	}

	/**
	 * Returns array giving areas of intersection corresponding to the rows in lstSubsets
	 * @param lstSubsets List of subsets of species to consider
	 * @param iIterations Number of iterations
	 * @return Map giving the mean area of intersection for the given row of the list of subsets
	 */
	public double[] getRangeIntersectionAreas0(ArrayList<HashSet<String>> lstSubsets, int iIterations){
		
		//s1 = random species for running findAreaIntersection function
		//bInBounds = flag for whether range within bounds is found
		//mapPolygon(sSpecies) = returns the polygon for the given species
		
		Map<String,Polygon> mapPolygon;
		boolean bInBounds; 
		String s1 = null;
		
		//loading map of polygons
		mapPolygon = new HashMap<String,Polygon>();
		for(int i=0;i<lstSubsets.size();i++){
			for(String s:lstSubsets.get(i)){
				if(!mapPolygon.containsKey(s)){
					mapPolygon.put(s, smr1.mapSmoothedRange.get(s));
				}
			}
		}
		
		//checking if bounds overlap and updating perimeter and area accordingly
		bInBounds = false;
		for(String s:mapPolygon.keySet()){
			if(s1==null){
				s1=s;
			}
			if(sph1.doBoundsOverlap(smr1.mapSmoothedRange.get(s).getBounds(), slc1.bds1.rgdArray)==true){
				bInBounds=true;
				break;
			}
		}
			
		//checking if range within bounds found
		if(bInBounds==false){
			return new double[lstSubsets.size()];
		}
		
		//returning area of intersection
		return null;
		//return smr1.mapSmoothedRange.get(s1).findAreaIntersections(slc1.bds1.rgdArray,"even-odd", iIterations, mapPolygon, lstSubsets);
	}
}
