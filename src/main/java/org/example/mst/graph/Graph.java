package org.example.mst.graph;

import java.util.*;

public class Graph {
    private final List<String> nodes;
    private final List<Edge> edges;
    private final Map<String, List<Edge>> adj;

    public Graph(List<String> nodes, List<Edge> edges) {
        this.nodes = List.copyOf(nodes);
        this.edges = List.copyOf(edges);
        this.adj = new HashMap<>();
        for (String v : nodes) adj.put(v, new ArrayList<>());
        for (Edge e : edges) {
            adj.get(e.from).add(e);
            adj.get(e.to).add(new Edge(e.to, e.from, e.weight));
        }
    }

    public List<String> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public Map<String, List<Edge>> getAdj() { return adj; }
    public int V() { return nodes.size(); }
    public int E() { return edges.size(); }
}
