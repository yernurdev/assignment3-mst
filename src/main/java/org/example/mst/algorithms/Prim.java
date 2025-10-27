package org.example.mst.algorithms;

import org.example.mst.graph.Edge;
import org.example.mst.graph.Graph;
import org.example.mst.utils.Counter;

import java.util.*;

public class Prim {
    public static class Result {
        public final List<Edge> mstEdges;
        public final int totalCost;
        public final long operations;
        public final long timeMillis;

        public Result(List<Edge> e, int c, long o, long ms) {
            this.mstEdges = e; this.totalCost = c; this.operations = o; this.timeMillis = ms;
        }
    }

    public static Result run(Graph g, String start) {
        long t0 = System.nanoTime();
        Counter ops = new Counter();

        if (g.V() == 0) return new Result(List.of(), 0, 0, 0);

        Set<String> visited = new HashSet<>();
        List<Edge> mst = new ArrayList<>();
        int cost = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        String s = (start == null || start.isEmpty()) ? g.getNodes().get(0) : start;
        visited.add(s);
        pq.addAll(g.getAdj().get(s));

        while (!pq.isEmpty() && mst.size() < g.V() - 1) {
            Edge e = pq.poll(); ops.inc(); // pop
            if (visited.contains(e.to)) continue;

            visited.add(e.to);
            mst.add(e);
            cost += e.weight;

            for (Edge ne : g.getAdj().get(e.to)) {
                if (!visited.contains(ne.to)) {
                    pq.offer(ne); ops.inc(); // push
                }
            }
        }

        long t1 = System.nanoTime();
        return new Result(mst, cost, ops.get(), (t1 - t0) / 1_000_000);
    }
}
