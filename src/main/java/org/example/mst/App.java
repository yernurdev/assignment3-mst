package org.example.mst;

import org.example.mst.algorithms.Kruskal;
import org.example.mst.algorithms.Prim;
import org.example.mst.graph.Edge;
import org.example.mst.graph.Graph;
import org.fasterxml.jackson.annotation.JsonCreator;
import org.fasterxml.jackson.annotation.JsonProperty;
import org.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class App {

    /* ===== Input DTO ===== */
    public static class InputEdge {
        public final String from, to; public final int weight;
        @JsonCreator public InputEdge(@JsonProperty("from") String from,
                                      @JsonProperty("to") String to,
                                      @JsonProperty("weight") int weight) {
            this.from = from; this.to = to; this.weight = weight;
        }
    }
    public static class InputGraph {
        public final int id; public final List<String> nodes; public final List<InputEdge> edges;
        @JsonCreator public InputGraph(@JsonProperty("id") int id,
                                       @JsonProperty("nodes") List<String> nodes,
                                       @JsonProperty("edges") List<InputEdge> edges) {
            this.id = id; this.nodes = nodes; this.edges = edges;
        }
    }
    public static class InputRoot {
        public final List<InputGraph> graphs;
        @JsonCreator public InputRoot(@JsonProperty("graphs") List<InputGraph> graphs) {
            this.graphs = graphs;
        }
    }

    /* ===== Output DTO ===== */
    public static class Stats { public int vertices; public int edges; }
    public static class AlgoOut {
        public List<Edge> mst_edges; public int total_cost; public long operations_count; public double execution_time_ms;
    }
    public static class Item {
        public int graph_id; public Stats input_stats; public AlgoOut prim; public AlgoOut kruskal;
    }
    public static class Out { public List<Item> results = new ArrayList<>(); }

    /* ===== Helpers ===== */
    private static InputRoot read(ObjectMapper om, File f) throws Exception {
        if (!f.exists() || Files.size(f.toPath())==0) return new InputRoot(List.of());
        return om.readValue(f, InputRoot.class);
    }
    private static Graph toGraph(InputGraph ig) {
        List<Edge> es = new ArrayList<>();
        for (InputEdge e : ig.edges) es.add(new Edge(e.from, e.to, e.weight));
        return new Graph(ig.id, ig.nodes, es);
    }

    private static Out runFile(ObjectMapper om, String inPath) throws Exception {
        InputRoot root = read(om, new File(inPath));
        Out out = new Out();
        for (InputGraph ig : root.graphs) {
            Graph g = toGraph(ig);
            Kruskal.Result kr = Kruskal.run(g);
            Prim.Result    pr = Prim.run(g, null);

            Item item = new Item();
            item.graph_id = g.getId();
            item.input_stats = new Stats();
            item.input_stats.vertices = g.V();
            item.input_stats.edges    = g.E();

            item.prim = new AlgoOut();
            item.prim.mst_edges = pr.mstEdges;
            item.prim.total_cost = pr.totalCost;
            item.prim.operations_count = pr.operations;
            item.prim.execution_time_ms = pr.timeMillis;

            item.kruskal = new AlgoOut();
            item.kruskal.mst_edges = kr.mstEdges;
            item.kruskal.total_cost = kr.totalCost;
            item.kruskal.operations_count = kr.operations;
            item.kruskal.execution_time_ms = kr.timeMillis;

            out.results.add(item);
        }
        return out;
    }

    private static void writeJson(ObjectMapper om, Out out, String outPath) throws Exception {
        File f = new File(outPath);
        f.getParentFile().mkdirs();
        om.writerWithDefaultPrettyPrinter().writeValue(f, out);
    }

    private static void appendSummaryCSV(Out out, String sizeLabel, String csvPath) throws Exception {
        File f = new File(csvPath);
        boolean writeHeader = !f.exists() || f.length() == 0;
        try (FileWriter fw = new FileWriter(f, true)) {
            if (writeHeader) {
                fw.write("size,graph_id,V,E,total_cost,prim_ms,prim_ops,kruskal_ms,kruskal_ops\n");
            }
            for (Item it : out.results) {
                fw.write(String.format("%s,%d,%d,%d,%d,%.3f,%d,%.3f,%d\n",
                        sizeLabel,
                        it.graph_id,
                        it.input_stats.vertices,
                        it.input_stats.edges,
                        it.prim.total_cost,              // cost одинаковый
                        it.prim.execution_time_ms,
                        it.prim.operations_count,
                        it.kruskal.execution_time_ms,
                        it.kruskal.operations_count
                ));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper om = new ObjectMapper();

        // Пути как на твоём скрине
        String inSmall  = "input/small_graphs.json";
        String inMedium = "input/medium_graphs.json";
        String inLarge  = "input/large_graphs.json";

        String outSmall  = "output/output_small_graphs.json";
        String outMedium = "output/output_medium_graphs.json";
        String outLarge  = "output/output_large_graphs.json";
        String summary   = "output/summary.csv";

        Out small  = runFile(om, inSmall);
        Out medium = runFile(om, inMedium);
        Out large  = runFile(om, inLarge);

        writeJson(om, small,  outSmall);
        writeJson(om, medium, outMedium);
        writeJson(om, large,  outLarge);

        appendSummaryCSV(small,  "small",  summary);
        appendSummaryCSV(medium, "medium", summary);
        appendSummaryCSV(large,  "large",  summary);

        System.out.println("Wrote:");
        System.out.println("  " + outSmall);
        System.out.println("  " + outMedium);
        System.out.println("  " + outLarge);
        System.out.println("  " + summary);
    }
}
