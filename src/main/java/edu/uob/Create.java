package edu.uob;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class Create {

    private File newFile;

    public boolean createDatabase(String filePath, String fileName, DBServer server) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase());
        if (newFile.exists()) {
            server.setErrorLine("The given directory already exists.");
            return false;
        } else if (!newFile.mkdir()) {
            server.setErrorLine("Directory could not be created.");
            return false;
        }
        return true;
    }

    public boolean createTable(String filePath, String fileName, List<String> attributeList, DBServer server) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase() + ".tab");
        try {
            if (checkHeadersForDupes(attributeList, server)) {
                return false;
            } else if (!newFile.createNewFile()) {
                server.setErrorLine("The given table already exists.");
                return false;
            }
        } catch (IOException e) {
            server.setErrorLine("Please try again.");
            return false;
        }
        Table newTable = new Table(newFile, attributeList, server.getCurrentDatabase());
        server.addTable(newTable);

        if (!newTable.saveToFile(newFile)) {
            server.setErrorLine("Could not create table, please try again.");
            return false;
        }

        return true;
    }

    public boolean checkHeadersForDupes(List<String> headers, DBServer server) {
        for (String header : headers) {
            if (Collections.frequency(headers, header) > 1) {
                if (header.equalsIgnoreCase("id")) {
                    server.setErrorLine("Cannot set id as header in table.");
                } else {
                    server.setErrorLine("One or more column headers have been duplicated.");
                }
                return true;
            }
        }
        return false;
    }

}
