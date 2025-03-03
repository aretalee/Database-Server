package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Table {

    // figure out List ArrayList thing
    private List<List<String>> tableList;
    private List<String> columnHeaders;
    private File tableFile;
    // File object or use file path?
    private static int ID = 0;

    // if creating new file: call CREATE, then make new Table + feed in File from CREATE method
    // if existing file: make new Table + feed in file (using new File(file path) ?), then call load data method
    public Table(File newTableFile) throws IOException {
        // determine how to create instance of this class

        // read in and parse data from table file

        tableList = new ArrayList<List<String>>();
        columnHeaders = new ArrayList<String>();
        columnHeaders.add("ID");

        tableFile = newTableFile;

    }

    public List<List<String>> accessTable() {
        return tableList;
    }

    public List<String> accessColumnHeaders() {
        return columnHeaders;
    }

    public int getCurrentID() {
        return ID++;
    }

    public File getTableFile() {
        return tableFile;
    }

    public void loadTableData() throws IOException {

        try {
            FileHandler.readFile(tableFile, this);
        } catch (IOException e) {
            throw new IOException("File could not be read: " + tableFile.getAbsolutePath());
        }
    }

    // method that save all changed records back to filesystem

    public void saveToFile(File file) throws IOException {

        try {
            FileHandler.writeTableToFile(file, this);
        } catch (IOException e) {
            throw new IOException("File could not be saved: " + file.getAbsolutePath());
        }
    }


//    // column name should be case-insensitive when queried, but saved with case
//
//    public File insertRecord(File file) {
//        // same at INSERT (adds new row)
//
//        // need to auto-gen ID for each row
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







