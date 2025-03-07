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
            if (!newFile.createNewFile()) {
                server.setErrorLine("The given table already exists.");
                return false;
            } else if (checkHeadersForDupes(attributeList)) {
                server.setErrorLine("One or more column headers have been duplicated.");
                return false;
            }
        } catch (IOException e) {

        }

        Table newTable = new Table(newFile, attributeList);
        server.addTable(newTable);

        if (!newTable.saveToFile(newFile, server)) {
            server.setErrorLine("Could not create table, please try again.");
            return false;
        }

        return true;
    }

    public boolean checkHeadersForDupes(List<String> headers) {
        for (String header : headers) {
            if (Collections.frequency(headers, header) > 1) {
                return true;
            }
        }
        return false;
    }

}
