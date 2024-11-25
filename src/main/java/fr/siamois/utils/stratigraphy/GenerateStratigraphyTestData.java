package fr.siamois.utils.stratigraphy;

import fr.siamois.models.Concept;
import fr.siamois.models.RecordingUnit;
import fr.siamois.models.StratigraphicRelationship;

import java.util.ArrayList;



public class GenerateStratigraphyTestData {

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static class NodeEdge {
        public final ArrayList<RecordingUnit> nodes;
        public final ArrayList<StratigraphicRelationship> edges;
        public NodeEdge(ArrayList<RecordingUnit> nodes, ArrayList<StratigraphicRelationship> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    public static NodeEdge generateData(int n) {

        Concept relationshipType1 = new Concept();
        relationshipType1.setLabel("Anterior");
        Concept relationshipType2 = new Concept();
        relationshipType2.setLabel("Synchronous");

        ArrayList<RecordingUnit> nodes = new ArrayList<>();
        ArrayList<StratigraphicRelationship> edges = new ArrayList<>();

        // Create root node
        RecordingUnit rootNode = new RecordingUnit();
        rootNode.setId(0L);
        ArrayList<RecordingUnit> nodesToProcess = new ArrayList<>();
        nodes.add(rootNode);
        nodesToProcess.add(rootNode);
        int iteration = 0;

        do {
            iteration+=1;
            System.out.println("Iteration: " + iteration);
            ArrayList<RecordingUnit> newNodesToProcess = new ArrayList<>();
            for(RecordingUnit node : nodesToProcess) {
                // add a random number of nodes in relation to this node
                int nb = getRandomNumber(1,8);
                for(long i = 0; i < nb; i++) {
                    RecordingUnit newNode = new RecordingUnit();
                    newNode.setId((long) (nodes.size()+1));
                    nodes.add(newNode);
                    newNodesToProcess.add(newNode);
                    StratigraphicRelationship newEdge = new StratigraphicRelationship();
                    newEdge.setRecording_unit_1(node);
                    newEdge.setRecording_unit_2(newNode);
                    int type = getRandomNumber(1,3);
                    if(type == 1) {
                        newEdge.setRelationship(relationshipType1);
                    } else {
                        newEdge.setRelationship(relationshipType2);
                    }
                    edges.add(newEdge);
                }
            }
            nodesToProcess = newNodesToProcess;
            System.out.println("nodes.size() " + nodes.size());
        }
        while(nodes.size()<n);

        // add a cycle
        StratigraphicRelationship newEdge = new StratigraphicRelationship();
        newEdge.setRecording_unit_1(nodes.get(nodes.size()-1));
        newEdge.setRecording_unit_2(nodes.get(0));
        newEdge.setRelationship(relationshipType1);
        edges.add(newEdge);

        // Generate data for n recording units

        return new NodeEdge(nodes, edges);
    }

}
