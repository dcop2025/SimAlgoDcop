package crypto.utils;


/**
 * A static class that is a collection of math functions where the results are module p
 * There is an assumption that p is 2^31 - 1
 */
public class OModMath {

    private OModMath() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static long add(final long a, final long b, final long p) {
    	long res = a + b;
    	if (res >= p) {
    		res = res - p;
    	}
        return res;
    }

    public static long sub(final long a, final long b, final long p) {
    	long res = a - b;
    	if (res < 0) {
    		res = res + p;
    	}
        return res;
    }

    
    public static long modInverse(long a, long prime) {
        long m0 = prime;
        long y = 0, x = 1;

        if (prime == 1)
            return 0;

        while (a > 1) {
            // q is the quotient
            long q = a / prime;
            long t = prime;

            // Update prime as a % prime, and a as t
            prime = a % prime;
            a = t;
            t = y;

            // Update x and y
            y = x - q * y;
            x = t;
        }

        // Make x positive
        if (x < 0)
            x += m0;

        return x;
    }
    
    public static long multiply(final long a, final long b, final long prime) {
    	//return (a * b) % prime;
    	long z = a * b;
    	long z1 = z & prime;
    	long z2 = z >> 31;
        
        return add(z1, z2, prime);
    } 
}

