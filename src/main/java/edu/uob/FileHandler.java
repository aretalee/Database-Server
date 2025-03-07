package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {

    public boolean readFile(File file, Table currentTable) {

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String currentLine;
            int lineCounter = 1;

            while((currentLine = bufferedReader.readLine()) != null) {
                String[] splitLine = currentLine.split("\t");
                if(lineCounter == 1) {
                    for(String item : splitLine) {
                        currentTable.accessColumnHeaders().add(item);
                    }
                } else {
                    List<String> thisRow = new ArrayList<String>(Arrays.asList(splitLine));
                    currentTable.accessTable().add(thisRow);
                }
                lineCounter++;
            }
            bufferedReader.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // should I still throw error if handled?
    public boolean writeTableToFile(File chosenFile, Table currentTable) {

        try {
            FileWriter fileWriter = new FileWriter(chosenFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

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

            bufferedWriter.close();

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean populateWithExistingFiles(DBServer server) {
        File[] storageFolder = (new File(server.getStorageFolderPath())).listFiles();

        if (storageFolder != null && storageFolder.length > 0) {
            for (File databaseFile : storageFolder) {
                Database thisDatabase = new Database(databaseFile);
                server.getAllDatabases().add(thisDatabase);
                if (!addTableObjects(server, thisDatabase.getDatabaseName())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addTableObjects(DBServer server, String databaseName) {
        String databasePath = server.getStorageFolderPath() + File.separator + databaseName;
        File[] databaseFolder = (new File(databasePath)).listFiles();

        if (databaseFolder != null) {
            for (File tableFile : databaseFolder) {
                List<String> attributeList = new ArrayList<String>();
                Table thisTable = new Table(tableFile, attributeList);
                if (!thisTable.loadTableData()) {
                    return false;
                }
                server.getAllTables().add(thisTable);
            }
        }
        return true;
    }

}
