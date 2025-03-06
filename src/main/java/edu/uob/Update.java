package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Update {

    public void updateTable(Table chosenTable, List<String> nameValueList, List<List<String>> conditionList) throws IOException {

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditionList);
        List<List<String>> tableList = chosenTable.accessTable();

        int attributeIndex = 0;
        int valueIndex = 1;

        // maybe can try to simplify this
        if (!rowsToUpdate.isEmpty()) {
            for (Integer index : rowsToUpdate) {
                while (attributeIndex < nameValueList.size()) {
                    String headerName = nameValueList.get(attributeIndex);
                    int headerIndex = chosenTable.accessColumnHeaders().indexOf(headerName);
                    tableList.get(index).set(headerIndex, nameValueList.get(valueIndex));
                    attributeIndex += 2;
                    valueIndex += 2;
                }
                attributeIndex = 0;
                valueIndex = 1;
            }

        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }


}
