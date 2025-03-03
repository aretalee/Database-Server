package edu.uob;

import java.io.File;
import java.io.IOException;

public class Use {

    public File switchDatabases(File databases, String databaseName) throws IOException {
        // same as USE

        // need to check if this works

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

        return requestedDatabase;
        // can then be saved as currentDatabase in class that called this
    }
}
