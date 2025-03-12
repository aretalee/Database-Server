package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Create {

    private File newFile;

    public boolean createDatabase(String filePath, String fileName, QueryHandler queryHandler) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase());
        if (newFile.exists()) {
            queryHandler.setErrorLine("The given directory already exists.");
            return false;
        } else if (!newFile.mkdir()) {
            queryHandler.setErrorLine("Directory could not be created.");
            return false;
        }
        return true;
    }

    public boolean createTable(String filePath, String fileName, List<String> attriList, QueryHandler queryHandler) {

        newFile = new File(filePath + File.separator + fileName.toLowerCase() + ".tab");
        if (!makeTableFile(attriList, queryHandler)) { return false; }

        Table newTable = new Table(newFile, attriList, queryHandler.getCurrentDatabase());
        queryHandler.addTable(newTable);

        if (!newTable.saveToFile(newFile)) {
            queryHandler.setErrorLine("Could not create table, please try again.");
            return false;
        }

        return true;
    }

    public boolean makeTableFile(List<String> attriList, QueryHandler queryHandler) {
        try {
            if (checkHeadersForDupes(attriList, queryHandler)) {
                return false;
            } else if (!newFile.createNewFile()) {
                queryHandler.setErrorLine("The given table already exists.");
                return false;
            }
        } catch (IOException e) {
            queryHandler.setErrorLine("Please try again.");
            return false;
        }
        return true;
    }

    public boolean checkHeadersForDupes(List<String> headers, QueryHandler queryHandler) {
        if (headers.isEmpty()) {
            return false;
        }
        List<String> lowercaseHeaders = makeHeadersLowerCase(headers);
        for (String header : lowercaseHeaders) {
            if (Collections.frequency(lowercaseHeaders, header) > 1) {
                setDupeError(header, queryHandler);
                return true;
            }
        }
        return false;
    }

    public void setDupeError(String header, QueryHandler queryHandler) {
        if (header.equalsIgnoreCase("id")) {
            queryHandler.setErrorLine("Cannot set id as header in table.");
        } else {
            queryHandler.setErrorLine("One or more column headers have been duplicated.");
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
