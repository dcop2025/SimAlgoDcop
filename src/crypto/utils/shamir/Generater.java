package crypto.utils.shamir;

import java.util.Random;

public class Generater {
	
	long[] coeff;
	long   secret;
	long   prime;
	Random random;
	
	public Generater(final long secret, int t, long prime, Random random) {
		this.coeff = ShamirSharedGen.generatCoeff(secret, t, prime, random);
		this.secret = secret;
		this.prime = prime;
		this.random = random;
	}
		
	public Shared generate(int index) {
		return ShamirSharedGen.generateShare(index, this.secret, this.coeff, this.prime);
	}
	
}
