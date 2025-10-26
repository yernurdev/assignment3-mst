package org.example.mst.graph;

import java.util.*;

public class GraphUtils {


    public static boolean hasPathDFS(Map<String, List<Edge>> adj, String src, String dst) {
        if (src.equals(dst)) return true;
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(src);
        while (!stack.isEmpty()) {
            String u = stack.pop();
            if (!visited.add(u)) continue;
            for (Edge e : adj.get(u)) {
                if (e.to.equals(dst)) return true;
                if (!visited.contains(e.to)) stack.push(e.to);
            }
        }
        return false;
    }


    public static boolean isConnectedSubgraph(int expectedVertices, Map<String, List<Edge>> mstAdj, String start) {
        if (start == null) return expectedVertices == 0;
        Set<String> vis = new HashSet<>();
        Deque<String> dq = new ArrayDeque<>();
        dq.add(start);
        while (!dq.isEmpty()) {
            String u = dq.poll();
            if (!vis.add(u)) continue;
            for (Edge e : mstAdj.getOrDefault(u, List.of())) dq.add(e.to);
        }
        return vis.size() == expectedVertices;
    }
}
