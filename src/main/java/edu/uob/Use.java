package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Use {

    public static void main(String args[]) throws IOException {
        String folderPath = Paths.get("databases").toAbsolutePath().toString();

        File currentDB = new File(folderPath + File.separator + "datatwo");
        System.out.println(currentDB.getName());

        Use use = new Use();
        Database switchedDB = use.switchDatabases(folderPath, "dataone");
        System.out.println(switchedDB.getDatabaseName());

        switchedDB = use.switchDatabases(folderPath, "datatwo");
        System.out.println(switchedDB.getDatabaseName());

    }

    public Database switchDatabases(String databasesPath, String databaseName) throws IOException {
        // same as USE

        // need to check if this works

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

        return new Database(requestedDatabase);
        // should Database instance be created here or in CREATE
        // can then be saved as currentDatabase in class that called this
    }
}
