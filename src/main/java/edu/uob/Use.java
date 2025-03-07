package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Use {

    public boolean switchDatabases(String databasesPath, String databaseName, DBServer server) {

        File databases = new File(databasesPath);
        File[] allDatabases = databases.listFiles();
        File requestedDatabase = null;

        if(allDatabases == null || allDatabases.length == 0) {
            server.setErrorLine("No databases found.");
            return false;
        }

        for (File file : allDatabases) {
            if (file != null && file.getName().equals(databaseName)) {
                requestedDatabase = file;
            }
        }

        if (requestedDatabase == null) {
            server.setErrorLine("Requested database does not exist.");
            return false;
        }

        Database newDatabase = doesDatabaseExist(databaseName, server);

        if (newDatabase == null) {
            newDatabase = new Database(requestedDatabase);
            server.addDatabase(newDatabase);
        }

        server.setCalledUseCommand(true);
        return true;
        // should Database instance be created here or in CREATE

    }

    public Database doesDatabaseExist(String databaseName, DBServer server) {

        List<Database> databases = server.getAllDatabases();
        for (Database database : databases) {
            if(database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        return null;
    }


}
