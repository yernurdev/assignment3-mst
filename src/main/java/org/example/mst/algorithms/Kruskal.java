package org.example.mst.algorithms;

import org.example.mst.graph.Edge;
import org.example.mst.graph.Graph;
import org.example.mst.graph.GraphUtils;
import org.example.mst.utils.Counter;

import java.util.*;

public class Kruskal {
    public static class Result {
        public final List<Edge> mstEdges;
        public final int totalCost;
        public final long operations;
        public final long timeMillis;

        public Result(List<Edge> e, int c, long o, long ms) {
            this.mstEdges = e; this.totalCost = c; this.operations = o; this.timeMillis = ms;
        }
    }

    public static Result run(Graph g) {
        long t0 = System.nanoTime();
        Counter ops = new Counter();

        List<Edge> sorted = new ArrayList<>(g.getEdges());
        Collections.sort(sorted);
        ops.add(sorted.size()); // грубая оценка сравнений

        // Временный MST-граф (adj-list) для проверки циклов
        Map<String, List<Edge>> mstAdj = new HashMap<>();
        for (String v : g.getNodes()) mstAdj.put(v, new ArrayList<>());

        List<Edge> mst = new ArrayList<>();
        int cost = 0;

        for (Edge e : sorted) {
            if (!GraphUtils.hasPathDFS(mstAdj, e.from, e.to)) {
                mst.add(e);
                cost += e.weight;
                mstAdj.get(e.from).add(e);
                mstAdj.get(e.to).add(new Edge(e.to, e.from, e.weight));
                if (mst.size() == g.V() - 1) break;
            }
            ops.inc(); // учтём проверку
        }

        long t1 = System.nanoTime();
        return new Result(mst, cost, ops.get(), (t1 - t0) / 1_000_000);
    }
}
