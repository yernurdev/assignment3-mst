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
}
