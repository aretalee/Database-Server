package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Delete {

    public void deleteRecord(Table chosenTable, List<List<String>> conditionList) throws IOException {

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToDelete = conditionHandler.filterTable(chosenTable, conditionList);
        List<List<String>> tableList = chosenTable.accessTable();

        if (!rowsToDelete.isEmpty()) {
            for (Integer index : rowsToDelete) {
                List<String> row = tableList.get(index);
                tableList.remove(row);
            }
        }

        chosenTable.saveToFile(chosenTable.getTableFile());

    }

}
