package maze;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    private static boolean mazeGenerated = false;
    private static int[][] maze;
    private final static String errorMessage = "Incorrect option. Please try again";

    public static void main(String[] args) {
        while (true) {
            displayMenu();
            int i;
            try {
                i = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println(errorMessage);
                continue;
            }
            switch (i) {
                case 1:
                    generateMaze();
                    break;
                case 2:
                    loadMaze();
                    break;
                case 3:
                    if (mazeGenerated)
                        saveMaze();
                    else
                        System.out.println(errorMessage);
                    break;
                case 4:
                    if (mazeGenerated)
                        System.out.println(mazeToString());
                    else
                        System.out.println(errorMessage);
                    break;
                case 5:
                    if (mazeGenerated)
                        System.out.println(findEscape());
                    else
                        System.out.println(errorMessage);
                    break;
                case 0:
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Incorrect option. Please try again");
            }
        }
    }


    private static String mazeToString() {
        StringBuilder sb = new StringBuilder();
        char wall = '\u2588';
        sb.append(System.lineSeparator());
        for (int[] row : maze) {
            for (int element : row) {
                switch(element) {
                    case 0:
                        sb.append("  ");
                        break;
                    case 1:
                        sb.append(wall).append(wall);
                        break;
                    case 2:
                        sb.append("//");
                        break;
                }
            }
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    private static void generateMaze() {
        System.out.println("Enter the size of a new maze");
        int size = new Scanner(System.in).nextInt();
        TiedGraph g = new TiedGraph(size, size);
        maze = g.getMatrix();
        System.out.println(mazeToString());
        mazeGenerated = true;
    }

    private static void saveMaze() {
        String path = new Scanner(System.in).nextLine();
        FileWriter fw;
        try {
            fw = new FileWriter(path);
            fw.write(mazeToString());
            fw.close();
        } catch (IOException e) {
            System.out.println(errorMessage);
        }
    }

    private static void loadMaze() {
        String path = new Scanner(System.in).nextLine();
        List<Integer[]> mazeList = new ArrayList<>();
        //Scanner s = null;
        try {
            Scanner s = new Scanner(new File(path), StandardCharsets.UTF_8);
            while (s.hasNextLine()) {
                String row = s.nextLine();
                if (row.isBlank())
                    continue;
                Integer[] arr = new Integer[row.length() / 2];
                for (int i = 0; i < row.length(); i += 2) {
                    if (row.charAt(i) == ' ') {
                        arr[i / 2] = 0;
                    } else if (row.charAt(i) == '\u2588') {
                        arr[i / 2] = 1;
                    }
                }
                mazeList.add(arr);
            }
            s.close();
        } catch (IOException e) {
            System.out.println(errorMessage);
            return;
        }
        maze = new int[mazeList.size()][mazeList.get(0).length];
        for (int i = 0; i < mazeList.size(); i++) {
            for (int j = 0; j < mazeList.get(0).length; j++) {
                maze[i][j] = mazeList.get(i)[j];
            }
        }
        mazeGenerated = true;
    }

    private static class Node {
        int x, y;
        Node prev;
        Node (int y, int x) {
            this.x = x;
            this.y = y;
        }
    }

    //find escape from the maze using DFS algorithm
    private static String findEscape() {
        //find entrance, potentially it can be anywhere on the sides of the maze.
        int[] entrance = new int[2]; //[0] = y, [1] = x
        int[] exit = new int[2];
        boolean entranceFound = false;
        boolean exitFound = false;
        for (int i = 0; i < maze.length; i++) {
            if (maze[i][0] == 0) {
                if (!entranceFound) {
                    entrance[0] = i;
                    entranceFound = true;
                } else {
                    exit[0] = i;
                    exitFound = true;
                }
            }
            int lastIndex = maze[0].length - 1;
            if (maze[i][lastIndex] == 0) {
                if (!entranceFound) {
                    entrance[1] = lastIndex;
                    entrance[0] = i;
                    entranceFound = true;
                } else {
                    exit[1] = lastIndex;
                    exit[0] = i;
                    exitFound = true;
                }
            }
        }
        if (!exitFound) {
            for (int i = 0; i < maze[0].length; i++) {
                if (maze[0][i] == 0) {
                    if (!entranceFound) {
                        entrance[1] = i;
                        entranceFound = true;
                    } else {
                        exit[1] = i;
                        exitFound = true;
                    }
                }
                int lastIndex = maze.length - 1;
                if (maze[lastIndex][i] == 0) {
                    if (!entranceFound) {
                        entrance[1] = i;
                        entrance[0] = lastIndex;
                    } else {
                        exit[1] = i;
                        exit[0] = lastIndex;
                    }
                }
            }
        }

        int h = maze.length;
        int w = maze[0].length;
        Node[][] nodesMatrix = new Node[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                nodesMatrix[i][j] = new Node(i, j);
            }
        }
        Node exitNode = nodesMatrix[exit[0]][exit[1]];
        Node startNode = nodesMatrix[entrance[0]][entrance[1]];
        Node dummy = new Node(-1, -1);
        startNode.prev = dummy;
        DFS(startNode, nodesMatrix, exitNode);
        Node node = exitNode;
        while (node.prev != null) {
            maze[node.y][node.x] = 2;
            node = node.prev;
        }
        return mazeToString();
    }

    private static void DFS(Node current, Node[][] nodesMatrix, Node exit) {
        for (int y = -1; y <= 1; y += 2) {
            if (current.y + y < 0 || current.y + y >= nodesMatrix.length)
                continue;
            Node neighbor = nodesMatrix[current.y + y][current.x];
            if (maze[neighbor.y][neighbor.x] == 1)
                continue;
            if (neighbor.prev == null) {
                neighbor.prev = current;
                if (neighbor == exit)
                    return;
                DFS(neighbor, nodesMatrix, exit);
            }
        }
        for (int x = -1; x <= 1; x += 2) {
            if (current.x + x < 0 || current.x + x >= nodesMatrix[0].length)
                continue;
            Node neighbor = nodesMatrix[current.y][current.x + x];
            if (maze[neighbor.y][neighbor.x] == 1)
                continue;
            if (neighbor.prev == null) {
                neighbor.prev = current;
                if (neighbor == exit)
                    return;
                DFS(neighbor, nodesMatrix, exit);
            }
        }
    }

    private static void displayMenu() {
        if (!mazeGenerated)
            System.out.print("=== Menu ===" +
                    "\n1. Generate a new maze" +
                    "\n2. Load a maze" +
                    "\n0. Exit" +
                    "\n>");
        else
            System.out.print("=== Menu ===" +
                    "\n1. Generate a new maze" +
                    "\n2. Load a maze" +
                    "\n3. Save the maze" +
                    "\n4. Display the maze" +
                    "\n5. Find the escape" +
                    "\n0. Exit" +
                    "\n>");
    }

}
