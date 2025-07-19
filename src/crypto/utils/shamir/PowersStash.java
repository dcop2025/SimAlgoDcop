package crypto.utils.shamir;

import crypto.utils.OModMath;

// PowersStash is a stash that stores seq of power
// basically it a matrix of with hard coded prime of 2^31 - 1
// 
// 		1 1 1 1   1   1 ...
//      1 2 4 8  16  32 ...
//      1 3 9 27 91 243 ...
//        .
//        .
//        .
//
public class PowersStash {
	private static PowersStash instance = null;
	private long[][] powers;
	private long prime;
	
    private PowersStash() {
    	prime = (long) Math.pow(2, 31) - 1;
    	powers = new long[101][100];
        for (int i = 1; i <= 100; i++) {
            long base = 1;
            for (int j = 0; j < 100; j++) {
                powers[i][j] = base;
                base = OModMath.multiply(base, i, prime);
            }
        }
    }
    
    public static PowersStash getInstance() {
        if (instance == null) {
            instance = new PowersStash();
        }
        return instance;
    }
    
    public long[] getPowers(int number) {
        if (number < 1 || number > 100) {
            throw new IllegalArgumentException("Number must be between 1 and 100. not " + number);
        }
        return powers[number];
    }
    
}
