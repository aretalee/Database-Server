package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;import java.util.ArrayList;
import java.util.List;

public class Database {

    private List<Table> thisDatabase;
    private File databaseFile;

    public Database(File newDatabaseFile) {
        // determine how to create instance of this class

        thisDatabase = new ArrayList<Table>();
        databaseFile = newDatabaseFile;
    }

    public List<Table> accessDatabase() {
        return thisDatabase;
    }

//    // see if I can extrapolate this later (along with createDatabase)
//    public File createTable(String fileName) throws IOException {
//        // same as CREATE
//
//        // include directory path to parent folder (databases)
//
//        // name must be case-insensitive
//
//        // create in filesystem + make new instance of class
//
//        return this.databaseFile;
//    }

    // is this needed?
    public void saveToFile(File file) {

    }

//    //might need helper methods in
//    public File selectFromTable(String fileName) throws IOException {
//        // same as SELECT
//
//        return this.databaseFile;
//    }
//
//    // should JOIN be in here?
//    // since need to do it with two tables
//    public File joinTables(String fileName1, String fileName2) throws IOException {
//        // same as JOIN
//
//        return this.databaseFile;
//    }


}
