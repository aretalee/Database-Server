package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Use {

    public Database switchDatabases(String databasesPath, String databaseName, DBServer server) throws IOException {

        File databases = new File(databasesPath);
        File[] allDatabases = databases.listFiles();
        File requestedDatabase = null;

        if(allDatabases == null) {
            throw new NullPointerException("allDatabases");
        } else if(allDatabases.length == 0) {
            throw new IOException("databases is empty");
        }

        for (File file : allDatabases) {
            if (file.getName().equals(databaseName)) {
                requestedDatabase = file;
            }
        }


        Database newDatabase = doesDatabaseExist(databaseName, server);

        if (newDatabase == null) {
            newDatabase = new Database(requestedDatabase);
            server.addDatabase(newDatabase);
        }

        // need to handle cases where object already exists

        // check if database exists by checking against list in Server class

        // if it doesn't exist, make new database and return

        return newDatabase;
        // should Database instance be created here or in CREATE
        // can then be saved as currentDatabase in class that called this

    }

    public Database doesDatabaseExist(String databaseName, DBServer server) throws IOException {

        List<Database> databases = server.getAllDatabases();
        for (Database database : databases) {
            if(database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        return null;
    }


}
