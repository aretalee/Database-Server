package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Alter {

    public void alterTable(String valueType, Table chosenTable, String chosenHeader) throws IOException {

        // best to separate out into smaller functions

        if(valueType.equals("ADD")) {

            chosenTable.accessColumnHeaders().add(chosenHeader);

            for(List<String> row : chosenTable.accessTable()) {
                row.add(null);
                // adding null parameter to each row for easy editing later
            }

        }

        if(valueType.equals("DROP")) {
            int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, chosenHeader);
            chosenTable.accessColumnHeaders().remove(chosenIndex);

            if(chosenIndex != -1) {
                for(List<String> row : chosenTable.accessTable()) {
                    row.remove(chosenIndex);
                }
            }

        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }

}
