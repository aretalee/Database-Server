package edu.uob;

import java.io.File;
import java.util.List;

public class Use {

    public boolean switchDatabases(String databasesPath, String databaseName, QueryHandler handler) {

        File databases = new File(databasesPath);
        File[] allDatabases = databases.listFiles();
        File requestedDatabase;

        if(allDatabases == null || allDatabases.length == 0) {
            handler.setErrorLine("No databases found.");
            return false;
        }
        requestedDatabase = doesDatabaseFileExist(allDatabases, databaseName);
        if (requestedDatabase == null) {
            handler.setErrorLine("Requested database does not exist.");
            return false;
        }
        Database newDatabase = doesDatabaseObjectExist(databaseName, handler);
        if (newDatabase == null) {
            newDatabase = new Database(requestedDatabase);
            handler.addDatabase(newDatabase);
        }
        handler.setCalledUseCommand(true);
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

    public Database doesDatabaseObjectExist(String databaseName, QueryHandler handler) {
        List<Database> databases = handler.getAllDatabases();
        for (Database database : databases) {
            if(database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        return null;
    }
}
