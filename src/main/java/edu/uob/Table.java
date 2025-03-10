package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table {

    private List<List<String>> tableList;
    private List<String> columnHeaders;
    private File tableFile;
    private String tableName;
    private String inWhichDatabase;
    private int latestID;

    public Table(File newTableFile, List<String> attributeList, String databaseName) {

        tableList = new ArrayList<List<String>>();
        columnHeaders = new ArrayList<String>();
        columnHeaders.addAll(attributeList);

        tableFile = newTableFile;
        if (tableFile != null) {
            tableName = tableFile.getName();
        }
        inWhichDatabase = databaseName;
        latestID = 0;

    }

    public String getTableName() {
        return tableName;
    }

    public String getWhichDatabase() {
        return inWhichDatabase;
    }

    public List<List<String>> accessTable() {
        return tableList;
    }

    public void addToTableList(List<String> row) {
        tableList.add(row);
    }

    public void removeFromTableList(List<String> row) {
        tableList.remove(row);
    }

    public int getTableIndex(List<String> row) {
        return tableList.indexOf(row);
    }

    public List<String> getTableRow(int index) {
        return tableList.get(index);
    }

    public List<String> getTableFromList(int index) {
        return tableList.get(index);
    }

    public void addColumnToRows() {
        for(List<String> row : tableList) {
            row.add("NULL");
        }
    }

    public void removeColumnFromRow(int index) {
        for (List<String> row : tableList) {
            row.remove(index);
        }
    }

    public void updateRowValue(int index, int headerIndex, String value) {
        tableList.get(index).set(headerIndex, value);
    }

    public List<String> accessColumnHeaders() {
        return columnHeaders;
    }

    public boolean hasRequestedHeader(String header) {
        return columnHeaders.contains(header.toLowerCase());
    }

    public int getHeaderIndex(String header) {
        return columnHeaders.indexOf(header.toLowerCase());
    }

    public void addToColumnHeaders(String columnHeader) {
        columnHeaders.add(columnHeader);
    }

    public void removeFromColumnHeaders(int index) {
        columnHeaders.remove(index);
    }

    public int getCurrentID() {
        return latestID;
    }

    public void setCurrentID(int currentID) {
        latestID = currentID;
    }

    public int getNextID() {
        return ++latestID;
    }

    public File getTableFile() {
        return tableFile;
    }

    public boolean loadTableData() {
        FileHandler fileHandler = new FileHandler();
        if (!fileHandler.readFile(tableFile, this)) {
            return false;
        }
        return true;
    }

    public boolean saveToFile(File file) {
        FileHandler fileHandler = new FileHandler();
        return fileHandler.writeTableToFile(file, this);
    }

}







