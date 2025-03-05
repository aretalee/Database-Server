package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {

    public void readFile(File file, Table currentTable) throws IOException {

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // for buffered reader --> read all lines by checking if line is null

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

        } catch (FileNotFoundException e){
            throw new FileNotFoundException(file.getName());
        } catch (IOException e) {
            throw new IOException(file.getName());
        }

    }

    // should file parser be in here? or new class

    public void writeTableToFile(File chosenFile, Table currentTable) throws IOException {

        try {
            FileWriter fileWriter = new FileWriter(chosenFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // for buffered reader --> read all lines by checking if line is null

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

        } catch (FileNotFoundException e){
            throw new FileNotFoundException(chosenFile.getName());
        } catch (IOException e) {
            throw new IOException(chosenFile.getName());
        }

        // remember to add in try-catch block
    }

    public void populateWithExistingFiles(DBServer server) throws IOException {
        File[] storageFolder = (new File(server.getStorageFolderPath())).listFiles();

        if (storageFolder != null) {
            for (File databaseFile : storageFolder) {
                Database thisDatabase = new Database(databaseFile);
                server.getAllDatabases().add(thisDatabase);
                addTableObjects(server, thisDatabase.getDatabaseName());
            }
        }


    }

    public void addTableObjects(DBServer server, String databaseName) throws IOException {
        String databasePath = server.getStorageFolderPath() + File.separator + databaseName;
        File[] databaseFolder = (new File(databasePath)).listFiles();

        if (databaseFolder != null) {
            for (File tableFile : databaseFolder) {
                List<String> attributeList = new ArrayList<String>();
                Table thisTable = new Table(tableFile, attributeList);
                thisTable.loadTableData();
                server.getAllTables().add(thisTable);
            }
        }

    }

}
