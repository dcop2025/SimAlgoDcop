package crypto.utils.shamir;

import crypto.utils.OModMath;

// Shared class encapsulates a Shamir's Shared Secret  
public final class Shared {

	// The index of the share
	private final int index; 
	// The shared value
	private final long share;
	// The real value behind the secret, used for debugging and mock flows
	private final long real; 

    /**
     * Constructs a Shared with the index, share and real value
     *
     * @param index of the shared
     * @param the share
     * @param the real value behind the secret 
     */
	public Shared(final int index, final long share, final long real)
	{
		this.index = index;
		this.share = share;
		this.real = real;
	}
	
	  /**
     * Returns the index of the shared.
     *
     * @return the index of the shared.
     */
    public int index() {
        return index;
    }

    /**
     * Returns the shared
     *
     * @return the shared
     */
    public long share() {
        return share;
    }

    /**
     * Returns the real value
     *
     * @return the real value
     */
    public long real() {
        return real;
    }

    /**
     * Returns a string representation of the Shared.
     *
     * @return a string representation of the Shared
     */
    @Override
    public String toString() {
        return "SecretShare [num=" + index + ", share=" + share + ", real=" + real + "]";
    }
    
    public Shared add(Shared other, long prime) {   
    	return new Shared(index, OModMath.add(share, other.share, prime), OModMath.add(real, other.real, prime));
    }
    
    public Shared sub(Shared other, long prime) {
    	return new Shared(index, OModMath.sub(share, other.share, prime), OModMath.sub(real, other.real, prime));
    }
    
    public Shared multiply(Shared other, long prime) {
    	return new Shared(index, OModMath.multiply(share, other.share(), prime) , OModMath.multiply(real(), other.real(), prime));
    }
    
    public Shared constMultiply(int c, long prime) {
    	return new Shared(index, OModMath.multiply(share, c, prime) , OModMath.multiply(real(), c, prime));
    }
}
