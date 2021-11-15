package maze;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**Create graph, where each node is connected to adjacent nodes.*/
public class TiedGraph {

    private static class Vertex {
        int x;
        int y;
        Set<Edge> edges = new HashSet<>();
        Set<Vertex> neighbors = new HashSet<>();

        Vertex(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void addEdge(Edge e) {
            edges.add(e);
            neighbors.add(getNeighbor(e));
        }

        //it's sufficient to add an edge to only 1 of 2 vertices.
        void addEdge(Vertex v, double weight) {
            if (neighbors.contains(v))
                return;
            Edge edge = new Edge(this, v, weight);
            this.addEdge(edge);
            v.addEdge(edge);
        }

        //should accept an edge, that is in the edges set.
        Vertex getNeighbor(Edge e) {
            if (!edges.contains(e))
                return null;
            if (e.first != this) {
                return e.first;
            } else {
                return e.second;
            }
        }

        @Override
        public String toString() {
            return x+":"+y;
        }
    }

    private static class Edge {
        double weight;
        Vertex first;
        Vertex second;

        Edge(Vertex first, Vertex second, double weight) {
            this.first = first;
            this.second = second;
            this.weight = weight;
        }

        //for debugging
        @Override
        public String toString() {
            return first + ", " + second + "| " + weight;
        }
    }

    private Vertex[][] vertices;
    private final int width;
    private final int height;
    int h;
    int w;

    /*Create graph, where each node is connected to adjacent nodes.*/
    public TiedGraph(int width, int height) {
        this.width = width;
        this.height = height;
        //if width is even, we reduce amount of vertices by 1, and then add an additional wall.
        w = width % 2 == 0 ? width / 2 - 1 : width / 2; //if width == 8 => w == 4.
        h = height % 2 == 0 ? height / 2 - 1 : height / 2;
        vertices = new Vertex[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Vertex v = new Vertex(i, j);
                vertices[i][j] = v;
            }
        }
        fillWithRandomEdges();
    }

    private void fillWithRandomEdges() {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Vertex current = vertices[i][j];
                if (i != 0) {
                    current.addEdge(vertices[i - 1][j], Math.random());
                }
                if (j != 0) {
                    current.addEdge(vertices[i][j - 1], Math.random());
                }
                if (i != w - 1) {
                    current.addEdge(vertices[i + 1][j], Math.random());
                }
                if (j != h - 1) {
                    current.addEdge(vertices[i][j + 1], Math.random());
                }
            }
        }
    }

    //get a set of edges, representing mst using Prim's algorithm
    private Set<Edge> getSpanTree() {
        Set<Vertex> leaves = new HashSet<>();
        Set<Vertex> notInTree = Stream.of(vertices)
                .flatMap(Stream::of)
                .collect(Collectors.toSet());
        leaves.add(vertices[0][0]);

        Set<Edge> tree = new HashSet<>(); //the result

        while (!notInTree.isEmpty()) {
            double smallest = 2; //because weight is 0..1
            Vertex toAdd = null;
            Edge edge = null;
            for (Vertex v : leaves) {
                for (Edge e : v.edges) {
                    if (!notInTree.contains(v.getNeighbor(e))) {
                        continue;
                    }
                    if (e.weight < smallest) {
                        edge = e;
                        smallest = e.weight;
                        toAdd = v.getNeighbor(e);
                    }
                }
            }
            tree.add(edge);
            notInTree.remove(toAdd);
            leaves.add(toAdd);
        }
        return tree;
    }

    /**Returns matrix, which can be displayed as a maze.
    * 1 - is a wall, 0 - is a path.*/
    public int[][] getMatrix() {
        Set<Edge> tree = getSpanTree();
        int[][] result = new int[height][width];//+2 because we're adding walls on the sides.
        Stream.of(result).forEach(row -> Arrays.fill(row, 1)); //fill result with ones.
        for (Edge e: tree) {
            Vertex v1 = e.first;
            Vertex v2 = e.second;
            result[v1.y*2 + 1][v1.x*2 + 1] = 0;
            result[v2.y*2 + 1][v2.x*2 + 1] = 0;
            if (v1.x == v2.x) {
                int y = Math.max(v1.y, v2.y) * 2;
                result[y - 1 + 1][v1.x * 2 + 1] = 0;
            } else {
                int x = Math.max(v1.x, v2.x) * 2;
                result[v1.y * 2 + 1][x - 1 + 1] = 0;
            }
        }
        result[1][0] = 0; //the entrance.
        if (width % 2 == 1) {
            for (int i = result.length - 1; i >= 1; i--) {
                if (result[i][width - 2] == 0) {
                    result[i][width - 1] = 0; //the exit
                    break;
                }
            }
        } else {
            for (int i = result.length - 1; i >= 1; i--) {
                if (result[i][width - 3] == 0) {
                    result[i][width - 2] = 0;  // if there was an event number of columns,
                    result [i][width - 1] = 0; // then we have 2 walls in the end
                    break;
                }
            }
        }
        return result;
    }

    //for debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                sb.append(vertices[i][j]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("\n----------------\n");
        Set<Vertex> set = Stream.of(vertices)
                .flatMap(Stream::of)
                .collect(Collectors.toSet());
        TreeSet<Vertex> vertSet = new TreeSet<>(Comparator.comparing(Vertex::toString));
        vertSet.addAll(set);
        for (Vertex v : vertSet) {
            sb.append("\n").append(v).append("- ").append(v.edges.size()).append(": ");
            for (Edge e : v.edges) {
                sb.append(v.getNeighbor(e)).append(" ");
            }
        }
        return sb.toString();
    }
}
