package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private List<List<String>> tableList;
    private List<String> columnHeaders;
    private File tableFile;
    private String tableName;
    private String inWhichDatabase;
    // File object or use file path?
    private int latestID;

    // if creating new file: call CREATE, then make new Table + feed in File from CREATE method
    // if existing file: make new Table + feed in file (using new File(file path) ?), then call load data method

    // is there a way to format table so that this list isn't needed
    // maybe move attributeList out of constructor?
    public Table(File newTableFile, List<String> attributeList, String databaseName) {

        tableList = new ArrayList<List<String>>();
        columnHeaders = new ArrayList<String>();
        columnHeaders.addAll(attributeList);

        tableFile = newTableFile;
        if (tableFile != null) {
            tableName = tableFile.getName();
        }
        inWhichDatabase = databaseName;
        setID();

    }

    public void setID() {
        if (tableList.isEmpty()) {
            this.latestID = 0;
        } else {
            List<String> lastRow = tableList.get(tableList.size() - 1);
            String id = lastRow.get(0);
            this.latestID = Integer.parseInt(id);
        }
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

    public void addNullToRows() {
        for(List<String> row : tableList) {
            row.add(null);
        }
    }

    public void removeTableRow(int index) {
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
        return columnHeaders.contains(header);
    }

    public int getHeaderIndex(String header) {
        return columnHeaders.indexOf(header);
    }

    public void addToColumnHeaders(String columnHeader) {
        columnHeaders.add(columnHeader);
    }

    public void removeFromColumnHeaders(int index) {
        columnHeaders.remove(index);
    }

    public int getCurrentID() {
        return ++latestID;
    }

    public File getTableFile() {
        return tableFile;
    }

    // can this be combined with below saveToFile?
    public boolean loadTableData() {
        FileHandler fileHandler = new FileHandler();
        if (!fileHandler.readFile(tableFile, this)) {
            return false;
        }
        setID();
        return true;
    }


    public boolean saveToFile(File file) {
        FileHandler fileHandler = new FileHandler();
        return fileHandler.writeTableToFile(file, this);
    }


//    // column name should be case-insensitive when queried, but saved with case
//
//    public File insertRecord(File file) {
//        // same at INSERT (adds new row)
//
//        // need to auto-gen ID for e ach row
//
//        return file;
//    }
//
//    public File updateRecord(File file) {
//        // same at UPDATE (updates existing data)
//
//        return file;
//    }
//
//    public File alterTableStructure(File file) {
//        // same at ALTER
//
//        return file
//;
//    }
//
//    public File deleteRecord(File file) {
//        // same at DELETE
//
//        return file;
//    }


}







