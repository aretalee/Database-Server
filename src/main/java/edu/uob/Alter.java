package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Alter {

    public boolean alterTable(Table chosenTable, String valueType, String chosenHeader, DBServer server) {

        // best to separate out into smaller functions
        if(valueType.equalsIgnoreCase("add")) {
            if (!addColumnHeader(chosenTable, chosenHeader, server)) {
                return false;
            }
        } else if(valueType.equalsIgnoreCase("drop")) {
            if (!removeColumnHeader(chosenTable, chosenHeader, server)) {
                return false;
            }
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile(), server)) {
            server.setErrorLine("Could not alter table, please try again.");
            return false;
        }

        return true;
    }

    public boolean addColumnHeader(Table table, String header, DBServer server) {

        if (table == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        } else if (table.hasRequestedHeader(header)) {
            server.setErrorLine("Column already exists.");
            return false;
        }

        table.addToColumnHeaders(header);
        table.addNullToRows();
        return true;
    }

    public boolean removeColumnHeader(Table table, String header, DBServer server) {
        int chosenIndex = ColumnIndexFinder.findColumnIndex(table, header);

        if (chosenIndex == -1) {
            server.setErrorLine("Column does not exist.");
            return false;
        } else if (header.equalsIgnoreCase("id")) {
            server.setErrorLine("Cannot remove id column from table.");
            return false;
        }

        table.removeFromColumnHeaders(chosenIndex);
        table.removeTableRow(chosenIndex);
        return true;
    }
}
