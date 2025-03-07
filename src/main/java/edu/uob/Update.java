package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Update {

    public boolean updateTable(Table chosenTable, List<String> nameValueList, List<List<String>> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditionList);
        List<List<String>> tableList = chosenTable.accessTable();

        if (!editValues(chosenTable, tableList, nameValueList, rowsToUpdate, server)) {
            return false;
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile(), server)) {
            server.setErrorLine("Could not update table, please try again.");
            return false;
        }
        return true;
    }

    public boolean editValues(Table table, List<List<String>> tableList, List<String> nameValueList, List<Integer> rowsToUpdate, DBServer server) {
        int attributeIndex = 0;
        int valueIndex = 1;

        if (!rowsToUpdate.isEmpty()) {
            for (Integer index : rowsToUpdate) {
                while (attributeIndex < nameValueList.size()) {
                    String headerName = nameValueList.get(attributeIndex);

                    if (headerName.equalsIgnoreCase("id")) {
                        server.setErrorLine("Cannot update id column.");
                        return false;
                    } else if (!table.accessColumnHeaders().contains(headerName)) {
                        server.setErrorLine("Requested column does not exist.");
                        return false;
                    }

                    int headerIndex = table.accessColumnHeaders().indexOf(headerName);
                    tableList.get(index).set(headerIndex, nameValueList.get(valueIndex));
                    attributeIndex += 2;
                    valueIndex += 2;
                }
                attributeIndex = 0;
                valueIndex = 1;
            }
        }
        return true;
    }
}
