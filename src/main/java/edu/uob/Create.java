package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Create {

    private File newFile;

    public boolean createDatabase(String filePath, String fileName, QueryHandler handler) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase());
        if (newFile.exists()) {
            handler.setErrorLine("The given directory already exists.");
            return false;
        } else if (!newFile.mkdir()) {
            handler.setErrorLine("Directory could not be created.");
            return false;
        }
        return true;
    }

    public boolean createTable(String filePath, String fileName, List<String> attriList, QueryHandler handler) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase() + ".tab");
        if (!makeTableFile(attriList, handler)) { return false; }

        Table newTable = new Table(newFile, attriList, handler.getCurrentDatabase());
        handler.addTable(newTable);

        if (!newTable.saveToFile(newFile)) {
            handler.setErrorLine("Could not create table, please try again.");
            return false;
        }

        return true;
    }

    public boolean makeTableFile(List<String> attriList, QueryHandler handler) {
        try {
            if (checkHeadersForDupes(attriList, handler)) {
                return false;
            } else if (!newFile.createNewFile()) {
                handler.setErrorLine("The given table already exists.");
                return false;
            }
        } catch (IOException e) {
            handler.setErrorLine("Please try again.");
            return false;
        }
        return true;
    }

    public boolean checkHeadersForDupes(List<String> headers, QueryHandler handler) {
        if (headers.isEmpty()) {
            return false;
        }
        List<String> lowercaseHeaders = makeHeadersLowerCase(headers);
        for (String header : lowercaseHeaders) {
            if (Collections.frequency(lowercaseHeaders, header) > 1) {
                setDupeError(header, handler);
                return true;
            }
        }
        return false;
    }

    public void setDupeError(String header, QueryHandler handler) {
        if (header.equalsIgnoreCase("id")) {
            handler.setErrorLine("Cannot set id as header in table.");
        } else {
            handler.setErrorLine("One or more column headers have been duplicated.");
        }
    }

    public List<String>  makeHeadersLowerCase(List<String> headers) {
        List<String> lowerCase = new ArrayList<String>();
        for (String header : headers) {
            lowerCase.add(header.toLowerCase());
        }
        return lowerCase;
    }

}
