package dcop;

import java.util.Vector;

import java.util.Iterator;
import java.util.Map;

/*
 * Problem is a lean representation of a DCOP problem
 */
public class Problem {

	/*
	 * ConstraintsMatrix holds the matrix of constraints between two agent
	 */
	public class ConstraintsMatrix {
		public int a;
		public int b;
		public boolean zero;
		
		public int[][] matrix;
		private int n;
		private int m;
		
		public ConstraintsMatrix(int a, int b, int n, int m, boolean zero) {
			this.a = a;
			this.b = b;
			this.n = n;
			this.m = m;
			matrix = new int[n][m];
			this.zero = zero;
		}
		
		public int[][] revMatrix() {
			int[][] rev = new int[m][n];
			
		    for (int i = 0; i < n; i++) {
		    	for (int j = 0; j < m; j++) {
		    		rev[j][i] = this.matrix[i][j];
		    	}
		    }
			
			return rev;
		}
		
		public int domainPowerAgentA() {
			return n;
		}
		
		public int domainPowerAgentB() {
			return m;
		}

		public boolean zero() {
			return zero;
		}
		
		public int getOtherId(int id) {
			if (id == this.a) {
				return b;
			}
			return a;
		}

		public int getOtherDomain(int id) {
			if (id == this.a) {
				return m;
			} 
			return n;
		}
		
		public int getPowerDomain(int id) {
			if (id == this.a) {
				return n;
			} 
			return m;
		}
	
		public int[][] getMatrix(int id) {
			if (id == this.a) {
				return matrix;
			}
			return revMatrix();
		}
		
		public int getConstraint(int originId, int x, int y) {
			if (originId == a) {
				return matrix[x][y];
			}
			return matrix[y][x];
		}
	} 
	
	public Problem() {
		constraints = new Vector<Problem.ConstraintsMatrix>();
	}
	
	private Vector<ConstraintsMatrix> constraints;

	public void addConstraints(ConstraintsMatrix matrix) {
		constraints.add(matrix);
	}
	
	public Iterator<ConstraintsMatrix> iterator() {
		return constraints.iterator();
	}
	
	public int solve(Map<Integer, Integer> assigments) {
		int toatlCost = 0;
		for (Problem.ConstraintsMatrix iter: this.constraints) {
			if (iter.zero) {
				continue;
			}
			Integer aIndex = assigments.get(iter.a);
			Integer bIndex = assigments.get(iter.b);
			int cost = iter.matrix[aIndex][bIndex];
			toatlCost = toatlCost + cost;
		}
		return toatlCost;
	}
}
