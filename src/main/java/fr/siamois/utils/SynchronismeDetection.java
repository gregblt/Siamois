package fr.siamois.utils;

import fr.siamois.SiamoisApplication;
import fr.siamois.models.Concept;
import fr.siamois.models.RecordingUnit;
import fr.siamois.models.StratigraphicRelationship;
import fr.siamois.models.StratigraphicRelationshipKey;
import fr.siamois.repositories.RecordingUnitRepository;
import fr.siamois.services.RecordingUnitService;
import fr.siamois.services.SpatialUnitService;
import fr.siamois.utils.stratigraphy.AdjacencyMatrix;
import fr.siamois.utils.stratigraphy.GenerateStratigraphyTestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SynchronismeDetection {


    private static final int ANTERIOR_OR_POSTERIOR = 1; // Valeur pour un synchronisme
    private static final int SYNCHRONISME = 2; // Valeur pour un synchronisme
    private static final int PROCESSED = 3;     // Valeur pour un synchronisme marqué
    // Detect all cycles in a graph using an adjacency matrix

    public static List<List<Integer>> detectAllCycles(int[][] graph) {
        int n = graph.length; // Number of nodes
        boolean[] visited = new boolean[n];
        boolean[] recStack = new boolean[n];
        List<Integer> currentPath = new ArrayList<>();
        List<List<Integer>> allCycles = new ArrayList<>();

        // Perform DFS for each node
        for (int node = 0; node < n; node++) {
            if (!visited[node]) {
                dfs(node, graph, visited, recStack, currentPath, allCycles);
            }
        }

        return allCycles;
    }

    private static void dfs(int node, int[][] graph, boolean[] visited, boolean[] recStack,
                     List<Integer> currentPath, List<List<Integer>> allCycles) {

        // Mark the node as visited and add to the recursion stack
        visited[node] = true;
        recStack[node] = true;
        currentPath.add(node);

        // Explore neighbors
        for (int neighbor = 0; neighbor < graph.length; neighbor++) {
            if (graph[node][neighbor] == 1) { // Check if edge exists
                if (!visited[neighbor]) {
                    // Recursive call to continue DFS
                    dfs(neighbor, graph, visited, recStack, currentPath, allCycles);
                } else if (recStack[neighbor]) {
                    // Cycle detected; extract the cycle
                    List<Integer> cycle = extractCycle(currentPath, neighbor);
                    allCycles.add(cycle);
                }
            }
        }

        // Remove the node from the recursion stack and path
        recStack[node] = false;
        currentPath.remove(currentPath.size() - 1);
    }

    // Extracts the cycle from the current path
    private static List<Integer> extractCycle(List<Integer> currentPath, int startNode) {
        int startIndex = currentPath.indexOf(startNode);
        return new ArrayList<>(currentPath.subList(startIndex, currentPath.size()));
    }

    public static void computeDistance(int[][] matrice) {
        int n = matrice.length;
        boolean updated;
        int[][] matriceNew = matrice;


        do {
            updated = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (matrice[i][j] > 0 && matrice[j][k] > 0 && matrice[i][k] <= matrice[j][k]) {
                            if(i == j) {
                                System.out.println("Loop caused by "+i+" and "+j);
                                System.exit(1);
                            }
                            matrice[i][k] = matrice[j][k] + 1;
                            System.out.println("Updates path:");
                            //afficherMatrice(matrice);
                            updated = true;
                        }
                    }
                }
            }
            System.out.println("Updates end scan:");
            //afficherMatrice(matrice);
        }
        while(updated);

    }

    public static void relationOrdre(int[][] matrice) {
        int n = matrice.length;
        boolean updated;
        int[][] matriceNew = matrice;


        do {
            updated = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrice[i][j] > 0) {
                        if(i == j) {
                            System.out.println("Loop caused by "+i+1+" and "+j+1);
                            System.exit(1);
                        }
                    }
                    else {
                        for (int k = 0; k < n; k++) {
                            if (matrice[j][k] > 0 && matrice[i][k] <= matrice[j][k]) {
                                matrice[i][k] = matrice[j][k] + 1;
                                System.out.println("Updates path:");
                                //afficherMatrice(matrice);
                                updated = true;
                            }
                        }
                    }

                }
            }
            System.out.println("Updates end scan:");
            //afficherMatrice(matrice);
        }
        while(updated);

    }

    // made by reading the thesis
    public static void detectSynchronousRelationshipsV1(int[][] matrice) {
        int n = matrice.length;
        boolean updated;
        int[][] matriceNew = matrice;

        do {
            updated = false;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrice[i][j] == SYNCHRONISME) {
                        //System.out.println(i+1);
                        //System.out.println(j+1);
                        // Symétrie
                        if (matrice[j][i] <= ANTERIOR_OR_POSTERIOR) { // If i,j has not been marked as synchronous
                            matrice[j][i] = SYNCHRONISME;
                            updated = true;
                            System.out.println("Updates 1:");
                            //afficherMatrice(matrice);
                        }

                        // Transitivité
                        for (int k = 0; k < n; k++) {
                            if (matrice[j][k] > ANTERIOR_OR_POSTERIOR  && matrice[i][k] <= ANTERIOR_OR_POSTERIOR) {
                                matrice[i][k] = SYNCHRONISME;
                                updated = true;
                                System.out.println("Updates 2:");
                                //afficherMatrice(matrice);
                            }
                        }
                        matrice[i][j] = PROCESSED; // Marquer le synchronisme traité
                        System.out.println("Updates:");
                        //afficherMatrice(matrice);

                    }
                }
            }
            System.out.println("Updates end scan:");
            //afficherMatrice(matrice);
        } while (updated);


        // Fusion des ensembles synchrones
        //fusionnerSynchronismes(matrice);
    }

    // made by comparing with stratifiant code
    public static void detectSynchronousRelationshipsV2(int[][] matrice) {
        int n = matrice.length;
        boolean updated;
        int[][] matriceNew = matrice;

        do {
            updated = false;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrice[i][j] == SYNCHRONISME && i != j) { // if i,j is synchronous but not processed
                        //System.out.println(i+1);
                        //System.out.println(j+1);
                        // Symétrie
                        if (matrice[j][i] <= ANTERIOR_OR_POSTERIOR) { // If j,i has not been marked as proccessed or as synchronous
                            matrice[j][i] = SYNCHRONISME;
                            updated = true;
                            //System.out.println("Updates 1:");
                            //afficherMatrice(matrice);
                        }

                        // Transitivité
                        for (int k = 0; k < n; k++) {
                            if ( (matrice[j][k] > ANTERIOR_OR_POSTERIOR  || matrice[k][j] > ANTERIOR_OR_POSTERIOR)
                            && matrice[i][k] <= ANTERIOR_OR_POSTERIOR) { // if not marked as synchronous yet
                                matrice[i][k] = SYNCHRONISME;
                                updated = true;
                                //System.out.println("Updates 2:");
                                //afficherMatrice(matrice);
                            }
                        }
                        matrice[i][j] = PROCESSED; // Marquer le synchronisme traité
                        //System.out.println("Updates:");
                        //afficherMatrice(matrice);

                    }
                }
            }
            //System.out.println("Updates end scan:");
            //afficherMatrice(matrice);
        } while (updated);


        // Fusion des ensembles synchrones
        //fusionnerSynchronismes(matrice);
    }

    // from thesis
    private static void fusionnerSynchronismes(int[][] matrice) {
        int n = matrice.length;


        for (int i = 0; i < n; i++) {
            if (matrice[i][i] == PROCESSED) { // US maître détectée
                for (int j = 0; j < n; j++) {
                    if (i != j && matrice[i][j] == PROCESSED) {
                        // Transférer les relations d'antéro-postériorité
                        for (int k = 0; k < n; k++) {
                            if (matrice[j][k] == ANTERIOR_OR_POSTERIOR) { // Relation antérieure
                                matrice[i][k] = ANTERIOR_OR_POSTERIOR;
                            }
                            if (matrice[k][j] == ANTERIOR_OR_POSTERIOR) { // Relation postérieure
                                matrice[k][i] = ANTERIOR_OR_POSTERIOR;
                            }
                        }

                        // Neutraliser l'US esclave
                        Arrays.fill(matrice[j], 0);
                        for (int k = 0; k < n; k++) {
                            matrice[k][j] = 0;
                        }
                    }
                }
                matrice[i][i] = 0; // Remove marquage
            }
        }
    }

    // from stratifiant 0.5
    private static void fusionnerSynchronismesV2(int[][] matrice) {
        int n = matrice.length;


        for (int i = 0; i < n; i++) {
            if (matrice[i][i] >= SYNCHRONISME) { // US maître détectée
                for (int j = 0; j < n; j++) {
                    if (i != j && matrice[i][j] >= SYNCHRONISME) {
                        // Transférer les relations d'antéro-postériorité
                        for (int k = 0; k < n; k++) {
                            if (matrice[j][k] == ANTERIOR_OR_POSTERIOR) { // Relation antérieure
                                matrice[i][k] = ANTERIOR_OR_POSTERIOR;
                            }
                            if (matrice[k][j] == ANTERIOR_OR_POSTERIOR) { // Relation postérieure
                                matrice[k][i] = ANTERIOR_OR_POSTERIOR;
                            }
                        }

                        // Neutraliser l'US esclave
                        Arrays.fill(matrice[j], 0);
                        for (int k = 0; k < n; k++) {
                            matrice[k][j] = 0;
                        }
                    }
                }
                matrice[i][i] = 0; // Remove marquage
            }
        }
    }

    public static void afficherMatrice(int[][] matrice) {
        for (int[] ligne : matrice) {
            System.out.println(Arrays.toString(ligne));
        }
    }



    public static void main(String[] args) throws IOException {

        //AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SiamoisApplication.class);
        //RecordingUnitService recordingUnitService = context.getBean(RecordingUnitService.class);
        //recordingUnitService.save(r1);
        RecordingUnit ru1 = new RecordingUnit();
        RecordingUnit ru2 = new RecordingUnit();
        RecordingUnit ru3 = new RecordingUnit();
        RecordingUnit ru4 = new RecordingUnit();
        RecordingUnit ru5 = new RecordingUnit();
        RecordingUnit ru6 = new RecordingUnit();
        RecordingUnit ru7 = new RecordingUnit();
        RecordingUnit ru8 = new RecordingUnit();
        RecordingUnit ru9 = new RecordingUnit();

        ArrayList<RecordingUnit> nodes = new ArrayList<>();
        nodes.add(ru1);
        nodes.add(ru2);
        nodes.add(ru3);
        nodes.add(ru4);
        nodes.add(ru5);
        nodes.add(ru6);
        nodes.add(ru7);
        nodes.add(ru8);
        nodes.add(ru9);

        Concept relationshipType = new Concept();
        relationshipType.setLabel("Anterior");
        StratigraphicRelationship sr = new StratigraphicRelationship();
        StratigraphicRelationshipKey key = new StratigraphicRelationshipKey();
//        key.setFk_recording_unit_1_id(r1.getId());
//        key.setFk_recording_unit_2_id(r2.getId());
        sr.setRecording_unit_1(ru1);
        sr.setRecording_unit_2(ru2);
        sr.setRelationship(relationshipType);

        ArrayList<StratigraphicRelationship> edges = new ArrayList<>();
        edges.add(sr);

        GenerateStratigraphyTestData.NodeEdge nodeEdge = GenerateStratigraphyTestData.generateData(10);


        for(StratigraphicRelationship edge: nodeEdge.edges) {
            System.out.println(edge);
        }

        AdjacencyMatrix adjacencyMatrix = new AdjacencyMatrix(nodeEdge.nodes, nodeEdge.edges);
        adjacencyMatrix.exportGraphAsJson();
        adjacencyMatrix.exportGraphAsXlsx();
        int[][] matrice = adjacencyMatrix.getMatrix();
        //afficherMatrice(matrix.getMatrix());

        //System.exit(1);


        // Exemple de matrice initiale
//        int[][] matrice = {
//                {0, 0, 0, 0, 0, 0, 0, 0, 1},
//                {1, 0, 0, 0, 0, 0, 0, 0, 0},
//                {1, 0, 0, 0, 0, 0, 0, 0, 0},
//                {1, 0, 0, 0, 0, 0, 0, 0, 0},
//                {1, 1, 1, 1, 0, 0, 0, 0, 0},
//                {0, 0, 1, 1, 1, 0, 0, 0, 0},
//                {1, 0, 1, 0, 0, 1, 0, 2, 0},
//                {1, 0, 0, 1, 0, 1, 0, 0, 0},
//                {0, 0, 1, 1, 1, 1, 1, 1, 0},
//        };

/*        int[][] matrice = {
                {0, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 2, 0, 0, 0},
        };*/

//        int[][] matrice = {
//                {0, 0, 0, 0, 0, 0, 2},
//                {0, 0, 0, 0, 0, 2, 0},
//                {0, 1, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {1, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 1, 0, 0, 0},
//                {0, 1, 0, 2, 0, 0, 0},
//        };

//        int[][] matrice = {
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {1, 0, 0, 0, 0, 0, 0, 0},
//                {0, 1, 0, 0, 1, 0, 0, 0},
//                {0, 0, 1, 0, 0, 0, 0, 0},
//                {0, 0, 0, 1, 0, 0, 0, 0},
//                {0, 1, 0, 0, 0, 0, 1, 0},
//                {0, 0, 0, 0, 0, 1, 0, 0},
//                {0, 0, 1, 0, 0, 0, 0, 0},
//        };

        // From a list of relationships, create the matrix

        System.out.println("Matrice initiale :");


        //afficherMatrice(matrice);

        detectSynchronousRelationshipsV2(matrice);

        System.out.println("\nMatrice après détection des synchronismes :");
        //afficherMatrice(matrice);

        fusionnerSynchronismesV2(matrice);
        System.out.println("\nMatrice finale :");
        //afficherMatrice(matrice);

        List<List<Integer>> cycles = detectAllCycles(matrice);

        //computeDistance(matrice);
        // Print all detected cycles
        System.out.println("Detected Cycles:");
        for (List<Integer> cycle : cycles) {
            System.out.println(cycle);
        }
    }

}
