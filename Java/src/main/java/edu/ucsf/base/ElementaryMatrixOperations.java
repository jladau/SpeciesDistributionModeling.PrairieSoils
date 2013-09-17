package edu.ucsf.base;

/**
 * This class implements basic matrix operations.
 * @author jladau
 *
 */
public class ElementaryMatrixOperations {
	
	
	/**
	 * Finds matrix product of two matrices, where one of the matrices is entered as a vector
	 * @param rgd1 Vector
	 * @param rgd2 Matrix
	 * @return Matrix product
	 */
	public static double[][] findMatrixProduct(double rgd1[], double rgd2[][]){
		
		//rgd3 = vector in matrix format
		
		double rgd3[][];
		
		if(rgd1.length==rgd2.length){	
			
			//loading vector in matrix format
			rgd3 = new double[1][rgd1.length];
			for(int j=0;j<rgd1.length;j++){
				rgd3[0][j]=rgd1[j];
			}
			
			//returning result
			return findMatrixProduct(rgd3,rgd2);
		
		}else{
			
			//loading vector in matrix format
			rgd3 = new double[rgd1.length][1];
			for(int i=0;i<rgd1.length;i++){
				rgd3[i][0]=rgd1[i];
			}
			
			//returning result
			return findMatrixProduct(rgd2,rgd3);
		}
	}
	
	
	/**
	 * Finds the matrix product of two matrices
	 * @param rgd1 First matrix
	 * @param rgd2 Second matrix
	 * @return rgd1*rgd2
	 */
	public static double[][] findMatrixProduct(double rgd1[][], double rgd2[][]){
		
		//rgd3 = output
		
		double rgd3[][];
		
		//initializing output
		rgd3 = new double[rgd1.length][rgd2[0].length];
		
		//looping through entries
		for(int i=0;i<rgd3.length;i++){
			for(int j=0;j<rgd3[0].length;j++){
				for(int k=0;k<rgd2.length;k++){
					rgd3[i][j]+=rgd1[i][k]*rgd2[k][j];
				}
			}
		}
		
		//returning result
		return rgd3;
	}
	
	/**
	 * Finds submatrix.
	 * @param rgsMatrix Matrix that from which submatrix is to be extracted.
	 * @param rgiCols List of columns to include in submatrix.  Enter {-1} for all columns.
	 * @param rgiRows List of rows to include in submatrix. Enter {-1} for all rows.
	 * @return Submatrix with specified rows and columns.
	 */
	public static String[][] extractSubMatrix(String rgsMatrix[][], int rgiRows[], int rgiCols[]){
		
		//rgs1 = output
		//rgi1 = list of rows
		//rgi2 = list of columns
		
		String rgs1[][];
		int rgi1[]; int rgi2[];
		
		//loading list of rows
		if(rgiRows[0]==-1){
			rgi1 = new int[rgsMatrix.length];
			for(int i=0;i<rgi1.length;i++){
				rgi1[i]=i;
			}
		}else{
			rgi1 = rgiRows;
		}
		
		//loading list of columns
		if(rgiCols[0]==-1){
			rgi2 = new int[rgsMatrix[0].length];
			for(int j=0;j<rgi2.length;j++){
				rgi2[j]=j;
			}
		}else{
			rgi2 = rgiCols;
		}
		
		//initializing output
		rgs1 = new String[rgi1.length][rgi2.length];
		
		//writing output
		for(int i=0;i<rgi1.length;i++){
			for(int j=0;j<rgi2.length;j++){
				rgs1[i][j]=rgsMatrix[rgi1[i]][rgi2[j]];
			}
		}
		
		//outputting result
		return rgs1;
	}
	
	/**
	 * Filters out rows that do not match the specified criterion.
	 * @param rgsMatrix Matrix for filtering.
	 * @param iFilterCol Column containing the variable by which filtering is to be done.
	 * @param sFilterVal Value that filter column must have for inclusion.
	 * @param iHeader 0 if no header; 1 if header is present (header will not be filtered out).
	 * @return Submatrix containing just the rows that match filter value
	 */
	public static String[][] filterMatrixRows(String rgsMatrix[][], int iFilterCol, String sFilterVal, int iHeader){
		
		//rgs1 = output
		//i1 = number of rows passing filter
		//iRow = current output row
		
		String rgs1[][];
		int i1=0; int iRow;
		
		//finding number of rows passing filter
		if(iHeader==1){
			i1=1;
		}
		for(int i=iHeader;i<rgsMatrix.length;i++){
			if(rgsMatrix[i][iFilterCol].equals(sFilterVal)){
				i1++;
			}
		}
		
		//initializing output
		rgs1 = new String[i1][rgsMatrix[0].length];
		
		//writing output
		iRow=0;
		for(int i=0;i<rgsMatrix.length;i++){
			if(rgsMatrix[i][iFilterCol].equals(sFilterVal) || (i==0 && iHeader==1)){
				for(int j=0;j<rgsMatrix[0].length;j++){
					rgs1[iRow][j]=rgsMatrix[i][j];
				}
				iRow++;
			}
		}
		
		//outputting result
		return rgs1;
	}
}
