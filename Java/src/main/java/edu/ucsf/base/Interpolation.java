package edu.ucsf.base;

/**
 * This class contains methods for running interpolation.
 * @author jladau
 */

public class Interpolation {

	/**
	 * Runs linear interpolation.
	 * @param dX x value to be interpolated.
	 * @param dX1 First known x value.
	 * @param dX2 Second known x value.
	 * @param dY1 First known y value.
	 * @param dY2 Second known y value.
	 * @return Interpolated value.
	 */
	public static double interpolateLinear(double dX, double dX1, double dX2, double dY1, double dY2){
		
		//checking if x values are the same
		if(dX1 == dX2){
			return dY1;
		}else{
			return dY1 + (dY2-dY1)*(dX-dX1)/(dX2-dX1);
		}
	}
	
	/**
	 * Runs bilinear interpolation.
	 * @param rgsRefData Reference data.  Each row represents a data point (four points total: x1,y1; x2,y1; x1,y2; x2,y2).   Column 0 with x value, column 1 with y value, column 2 with raster value (can be multivariate: delimited with commas).
	 * @param sPoint Point at which interpolation is to be done.   In format x,y.
	 * @return Interpolated value (can be multivariate: delimited with commas).
	 */
	public static String interpolateBilinear(String rgsRefData[][], String sPoint){
		
		//dX1, dX2, dY1, dY2 = values of variables
		//dQ11, dQ12, dQ21, dQ22 = values of raster
		//rgdQ = values of raster
		//rgs11, rgs12, rgs21, rgs22 = raster values in split format
		//dValue = current interpolated value
		//sbl1 = output
		//dX,dY = value to interpolate to
		//dMean = mean of raster values (excluding errors)
		//dCount = number of non-error raster values
		
		double dCount; double dMean; double dX; double dY; double dValue; double dX1; double dX2; double dY1; double dY2;
		String rgs11[]; String rgs21[]; String rgs12[]; String rgs22[];
		StringBuilder sbl1;
		double rgdQ[][];
		
		//loading values of variables
		dX1 = Double.parseDouble(rgsRefData[0][0]);
		dX2 = Double.parseDouble(rgsRefData[1][0]);
		dY1 = Double.parseDouble(rgsRefData[0][1]);
		dY2 = Double.parseDouble(rgsRefData[2][1]);
		
		//loading value to interpolate to
		dX = Double.parseDouble(sPoint.split(",")[0]);
		dY = Double.parseDouble(sPoint.split(",")[1]);
		
		//loading raster values
		rgs11 = rgsRefData[0][2].split(",");
		rgs21 = rgsRefData[1][2].split(",");
		rgs12 = rgsRefData[2][2].split(",");
		rgs22 = rgsRefData[3][2].split(",");
		
		//initializing output
		sbl1 = new StringBuilder();
		
		//looping through variables and interpolating
		for(int j=0;j<rgs11.length;j++){
			
			//loading values of raster
			rgdQ = new double[3][3];
			rgdQ[1][1] = Double.parseDouble(rgs11[j]);
			rgdQ[1][2] = Double.parseDouble(rgs12[j]);
			rgdQ[2][1] = Double.parseDouble(rgs21[j]);
			rgdQ[2][2] = Double.parseDouble(rgs22[j]);
			
			//correcting errors
			dCount=0;
			dMean=0;
			for(int k=1;k<=2;k++){
				for(int l=1;l<=2;l++){
					if(rgdQ[k][l]!=-9999){
						dCount++;
						dMean+=rgdQ[k][l];
					}
				}
			}
			if(dCount>0){
				dMean=dMean/dCount;
				for(int k=1;k<=2;k++){
					for(int l=1;l<=2;l++){
						if(rgdQ[k][l]==-9999){
							rgdQ[k][l]=dMean;
						}
					}
				}
			}
			
			//interpolating
			if(dX1==dX2 && dY1==dY2){
				dValue = rgdQ[1][1];
			}else if(dX1!=dX2 && dY1==dY2){
				dValue = rgdQ[1][1]+(dX-dX1)*(rgdQ[2][1]-rgdQ[1][1])/(dX2-dX1);
			}else if(dX1==dX2 && dY1!=dY2){
				dValue = rgdQ[2][1]+(dY-dY1)*(rgdQ[2][2]-rgdQ[1][1])/(dY2-dY1);
			}else{
				dValue = rgdQ[1][1]*(dX2-dX)*(dY2-dY);
				dValue += rgdQ[2][1]*(dX-dX1)*(dY2-dY);
				dValue += rgdQ[1][2]*(dX2-dX)*(dY-dY1);
				dValue += rgdQ[2][2]*(dX-dX1)*(dY-dY1);
				dValue = dValue/((dX2-dX1)*(dY2-dY1));
			}
			
			//**********************
			//f(dValue<0){
			//	System.out.println("UHOH:" + dValue);
			//}
			//**********************
				
			//saving result
			if(j>0){
				sbl1.append(",");
			}
			sbl1.append(dValue);	
		}
		
		//returning result
		return sbl1.toString();
	}
}
