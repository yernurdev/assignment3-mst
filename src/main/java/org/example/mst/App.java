package org.example.mst;

import org.example.mst.algorithms.Kruskal;
import org.example.mst.algorithms.Prim;
import org.example.mst.graph.Edge;
import org.example.mst.graph.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    // ===== DTO =====
    private static class InputEdge {
        String from, to; int weight;
        InputEdge(String f, String t, int w){ from=f; to=t; weight=w; }
    }
    private static class InputGraph {
        int id; List<String> nodes; List<InputEdge> edges;
        InputGraph(int id, List<String> nodes, List<InputEdge> edges){
            this.id=id; this.nodes=nodes; this.edges=edges;
        }
    }

    // ======= SIMPLE JSON PARSER FOR OUR FIXED SCHEMA (no libs) =======
    // Expecting: { "graphs": [ { "id":N, "nodes":[...], "edges":[ {...}, {...} ] }, ... ] }
    private static String readTextFile(String path) throws IOException {
        return Files.readString(new File(path).toPath(), StandardCharsets.UTF_8);
    }

    private static List<InputGraph> parseGraphsFromJson(String json) {
        // Make tolerant to whitespace and newlines
        // 1) Extract content of "graphs": [ ... ]
        Pattern pGraphs = Pattern.compile("\"graphs\"\\s*:\\s*\\[(.*)\\]\\s*\\}?\\s*$",
                Pattern.DOTALL);
        Matcher mGraphs = pGraphs.matcher(json);
        if (!mGraphs.find()) return List.of();

        String graphsBlock = mGraphs.group(1);

        // 2) Split into top-level {...} graph objects (balanced braces)
        List<String> graphObjs = splitTopLevelObjects(graphsBlock);

        List<InputGraph> result = new ArrayList<>();
        for (String gobj : graphObjs) {
            int id = extractId(gobj);
            List<String> nodes = extractNodes(gobj);
            List<InputEdge> edges = extractEdges(gobj);
            result.add(new InputGraph(id, nodes, edges));
        }
        return result;
    }

    // Split a comma-separated sequence of {...} objects at top level
    private static List<String> splitTopLevelObjects(String s) {
        List<String> parts = new ArrayList<>();
        int brace=0, start=-1;
        for (int i=0;i<s.length();i++){
            char c = s.charAt(i);
            if (c=='{'){
                if (brace==0) start=i;
                brace++;
            } else if (c=='}'){
                brace--;
                if (brace==0 && start!=-1){
                    parts.add(s.substring(start, i+1));
                    start=-1;
                }
            }
        }
        return parts;
    }

    private static int extractId(String gobj) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(gobj);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static List<String> extractNodes(String gobj) {
        Matcher m = Pattern.compile("\"nodes\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(gobj);
        if (!m.find()) return List.of();
        String block = m.group(1);
        Matcher sm = Pattern.compile("\"([^\"]*)\"").matcher(block);
        List<String> nodes = new ArrayList<>();
        while (sm.find()) nodes.add(sm.group(1));
        return nodes;
    }

    private static List<InputEdge> extractEdges(String gobj) {
        Matcher m = Pattern.compile("\"edges\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(gobj);
        if (!m.find()) return List.of();
        String block = m.group(1);

        // match objects like {"from":"A","to":"B","weight":4}
        Matcher em = Pattern.compile(
                "\\{[^{}]*\"from\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"to\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"weight\"\\s*:\\s*(-?\\d+)\\s*\\}"
        ).matcher(block);

        List<InputEdge> edges = new ArrayList<>();
        while (em.find()){
            edges.add(new InputEdge(em.group(1), em.group(2), Integer.parseInt(em.group(3))));
        }
        return edges;
    }

    // ======= RUN ONE FILE =======
    private static List<InputGraph> readInputFile(String path) throws IOException {
        File f = new File(path);
        if (!f.exists() || Files.size(f.toPath())==0) return List.of();
        String json = readTextFile(path);
        return parseGraphsFromJson(json);
    }

    private static Graph toGraph(InputGraph ig){
        List<Edge> es = new ArrayList<>();
        for (InputEdge e : ig.edges) es.add(new Edge(e.from, e.to, e.weight));
        return new Graph(ig.id, ig.nodes, es);
    }

    private static String jsonEscape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    private static void writeOutJson(String path, List<Map<String,Object>> items) throws IOException {
        File out = new File(path);
        out.getParentFile().mkdirs();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"results\": [\n");
        for (int i=0;i<items.size();i++){
            Map<String,Object> it = items.get(i);
            @SuppressWarnings("unchecked")
            List<Edge> primEdges = (List<Edge>)((Map<String,Object>)it.get("prim")).get("mst_edges");
            @SuppressWarnings("unchecked")
            List<Edge> krEdges   = (List<Edge>)((Map<String,Object>)it.get("kruskal")).get("mst_edges");

            sb.append("    {\n");
            sb.append("      \"graph_id\": ").append(it.get("graph_id")).append(",\n");
            sb.append("      \"input_stats\": { \"vertices\": ").append(it.get("V"))
                    .append(", \"edges\": ").append(it.get("E")).append(" },\n");

            // Prim block
            sb.append("      \"prim\": {\n");
            sb.append("        \"mst_edges\": ").append(edgesToJsonArray(primEdges)).append(",\n");
            sb.append("        \"total_cost\": ").append(((Map<?,?>)it.get("prim")).get("total_cost")).append(",\n");
            sb.append("        \"operations_count\": ").append(((Map<?,?>)it.get("prim")).get("ops")).append(",\n");
            sb.append("        \"execution_time_ms\": ").append(((Map<?,?>)it.get("prim")).get("ms")).append("\n");
            sb.append("      },\n");

            // Kruskal block
            sb.append("      \"kruskal\": {\n");
            sb.append("        \"mst_edges\": ").append(edgesToJsonArray(krEdges)).append(",\n");
            sb.append("        \"total_cost\": ").append(((Map<?,?>)it.get("kruskal")).get("total_cost")).append(",\n");
            sb.append("        \"operations_count\": ").append(((Map<?,?>)it.get("kruskal")).get("ops")).append(",\n");
            sb.append("        \"execution_time_ms\": ").append(((Map<?,?>)it.get("kruskal")).get("ms")).append("\n");
            sb.append("      }\n");

            sb.append("    }");
            if (i < items.size()-1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        Files.writeString(out.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String edgesToJsonArray(List<Edge> edges){
        StringBuilder s = new StringBuilder("[");
        for (int i=0;i<edges.size();i++){
            Edge e = edges.get(i);
            s.append("{\"from\":\"").append(jsonEscape(e.from))
                    .append("\",\"to\":\"").append(jsonEscape(e.to))
                    .append("\",\"weight\":").append(e.weight).append("}");
            if (i<edges.size()-1) s.append(",");
        }
        s.append("]");
        return s.toString();
    }

    private static void appendSummaryCsv(String csvPath, String sizeLabel, List<Map<String,Object>> items) throws IOException {
        File f = new File(csvPath);
        boolean header = !f.exists() || f.length()==0;
        try (FileWriter fw = new FileWriter(f, true)) {
            if (header) fw.write("size,graph_id,V,E,total_cost,prim_ms,prim_ops,kruskal_ms,kruskal_ops\n");
            for (Map<String,Object> it : items){
                Map<String,Object> prim = cast(it.get("prim"));
                Map<String,Object> kr   = cast(it.get("kruskal"));
                fw.write(String.format(
                        "%s,%d,%d,%d,%d,%.3f,%d,%.3f,%d%n",
                        sizeLabel,
                        (int) it.get("graph_id"),
                        (int) it.get("V"),
                        (int) it.get("E"),
                        (int) prim.get("total_cost"),
                        (double) prim.get("ms"),
                        (long) prim.get("ops"),
                        (double) kr.get("ms"),
                        (long) kr.get("ops")
                ));
            }
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> cast(Object o){ return (Map<String,Object>) o; }

    // ======= PIPELINE =======
    private static List<Map<String,Object>> runOne(String inputPath) throws Exception {
        List<InputGraph> in = readInputFile(inputPath);
        List<Map<String,Object>> items = new ArrayList<>();

        for (InputGraph ig : in) {
            Graph g = toGraph(ig);
            Kruskal.Result kr = Kruskal.run(g);
            Prim.Result    pr = Prim.run(g, null);

            Map<String,Object> prim = new HashMap<>();
            prim.put("mst_edges", pr.mstEdges);
            prim.put("total_cost", pr.totalCost);
            prim.put("ops", pr.operations);
            prim.put("ms", (double) pr.timeMillis);

            Map<String,Object> kruskal = new HashMap<>();
            kruskal.put("mst_edges", kr.mstEdges);
            kruskal.put("total_cost", kr.totalCost);
            kruskal.put("ops", kr.operations);
            kruskal.put("ms", (double) kr.timeMillis);

            Map<String,Object> item = new HashMap<>();
            item.put("graph_id", ig.id);
            item.put("V", g.V());
            item.put("E", g.E());
            item.put("prim", prim);
            item.put("kruskal", kruskal);
            items.add(item);
        }
        return items;
    }

    public static void main(String[] args) throws Exception {
        String inSmall  = "input/small_graphs.json";
        String inMedium = "input/medium_graphs.json";
        String inLarge  = "input/large_graphs.json";

        String outSmall  = "output/output_small_graphs.json";
        String outMedium = "output/output_medium_graphs.json";
        String outLarge  = "output/output_large_graphs.json";
        String summary   = "output/summary.csv";

        List<Map<String,Object>> small  = runOne(inSmall);
        List<Map<String,Object>> medium = runOne(inMedium);
        List<Map<String,Object>> large  = runOne(inLarge);

        writeOutJson(outSmall,  small);
        writeOutJson(outMedium, medium);
        writeOutJson(outLarge,  large);

        appendSummaryCsv(summary, "small",  small);
        appendSummaryCsv(summary, "medium", medium);
        appendSummaryCsv(summary, "large",  large);

        System.out.println("Wrote:");
        System.out.println("  " + outSmall);
        System.out.println("  " + outMedium);
        System.out.println("  " + outLarge);
        System.out.println("  " + summary);
    }
}
