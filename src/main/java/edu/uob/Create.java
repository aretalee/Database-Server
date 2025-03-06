package edu.uob;

import java.io.*;
import java.util.List;

public class Create {

    private File newFile;

    public void createDatabase(String filePath, String fileName) throws IOException {

        newFile = new File(filePath + File.separator + fileName.toLowerCase());
        if (newFile.exists()) {
            throw new IOException("Directory already exists " + filePath);
        }
        else if (!newFile.mkdir()) {
            throw new IOException("Could not create directory " + filePath);
        }

    }

    public void createTable(String filePath, String fileName, List<String> attributeList, DBServer server) throws IOException {

        newFile = new File(filePath + File.separator + fileName.toLowerCase() + ".tab");
        if (newFile.exists()) {
            throw new IOException("Table already exists " + filePath);
        }
        else if (!newFile.createNewFile()) {
            throw new IOException("Could not create table " + filePath);
        }
        // add column headings to file
        Table newTable = new Table(newFile, attributeList);
        server.addTable(newTable);
        newTable.saveToFile(newFile);

    }

}
