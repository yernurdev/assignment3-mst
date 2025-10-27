package org.example.mst;

import org.example.mst.algorithms.Kruskal;
import org.example.mst.algorithms.Prim;
import org.example.mst.graph.Edge;
import org.example.mst.graph.Graph;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MSTTests {

    private void assertBasicMST(Graph g) {
        Kruskal.Result kr = Kruskal.run(g);
        Prim.Result pr = Prim.run(g, null);

        assertEquals(kr.totalCost, pr.totalCost, "Costs must match");
        assertTrue(kr.operations >= 0 && pr.operations >= 0);
        assertTrue(kr.timeMillis >= 0 && pr.timeMillis >= 0);

        if (g.getNodes().size() > 0) {
            // Если связный, то |E|=V-1; если нет — допустим меньше
            assertTrue(kr.mstEdges.size() <= Math.max(0, g.V()-1));
            assertTrue(pr.mstEdges.size() <= Math.max(0, g.V()-1));
        }
    }

    @Test
    public void smallDenseGraph() {
        List<String> nodes = List.of("A","B","C","D","E");
        List<Edge> edges = List.of(
                new Edge("A","B",4), new Edge("A","C",3), new Edge("B","C",2),
                new Edge("B","D",5), new Edge("C","D",7), new Edge("C","E",8), new Edge("D","E",6)
        );
        assertBasicMST(new Graph(1, nodes, edges));
    }

    @Test
    public void sparseGraph() {
        List<String> nodes = List.of("A","B","C","D","E","F");
        List<Edge> edges = List.of(
                new Edge("A","B",1), new Edge("B","C",2), new Edge("C","D",3) // E,F изолированы
        );
        assertBasicMST(new Graph(2, nodes, edges));
    }

    @Test
    public void zeroAndNegativeWeights() {
        List<String> nodes = List.of("A","B","C","D");
        List<Edge> edges = List.of(
                new Edge("A","B",0),
                new Edge("B","C",-2),
                new Edge("C","D",1),
                new Edge("A","D",5),
                new Edge("A","C",0)
        );
        assertBasicMST(new Graph(3, nodes, edges));
    }

    @Test
    public void multiEdgesBetweenSameVertices() {
        List<String> nodes = List.of("A","B","C");
        List<Edge> edges = List.of(
                new Edge("A","B",5),
                new Edge("A","B",1),  // дубль лучше
                new Edge("B","C",2),
                new Edge("A","C",4)
        );
        assertBasicMST(new Graph(4, nodes, edges));
    }

    @RepeatedTest(3)
    public void randomStress_ConnectedBaseWithNoise() {
        Random rnd = new Random();
        int n = 20;
        List<String> nodes = new ArrayList<>();
        for (int i=0;i<n;i++) nodes.add("v"+i);

        // гарантированно связный каркас (дерево)
        List<Edge> edges = new ArrayList<>();
        for (int i=1;i<n;i++) {
            int parent = rnd.nextInt(i);
            edges.add(new Edge("v"+parent, "v"+i, rnd.nextInt(50)+1));
        }
        // добавим шумные рёбра
        for (int k=0;k<80;k++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a==b) continue;
            edges.add(new Edge("v"+a, "v"+b, rnd.nextInt(50)+1));
        }
        assertBasicMST(new Graph(100+rnd.nextInt(1000), nodes, edges));
    }
}
