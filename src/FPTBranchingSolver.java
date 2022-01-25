import tc.wata.data.*;
import tc.wata.util.*;

public class FPTBranchingSolver extends Solver {

	public static long counter = 0;

	@Override
	public void solve(Graph g) {
		ReductionRoot.reduce(g);
		if (ub <= LowerBound.lowerBound(g))
			return;
		if (g.m() == 0) {
			update(g.getS());
			return;
		}
		Graph[] gs = g.decompose(g.n() < g.n * 0.5);
		if (gs != null) {
			if (ReductionRoot.DEBUG) {
				int[] size = new int[gs.length];
				for (int i = 0; i < gs.length; i++)
					size[i] = gs[i].n;
				System.err.printf("decompose: %s%n", Utils.toString(size, " + "));
			}
			IntArray tmp = new IntArray();
			for (int i = 0; i < g.n; i++)
				if (g.used[i] == 'S')
					tmp.add(i);
			for (int i = 0; i < gs.length; i++) {
				Graph h = gs[i];
				FPTBranchingSolver solver = new FPTBranchingSolver();
				solver.ub = ub - tmp.length;
				if (outputUB && i == gs.length - 1) {
					solver.outputUB = true;
					solver.add = add + tmp.length;
				}
				solver.solve(h);
				if (solver.res == null)
					return;
				for (int j : solver.res)
					tmp.add(h.id[j]);
			}
			ub = tmp.length;
			res = tmp.toArray();
			return;
		}
		ReductionRoot.DEBUG = false;
		int s = -1;
		for (int i = 0; i < g.n; i++)
			if (g.adj[i].length > 0) {
				if (s < 0 || g.adj[s].length < g.adj[i].length)
					s = i;
			}
		count();
		Graph g1 = new Graph(g), g2 = g;
		// Debug.print("S", s);
		g1.setS(s);
		solve(g1);
		// Debug.print("F", s);
		g2.setF(s);
		solve(g2, s);
	}

	void solve(Graph g, int s) {
		Reduction.reduce(g, ub);
		if (ub <= LowerBound.lowerBound(g))
			return;
		if (g.m() == 0) {
			update(g.getS());
			return;
		}
		if (g.adj[s].length == 0) {
			solve(g);
			return;
		}
		Graph[] gs = g.decompose(false);
		if (gs != null) {
			IntArray tmp = new IntArray();
			for (int i = 0; i < g.n; i++)
				if (g.used[i] == 'S')
					tmp.add(i);
			for (int i = 0; i < gs.length; i++) {
				Graph h = gs[i];
				FPTBranchingSolver solver = new FPTBranchingSolver();
				solver.ub = ub - tmp.length;
				if (outputUB && i == gs.length - 1) {
					solver.outputUB = true;
					solver.add = add + tmp.length;
				}
				int s2 = -1;
				for (int j = 0; j < h.n; j++)
					if (h.adj[j].length > 0 && h.used[j] == 'F') {
						s2 = j;
					}
				if (s2 >= 0)
					solver.solve(h, s2);
				else
					solver.solve(h);
				if (solver.res == null)
					return;
				for (int j : solver.res)
					tmp.add(h.id[j]);
			}
			ub = tmp.length;
			res = tmp.toArray();
			return;
		}

		int v = -1;
		for (int u : g.adj[s]) {
			if (v < 0 || g.adj[v].length < g.adj[u].length)
				v = u;
		}
		count();
		Graph g1 = new Graph(g), g2 = g;
		// Debug.print("+S", v);
		g1.setS(v);
		solve(g1, s);
		// Debug.print("+F", v);
		g2.contract(v, s);
		solve(g2, s);
	}

	void count() {
		if (Long.bitCount(++counter) == 1)
			;
		// Debug.print("#branch", counter);
		// if (Long.bitCount(++counter) == 1)
		;
	}

}
