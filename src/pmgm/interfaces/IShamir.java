package pmgm.interfaces;

import java.util.Random;

public interface IShamir {
	Random cryptoRandom();
	int threshold();
	long prime();
}
