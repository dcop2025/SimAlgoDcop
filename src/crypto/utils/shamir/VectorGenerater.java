package crypto.utils.shamir;

import java.util.Random;
import java.util.Vector;

public class VectorGenerater {
	private Vector<Generater> generators;
	
	public VectorGenerater(final int[] secrets, final int t, final long prime, Random random) {
		this.generators = new Vector<Generater>();
		for (int i = 0; i < secrets.length; i++) {
			this.generators.add(new Generater(secrets[i], t, prime, random));
		}
	}
	
	public Shared[] generate(int index) {
		Shared[] shared = new Shared[generators.size()];
		int i = 0;
		for (Generater g: generators) {
			shared[i] = g.generate(index);
			i++;
		}
		return shared;
	}

}
