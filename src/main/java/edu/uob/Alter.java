package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Alter {

    public void alterTable(Table chosenTable, String valueType, String chosenHeader) throws IOException {

        // best to separate out into smaller functions

        if(valueType.equalsIgnoreCase("add")) {

            chosenTable.accessColumnHeaders().add(chosenHeader);

            for(List<String> row : chosenTable.accessTable()) {
                row.add(null);
                // adding null parameter to each row for easy editing later
            }

        }

        if(valueType.equalsIgnoreCase("drop")) {
            int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, chosenHeader);
            chosenTable.accessColumnHeaders().remove(chosenIndex);

            if(chosenIndex != -1 && !chosenHeader.equalsIgnoreCase("id")) { // should change checking ID into error handling?
                for(List<String> row : chosenTable.accessTable()) {
                    row.remove(chosenIndex);
                }
            }

        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }

}
