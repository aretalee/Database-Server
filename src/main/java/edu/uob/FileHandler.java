package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {

    public boolean readFile(File file, Table currentTable) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            int lineCounter = 1;

            while((currentLine = bufferedReader.readLine()) != null) {
                updateTableValues(currentTable, currentLine, lineCounter);
                lineCounter++;
            }
        } catch (NullPointerException | IOException e) {
            return false;
        }
        return true;
    }

    public void updateTableValues(Table currentTable, String currentLine, int lineCounter) {
        if(lineCounter == 1) {
            currentTable.setCurrentID(Integer.parseInt(currentLine));
        }
        String[] splitLine = currentLine.split("\t");
        if(lineCounter == 2) {
            for(String item : splitLine) {
                currentTable.addToColumnHeaders(item);
            }
        } else if (lineCounter >= 3) {
            List<String> thisRow = new ArrayList<String>(Arrays.asList(splitLine));
            currentTable.addToTableList(thisRow);
        }
    }

    public boolean writeTableToFile(File chosenFile, Table currentTable) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(chosenFile))) {
            String latestID = String.valueOf(currentTable.getCurrentID());
            bufferedWriter.write(latestID);
            bufferedWriter.newLine();

            List<String> columnHeaders = currentTable.accessColumnHeaders();
            for (String header : columnHeaders) {
                bufferedWriter.write(header);
                bufferedWriter.write("\t");
            }
            bufferedWriter.newLine();

            List<List<String>> tableContent = currentTable.accessTable();
            for (List<String> row : tableContent) {
                bufferedWriter.write(String.join("\t", row));
                bufferedWriter.newLine();
            }
        } catch (NullPointerException | IOException e) {
            return false;
        }
        return true;
    }

    public boolean populateWithExistingFiles(QueryHandler handler) {
        File[] storageFolder = (new File(handler.getCurrentServer().getStorageFolderPath())).listFiles();

        if (storageFolder != null && storageFolder.length > 0) {
            for (File databaseFile : storageFolder) {
                Database thisDatabase = new Database(databaseFile);
                handler.addDatabase(thisDatabase);
                if (!addTableObjects(handler, thisDatabase.getDatabaseName())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addTableObjects(QueryHandler handler, String databaseName) {
        String databasePath = handler.getCurrentServer().getStorageFolderPath() + File.separator + databaseName;
        File[] databaseFolder = (new File(databasePath)).listFiles();

        if (databaseFolder != null) {
            for (File tableFile : databaseFolder) {
                if (!addEachTable(tableFile, databaseName, handler)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addEachTable(File tableFile, String databaseName, QueryHandler handler) {
        List<String> attributeList = new ArrayList<String>();
        Table thisTable = new Table(tableFile, attributeList, databaseName);
        if (!thisTable.getTableName().equals(".DS_Store")) {
            if (!thisTable.loadTableData()) {
                return false;
            }
            handler.addTable(thisTable);
        }
        return true;
    }

}
