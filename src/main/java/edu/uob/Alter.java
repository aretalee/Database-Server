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
            addColumnHeader(chosenTable, chosenHeader);
        }

        if(valueType.equalsIgnoreCase("drop")) {
            removeColumnHeader(chosenTable, chosenHeader);
        }

        chosenTable.saveToFile(chosenTable.getTableFile());

    }

    public void addColumnHeader(Table table, String header) {
        table.accessColumnHeaders().add(header);

        for(List<String> row : table.accessTable()) {
            row.add(null);
            // adding null parameter to each row for easy editing later
        }
    }

    public void removeColumnHeader(Table table, String header) {
        int chosenIndex = ColumnIndexFinder.findColumnIndex(table, header);
        table.accessColumnHeaders().remove(chosenIndex);

        if(chosenIndex != -1 && !header.equalsIgnoreCase("id")) { // should change checking ID into error handling?
            for(List<String> row : table.accessTable()) {
                row.remove(chosenIndex);
            }
        }
    }

}
