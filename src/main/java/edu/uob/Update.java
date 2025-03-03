package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Update {

    // must call Comparer.java method before this to unsure conditions have already been checked

    public void updateTable(Table chosenTable, String chosenHeader, String value) throws IOException {

        int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, chosenHeader);

        if(chosenIndex != -1) { // remove magic number later
            for(List<String> row : chosenTable.accessTable()) {
                // assume table has already been filtered
                row.set(chosenIndex, value);
            }
        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }


}
