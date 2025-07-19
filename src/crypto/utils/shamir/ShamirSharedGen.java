package crypto.utils.shamir;

import java.util.Random;
import java.util.Vector;

import crypto.utils.OModMath;


// 
public final class ShamirSharedGen {

    /**
     * Generates an array of Shared objects representing shares of a secret.
     *
     * @param secret the secret to be shared
     * @param t the threshold number of shares needed to reconstruct the secret
     * @param n the total number of shares to generate
     * @param prime a prime number used for modular arithmetic
     * @param random a Random object for generating random coefficients
     * @return an array of Shared objects
     */
	public static Shared[] generate(final long secret, final int t, final int n, long prime, Random random) {

		// Generates coefficients
		final long[] coeff = generatCoeff(secret, t, prime, random);

		// Generates shares
        final Shared[] shares = new Shared[n];        
        // starting from 1 as the first ID is 1
        for (int x = 1; x <= n; x++)
        {
        	/*
            long accum = secret;
            long[] powers = PowersStash.getInstance().getPowers(x);

            // starting from 1 because the 0 is the free coeff (secret)
            for (int exp = 1; exp < t; exp++)
            {
            	accum = OModMath.add(accum, OModMath.multiply(coeff[exp], powers[exp], prime), prime);
            }
            shares[x - 1] = new Shared(x, accum, secret);
            */
        	shares[x - 1] = generateShare(x, secret, coeff, prime);
        }

        return shares;		
	}
		
	public static long[] generatCoeff(final long secret, int t, long prime, Random random) {
		final long[] coeff = new long[t];
        coeff[0] = secret;
        for (int i = 1; i < t; i++)
        {
            coeff[i] = random.nextLong(prime);
        }
       
        return coeff;
	}
	
	public static Shared generateShare(final int x, final long secret, long []coeff, long prime) {
        long accum = secret;
        long[] powers = PowersStash.getInstance().getPowers(x);

        // starting from 1 because the 0 is the free coeff (secret)
        for (int exp = 1; exp < coeff.length; exp++)
        {
        	accum = OModMath.add(accum, OModMath.multiply(coeff[exp], powers[exp], prime), prime);
        }
        return new Shared(x, accum, secret);
		
	}
	
    public static long reconstruct(final Shared[] shares, final long prime)
    {
    	long accum = 0;

    	ReconstructionCoeff w = new ReconstructionCoeff(shares.length, prime);
    	
    	for (int i = 0; i < shares.length; i++) {    		
    		long wi = w.coeff(shares[i].index());
    		long local = OModMath.multiply(shares[i].share(), wi, prime);
    		accum = OModMath.add(accum, local, prime); 
    	}
    	
    	return accum;
    }

    public static long reconstruct(final Vector<Shared> shares, final long prime)
    {
    	long accum = 0;

    	ReconstructionCoeff w = new ReconstructionCoeff(shares.size(), prime);
    	
    	for (int i = 0; i < shares.size(); i++) {    		
    		long wi = w.coeff(shares.get(i).index());
    		long local = OModMath.multiply(shares.get(i).share(), wi, prime);
    		accum = OModMath.add(accum, local, prime); 
    	}
    	
    	return accum;
    }

    /*
    public static long reconstruct(final Shared[] shares, final long prime)
    {
    	long accum = 0;

    	ReconstructionCoeff w = new ReconstructionCoeff(shares.length, prime);
    	
    	for (int i = 0; i < shares.length; i++) {
    		long numerator = 1;
    		long denominator = 1;
    		for (int j = 0; j < shares.length; j++) {
    			if (j == i) {
    				continue;
    			}
    			int ii = shares[i].index();
    			int jj = shares[j].index();
    			
    			numerator = OModMath.multiply(numerator, jj, prime);
    			denominator =  OModMath.multiply(denominator, OModMath.sub(jj, ii, prime), prime);
    		}
    		
    		long ww = OModMath.multiply(numerator, OModMath.modInverse(denominator, prime), prime);
    		long www = w.coeff(shares[i].index());
    		if (ww != www) {
    			ww = 8/0;
    		}
    		long local = OModMath.multiply(shares[i].share(), ww, prime);
    		accum = OModMath.add(accum, local, prime); 
    	}
    	
    	return accum;
    }

     * 
     */
}
