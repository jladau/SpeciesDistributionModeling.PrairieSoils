package edu.ucsf.base;

/**
 * This class allows rows of a matrix to be filtered based on a the value of a specified column.
 * @author jladau
 */
public class FilterMatrix {

	//rgsIncluded = matrix giving values included after filtering
	//rgsExcluded = matrix giving values excluded after filtering
	//rgsPassed = matrix giving values that passed filter
	
	private String rgsIncluded[][];
	private String rgsExcluded[][];
	private String rgsPassed[][];
	
	/**
	 * Constructor.
	 * @param rgsMatrix Matrix to be filtered.
	 * @param sFilter Filter to apply.  Format is variable:filter:value.
	 * variable = the name of any variable listed in the first row of rgsMatrix,
	 * filter = equals to include only values that equal value; notequals to include only values that do not equal value
	 * value = value to use for comparison
	 */
	public FilterMatrix(String rgsMatrix[][], String sFilter){
		
		//iFilterCol = filtering column
		//iIncludeRows = number of rows of data to include
		//iExcludeRows = number of rows of data to exclude
		//iIncludeRow = current output row for inclusion matrix
		//iExcludeRow = current output row for exclusion matrix
		//rgsFilter = filter in split format
		
		String rgsFilter[];
		int iFilterCol=-1; int iIncludeRows; int iExcludeRows; int iIncludeRow; int iExcludeRow;
		
		//loading filter in split format
		rgsFilter = sFilter.split(":");
		
		//loading filtering column
		for(int j=0;j<rgsMatrix[0].length;j++){
			if(rgsMatrix[0][j].equals(rgsFilter[0])){
				iFilterCol=j;
				break;
			}
		}
		
		//finding number of rows to include and exclude
		iIncludeRows = 1;
		iExcludeRows = 1;
		for(int i=1;i<rgsMatrix.length;i++){
			if(rgsMatrix[i][iFilterCol].equals(rgsFilter[2])){
				iIncludeRows++;
			}else{
				iExcludeRows++;
			}
		}
		
		//initializing output matrices
		rgsIncluded = new String[iIncludeRows][rgsMatrix[0].length];
		rgsExcluded = new String[iExcludeRows][rgsMatrix[0].length];
		for(int j=0;j<rgsMatrix[0].length;j++){
			rgsIncluded[0][j]=rgsMatrix[0][j];
			rgsExcluded[0][j]=rgsMatrix[0][j];
		}
		
		//loading output matrices
		iIncludeRow=1;
		iExcludeRow=1;
		for(int i=1;i<rgsMatrix.length;i++){
			if(rgsMatrix[i][iFilterCol].equals(rgsFilter[2])){
				for(int j=0;j<rgsMatrix[0].length;j++){
					rgsIncluded[iIncludeRow][j]=rgsMatrix[i][j];
				}
				iIncludeRow++;
			}else{
				for(int j=0;j<rgsMatrix[0].length;j++){
					rgsExcluded[iExcludeRow][j]=rgsMatrix[i][j];
				}
				iExcludeRow++;
			}
		}
		
		//outputting passed matrix
		if(rgsFilter[1].equals("equals")){
			rgsPassed = rgsIncluded;
		}else{
			rgsPassed = rgsExcluded;
		}
	}
	
	/**
	 * Gets array of values that equaled filter value.
	 * @return Array of included values. First row contains headers.
	 */
	public String[][] getEqualsMatrix(){
		return rgsIncluded;
	}
	
	/**
	 * Gets array of values that did not equal filter value.
	 * @return Array of excluded values. First row contains headers.
	 */
	public String[][] getNotEqualsMatrix(){
		return rgsExcluded;	
	}
	
	/**
	 * Gets array of values that passed filter value.
	 * @return Array of excluded values. First row contains headers.
	 */
	public String[][] getMatrix(){
		return rgsPassed;	
	}
}
