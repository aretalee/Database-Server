package edu.uob;

import java.io.File;
import java.util.List;

public class Use {

    public boolean switchDatabases(String databasesPath, String databaseName, QueryHandler queryHandler) {

        File databases = new File(databasesPath);
        File[] allDatabases = databases.listFiles();
        File requestedDatabase;

        if(allDatabases == null || allDatabases.length == 0) {
            queryHandler.setErrorLine("No databases found.");
            return false;
        }
        requestedDatabase = doesDatabaseFileExist(allDatabases, databaseName);
        if (requestedDatabase == null) {
            queryHandler.setErrorLine("Requested database does not exist.");
            return false;
        }
        Database newDatabase = doesDatabaseObjectExist(databaseName, queryHandler);
        if (newDatabase == null) {
            newDatabase = new Database(requestedDatabase);
            queryHandler.addDatabase(newDatabase);
        }
        queryHandler.setCalledUseCommand(true);
        return true;
    }

    public File doesDatabaseFileExist(File[] allDatabases, String databaseName) {
        File foundDatabase = null;

        for (File file : allDatabases) {
            if (file != null && file.getName().equals(databaseName)) {
                foundDatabase = file;
            }
        }
        return foundDatabase;
    }

    public Database doesDatabaseObjectExist(String databaseName, QueryHandler queryHandler) {
        List<Database> databases = queryHandler.getAllDatabases();
        for (Database database : databases) {
            if(database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        return null;
    }
}
