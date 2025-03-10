package edu.uob;

import java.io.*;

public class Database {

    private File databaseFile;
    private String databaseName;

    public Database(File newDatabaseFile) {
        databaseFile = newDatabaseFile;
        databaseName = databaseFile.getName();
    }

    public String getDatabaseName() {
        return databaseName;
    }

}
