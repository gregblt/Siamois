package fr.siamois.utils.stratigraphy;

import fr.siamois.models.RecordingUnit;
import fr.siamois.models.StratigraphicRelationship;
import jakarta.json.*;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdjacencyMatrix {

    List<RecordingUnit> nodes;
    List<StratigraphicRelationship> edges;
    int[][] matrix;

    public AdjacencyMatrix(ArrayList<RecordingUnit> nodes, ArrayList<StratigraphicRelationship> edges) {
        this.nodes = nodes;
        this.edges = edges;
        this.matrix = AdjacencyMatrix.buildAdjacencyMatrixFromGraph(nodes, edges);
    }

    public void exportGraphAsJson() throws IOException {
        // Nodes
        JsonArrayBuilder nodesBuilder = Json.createArrayBuilder();
        JsonArrayBuilder edgesBuilder = Json.createArrayBuilder();
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for(RecordingUnit node : nodes) {
            nodesBuilder.add(Json.createObjectBuilder().add("id", node.getId()).add("label", "Node "+node.getId()));
        }
        for(StratigraphicRelationship edge : edges) {
            edgesBuilder.add(Json.createObjectBuilder().add("source", edge.getRecording_unit_1().getId()).add("target", edge.getRecording_unit_2().getId()));
        }

        jsonObjectBuilder.add("nodes", nodesBuilder);
        jsonObjectBuilder.add("edges", edgesBuilder);
        JsonObject graphObject = jsonObjectBuilder.build();

        FileWriter fileWriter = new FileWriter("C:\\Users\\pccnr\\Documents\\code\\Siamois\\src\\main\\java\\fr\\siamois\\utils\\stratigraphy\\graph.json");
        JsonWriter jsonWriter = Json.createWriter(fileWriter);
        jsonWriter.write(graphObject);
        jsonWriter.close();
    }

    public void exportGraphAsXlsx() throws IOException {

        // Starting with US
        String filename = "C:\\Users\\pccnr\\Documents\\code\\Siamois\\src\\main\\java\\fr\\siamois\\utils\\stratigraphy\\graph.xls";
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("US");

        HSSFRow rowhead = sheet.createRow((short)0);
        rowhead.createCell(0).setCellValue("ID_US");
        rowhead.createCell(1).setCellValue("TYPE");
        rowhead.createCell(2).setCellValue("STATUT");
        rowhead.createCell(3).setCellValue("REF_FAIT");

        for(int i = 0; i< nodes.size() ; i++) {
            HSSFRow row = sheet.createRow((short)(i+1));
            row.createCell(0).setCellValue(nodes.get(i).getId());
        }

        FileOutputStream fileOut = new FileOutputStream(filename);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    static int[][] buildAdjacencyMatrixFromGraph(ArrayList<RecordingUnit> nodes, ArrayList<StratigraphicRelationship> edges) {

        int[][] adjacencyMatrix = new int[nodes.size()][nodes.size()];
        for (StratigraphicRelationship edge : edges) { // every edge will be a cell in the matrix
            int line;
            int col;

            line = nodes.indexOf(edge.getRecording_unit_1()); // get line index
            if (line == -1) {
                throw new RuntimeException("The node " + edge.getRecording_unit_1() + " from edge " + edge + " is not in the node list.");
            }

            col = nodes.indexOf(edge.getRecording_unit_2()); // get col index
            if (col == -1) {
                throw new RuntimeException("The node " + edge.getRecording_unit_2() + " from edge " + edge + " is not in the node list.");
            }
            if(edge.getRelationship().getLabel().equals("Anterior")) {
                adjacencyMatrix[line][col] = 1;
            }else {
                adjacencyMatrix[line][col] = 2;
            }
        }
        return adjacencyMatrix;
    }



}
