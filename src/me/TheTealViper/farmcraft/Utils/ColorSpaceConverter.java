package me.TheTealViper.farmcraft.Utils;

public class ColorSpaceConverter {
	//https://drafts.csswg.org/css-color-4/conversions.js
	//http://www.brucelindbloom.com/index.html?Math.html
	//https://github.com/LeaVerou/css.land/blob/master/lch/lch.js
	//https://css.land/lch/
	//http://colormine.org/convert/rgb-to-xyz
	
	final static double[] D50 = {0.3457 / 0.3585, 1.00000, (1.0 - 0.3457 - 0.3585) / 0.3585};
	final static double[] D65 = {0.3127 / 0.3290, 1.00000, (1.0 - 0.3127 - 0.3290) / 0.3290};
	
	public static double[] RGB_to_LCH(int r, int g, int b) {
		return LAB_to_LCH(XYZ_to_LAB(RGB_to_XYZ(r, g, b)));
	}
	public static double[] LCH_to_RGB(double[] LCH) {
		return XYZ_to_RGB(LAB_to_XYZ(LCH_to_LAB(LCH)));
	}
	
/*
 * 
 * 
 * INDIVIDUAL OPERATIONS
 * 
 * 
 */
	
	public static double[] RGB_to_XYZ(int r, int g, int b) {
		//Input RGB should be 0 -> 1
		//Output XYZ is 0 -> 1
		
		double[][] rgb = {{r/255d},{g/255d},{b/255d}};
		double[][] M = {
			{ 0.41239079926595934, 0.357584339383878,   0.1804807884018343  },
			{ 0.21263900587151027, 0.715168678767756,   0.07219231536073371 },
 			{ 0.01933081871559182, 0.11919477979462598, 0.9505321522496607  }
		};
		double[][] XYZ_raw = MultiplyMatrices.multiplyMatrices(M, rgb);
		double[] XYZ = new double[XYZ_raw.length * XYZ_raw[0].length];
		for(int i1 = 0;i1 < XYZ_raw.length;i1++) {
			for(int i2 = 0;i2 < XYZ_raw[0].length;i2++) {
				XYZ[i1*XYZ_raw[0].length + i2] = XYZ_raw[i1][i2]*1;
			}
		}
		XYZ = D65_to_D50(XYZ);
		
		return XYZ;
	}
	
	public static double[] D65_to_D50(double[] Orig_XYZ) {
    	// Bradford chromatic adaptation from D65 to D50
    	// The matrix below is the result of three operations:
    	// - convert from XYZ to retinal cone domain
    	// - scale components from one reference white to another
    	// - convert back to XYZ
    	// http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    	double[][] M =  {
    		{  1.0479298208405488,    0.022946793341019088,  -0.05019222954313557 },
    		{  0.029627815688159344,  0.990434484573249,     -0.01707382502938514 },
    		{ -0.009243058152591178,  0.015055144896577895,   0.7518742899580008  }
    	};
    	double[][] XYZ = {{Orig_XYZ[0]},{Orig_XYZ[1]},{Orig_XYZ[2]}};
    
        double[][] result_raw = MultiplyMatrices.multiplyMatrices(M, XYZ);
        double[] result = new double[result_raw.length * result_raw[0].length];
        for(int i1 = 0;i1 < result_raw.length;i1++) {
    			for(int i2 = 0;i2 < result_raw[0].length;i2++) {
    				result[i1*result_raw[0].length + i2] = result_raw[i1][i2]*1;
    			}
    		}
    	return result;
    }
	
	public static double[] XYZ_to_LAB(double[] XYZ){
	    
    	double EPSILON = 216d/24389d;  // 6^3/29^3
    	double KAPPA = 24389d/27d;   // 29^3/3^3
    	
    	double x_scaled = XYZ[0] / D50[0];
    	double y_scaled = XYZ[1] / D50[1];
    	double z_scaled = XYZ[2] / D50[2];
    
        double f_x = x_scaled > EPSILON ? Math.cbrt(x_scaled) : (KAPPA*x_scaled + 16d)/116d;
        double f_y = y_scaled > EPSILON ? Math.cbrt(y_scaled) : (KAPPA*y_scaled + 16d)/116d;
        double f_z = z_scaled > EPSILON ? Math.cbrt(z_scaled) : (KAPPA*z_scaled + 16d)/116d;
        
    	double[] LAB = {(116d*f_y)-16d, 500d*(f_x-f_y), 200d*(f_y-f_z)};
    
    	return LAB;
    	// L in range [0,100]. For use in CSS, add a percent
	}
	
	public static double[] LAB_to_LCH(double[] LAB){
	    // Convert to polar form
    	double hue = Math.atan2(LAB[2], LAB[1]) * 180d / Math.PI;
    	
    	double[] LCH = {
    		LAB[0], // L is still L
    		Math.sqrt(Math.pow(LAB[1], 2) + Math.pow(LAB[2], 2)), // Chroma
    		hue >= 0d ? hue : hue + 360d // Hue, in degrees [0 to 360)
    	};
    	return LCH;
	}
	
/*
 * 
 * 
 * REVERSE OPERATIONS
 * 
 * 
 */
	
	public static double[] LCH_to_LAB(double[] LCH){
        // Convert from polar form
        double[] LAB = {
    		LCH[0], // L is still L
    		LCH[1] * Math.cos(LCH[2] * Math.PI / 180d), // a
    		LCH[1] * Math.sin(LCH[2] * Math.PI / 180d) // b
    	};
    	
    	return LAB;
    }
    
    public static double[] D50_to_D65(double[] Orig_XYZ) {
    	// Bradford chromatic adaptation from D50 to D65
    	double[][] M = {
    		{  0.9554734527042182,   -0.023098536874261423,  0.0632593086610217   },
    		{ -0.028369706963208136,  1.0099954580058226,    0.021041398966943008 },
    		{  0.012314001688319899, -0.020507696433477912,  1.3303659366080753   }
    	};
        double[][] XYZ = {{Orig_XYZ[0]},{Orig_XYZ[1]},{Orig_XYZ[2]}};
    
        double[][] result_raw = MultiplyMatrices.multiplyMatrices(M, XYZ);
        double[] result = new double[result_raw.length * result_raw[0].length];
        for(int i1 = 0;i1 < result_raw.length;i1++) {
    			for(int i2 = 0;i2 < result_raw[0].length;i2++) {
    				result[i1*result_raw[0].length + i2] = result_raw[i1][i2]*1;
    			}
    		}
    	return result;
    }
    
    public static double[] LAB_to_XYZ(double[] LAB){
        // Convert Lab to D50-adapted XYZ
    	// http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
    	double EPSILON = 216d/24389d;  // 6^3/29^3
    	double KAPPA = 24389d/27d;   // 29^3/3^3
    	double[] f = new double[3];
    
    	// compute f, starting with the luminance-related term
    	f[1] = (LAB[0] + 16d)/116d;
    	f[0] = LAB[1]/500d + f[1];
    	f[2] = f[1] - LAB[2]/200d;
    
    	// compute xyz
    	double[] XYZ_orig = {
    		Math.pow(f[0],3) > EPSILON ? Math.pow(f[0],3) : (116d*f[0]-16d)/KAPPA,
    		LAB[0] > KAPPA * EPSILON ? Math.pow((LAB[0]+16d)/116d,3) : LAB[0]/KAPPA,
    		Math.pow(f[2],3) > EPSILON ? Math.pow(f[2],3) : (116d*f[2]-16d)/KAPPA
    	};
    	double[] XYZ = new double[3];
    	for(int i = 0;i < 3;i++){
    	    XYZ[i] = XYZ_orig[i] * D50[i];
    	}
    // 	XYZ = D50_to_D65(XYZ);
    
    	// Compute XYZ by scaling xyz by reference white
    	return XYZ;
    }
    
    private static double bufferDouble;
    public static double[] XYZ_to_RGB(double[] Orig_XYZ){
        // convert XYZ to linear-light sRGB
        Orig_XYZ = D50_to_D65(Orig_XYZ);
        
    	double[][] M = {
    		{  3.2409699419045226,  -1.537383177570094,   -0.4986107602930034  },
    		{ -0.9692436362808796,   1.8759675015077202,   0.04155505740717559 },
    		{  0.05563007969699366, -0.20397695888897652,  1.0569715142428786  }
    	};
        double[][] XYZ = {{Orig_XYZ[0]},{Orig_XYZ[1]},{Orig_XYZ[2]}};
    
        double[][] result_raw = MultiplyMatrices.multiplyMatrices(M, XYZ);
        double[] result = new double[result_raw.length * result_raw[0].length];
        for(int i1 = 0;i1 < result_raw.length;i1++) {
    			for(int i2 = 0;i2 < result_raw[0].length;i2++) {
    			    bufferDouble = result_raw[i1][i2]*255d;
    				result[i1*result_raw[0].length + i2] = bufferDouble > 255 ? 255 : bufferDouble < 0 ? 0 : bufferDouble;
    			}
    		}
    	return result;
    }
	
}
