package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Delete {
    // must call Comparer.java method before this to unsure conditions have already been checked

    public void deleteRecord(Table chosenTable, List<List<String>> conditionList) throws IOException {

        int currentIndex = 0;
        for(List<String> row : chosenTable.accessTable()) {
                // assume table has already been filtered
            row.remove(currentIndex);
            currentIndex++;
        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }

}
