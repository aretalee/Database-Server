package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Delete {

    public boolean deleteRecord(Table chosenTable, List<List<String>> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToDelete = conditionHandler.filterTable(chosenTable, conditionList);
        List<List<String>> tableList = chosenTable.accessTable();

        if (!rowsToDelete.isEmpty()) {
            for (Integer index : rowsToDelete) {
                List<String> row = tableList.get(index);
                tableList.remove(row);
            }
        }

        if (chosenTable.saveToFile(chosenTable.getTableFile(), server)) {
            server.setErrorLine("Could not delete record, please try again.");
            return false;
        }
        return true;
    }

}
