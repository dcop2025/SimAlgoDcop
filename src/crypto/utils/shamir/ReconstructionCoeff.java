package crypto.utils.shamir;

import crypto.utils.OModMath;

public class ReconstructionCoeff {

	public ReconstructionCoeff(int count, long prime) {
		loadCoeff(count, prime);
	}
	
	private void loadCoeff(int count, long prime) {
		this.coeff = new long[count+1];
		 
		for (int i = 1 ; i <= count; i++) {
			loadCoeffIndex(i, count, prime);
		}		
	}
	
	private void loadCoeffIndex(int index, int count, long prime) {
		long numerator = 1;
		long denominator = 1;
		for (int j = 1; j <= count; j++) {
			if (j == index) {
				continue;
			}
			numerator = OModMath.multiply(numerator, j, prime);
			denominator =  OModMath.multiply(denominator, OModMath.sub(j, index, prime), prime);
		}
		
		numerator = numerator % prime; // numerator is PI(j) j| { {t}\index
		denominator = denominator % prime; // denominator is PI(j-index) | { {t}\index
		coeff[index] = OModMath.multiply(numerator, OModMath.modInverse(denominator, prime), prime);
	}
			
	public long coeff(int index) {
		return coeff[index];
	}
	
	
	private long[] coeff;

}
