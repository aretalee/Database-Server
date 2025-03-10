package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Delete {

    public boolean deleteRecord(Table chosenTable, List<String> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToDelete = conditionHandler.filterTable(chosenTable, conditionList, server);

        List<List<String>> rowObjects = new ArrayList<List<String>>();
        if (!getRowObjects(chosenTable, rowObjects, rowsToDelete, server)) {
            return false;
        }

        for (List<String> row : rowObjects) {
            chosenTable.removeFromTableList(row);
            // does there need to be error handling here?
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            server.setErrorLine("Could not delete record, please try again.");
            return false;
        }
        return true;
    }

    public boolean getRowObjects(Table table, List<List<String>> rowObjects, List<Integer> rowsToDelete, DBServer server) {
        if (!rowsToDelete.isEmpty()) {
            for (Integer index : rowsToDelete) {
                if (index == -1) {
                    server.setErrorLine("Requested column(s) in condition does not exist.");
                    return false;
                }
                List<String> row = table.getTableFromList(index);
                rowObjects.add(row);
            }
        }
        return true;
    }



}
