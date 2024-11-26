package fr.siamois.utils.stratigraphy;

import fr.siamois.models.Concept;
import fr.siamois.models.RecordingUnit;
import fr.siamois.models.StratigraphicRelationship;
import jakarta.json.*;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;


import java.io.*;
import java.util.*;

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
            edgesBuilder.add(Json.createObjectBuilder()
                    .add("source", edge.getRecording_unit_1().getId())
                    .add("target", edge.getRecording_unit_2().getId())
                    .add("type", edge.getRelationship().getLabel())
            );
        }

        jsonObjectBuilder.add("nodes", nodesBuilder);
        jsonObjectBuilder.add("edges", edgesBuilder);
        JsonObject graphObject = jsonObjectBuilder.build();

        FileWriter fileWriter = new FileWriter("C:\\Users\\pccnr\\Documents\\code\\Siamois\\src\\main\\java\\fr\\siamois\\utils\\stratigraphy\\data\\graph.json");
        JsonWriter jsonWriter = Json.createWriter(fileWriter);
        jsonWriter.write(graphObject);
        jsonWriter.close();
    }

    public void importGraphFromXlsx(String filename) throws IOException {

        ArrayList<StratigraphicRelationship> edges = new ArrayList<>();;
        ArrayList<RecordingUnit> nodes = new ArrayList<>();

        FileInputStream file = new FileInputStream(filename);

        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);

        // We start with the nodes
        XSSFSheet sheetUS = workbook.getSheetAt(0);
        int nodeIndex = 0;
        Map<String, RecordingUnit> indexMap = new HashMap<>();
        boolean firstRow = true;
        for (Row row : sheetUS) {
            if (firstRow) {
                firstRow = false; // Skip the first row
                continue;
            }
            // New node
            RecordingUnit node = new RecordingUnit();
            node.setId((long) nodeIndex);
            String nodeName = row.getCell(0).getStringCellValue();
            //System.out.println(nodeName);
            indexMap.put(nodeName, node );
            nodes.add(node);
            nodeIndex++;
        }

        // Now we add edges
        XSSFSheet sheetRel = workbook.getSheetAt(1);

        Concept relationshipType1 = new Concept();
        relationshipType1.setLabel("Anterior");
        Concept relationshipType2 = new Concept();
        relationshipType2.setLabel("Synchronous");

        firstRow = true;
        for (Row row : sheetRel) {
            if (firstRow) {
                firstRow = false; // Skip the first row
                continue;
            }
            // New rel
            StratigraphicRelationship edge = new StratigraphicRelationship();
            String ru1Name = row.getCell(0).getStringCellValue();
            //System.out.println(ru1Name);
            String relType = row.getCell(1).getStringCellValue();
            String ru2Name = row.getCell(2).getStringCellValue();
            // find node RU1
            edge.setRecording_unit_1(indexMap.get(ru1Name));
            edge.setRecording_unit_2(indexMap.get(ru2Name));
            if(Objects.equals(relType, "sous") || Objects.equals(relType, "pt.être sous")) {
                edge.setRelationship(relationshipType1);
            }
            else if(Objects.equals(relType, "synchrone avec") || Objects.equals(relType, "pt.être synchrone")) {
                edge.setRelationship(relationshipType2);
            }


            edges.add(edge);

        }

        // close file
        file.close();

        this.nodes = nodes;
        this.edges = edges;
        this.matrix = AdjacencyMatrix.buildAdjacencyMatrixFromGraph(nodes, edges);
    }

    public void exportGraphAsXlsx(String filename) throws IOException {

        // Starting with US

        XSSFWorkbook workbook = new XSSFWorkbook();

        // US
        XSSFSheet sheet = workbook.createSheet("US");

        XSSFRow rowhead = sheet.createRow((short)0);
        rowhead.createCell(0).setCellValue("ID_US");
        rowhead.createCell(1).setCellValue("TYPE");
        rowhead.createCell(2).setCellValue("STATUT");
        rowhead.createCell(3).setCellValue("REF_FAIT");

        for(int i = 0; i< nodes.size() ; i++) {
            XSSFRow row = sheet.createRow((short)(i+1));
            row.createCell(0).setCellValue(String.valueOf(nodes.get(i).getId()));
        }

        // Relationships
        XSSFSheet sheet_rel = workbook.createSheet("Rel_Strati");
        XSSFRow rowhead_rel = sheet_rel.createRow((short)0);
        rowhead_rel.createCell(0).setCellValue("REF_US1");
        rowhead_rel.createCell(1).setCellValue("TYPE");
        rowhead_rel.createCell(2).setCellValue("REF_US2");

        for(int i = 0; i< edges.size() ; i++) {
            XSSFRow row = sheet_rel.createRow((short)(i+1));
            String relType = "";
            if(Objects.equals(edges.get(i).getRelationship().getLabel(), "Anterior")) {
                relType = "sous";
            }
            if(Objects.equals(edges.get(i).getRelationship().getLabel(), "Synchronous")) {
                relType = "synchrone avec";
            }
            row.createCell(0).setCellValue(String.valueOf(edges.get(i).getRecording_unit_1().getId()));
            row.createCell(1).setCellValue(relType);
            row.createCell(2).setCellValue(String.valueOf(edges.get(i).getRecording_unit_2().getId()));
        }

        workbook.createSheet("Sequences");
        workbook.createSheet("Phases");
        workbook.createSheet("Couleurs");

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
