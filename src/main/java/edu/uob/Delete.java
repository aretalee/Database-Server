package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Delete {

    public boolean deleteRecord(Table chosenTable, List<String> conditionList, QueryHandler queryHandler) {

        if (chosenTable == null) {
            queryHandler.setErrorLine("Requested table does not exist.");
            return false;
        }
        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToDelete = conditionHandler.filterTable(chosenTable, conditionList, queryHandler);

        if (!findAndDelete(chosenTable, rowsToDelete, queryHandler)) { return false; }

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            queryHandler.setErrorLine("Could not delete record, please try again.");
            return false;
        }
        return true;
    }

    public boolean findAndDelete(Table chosenTable, List<Integer> rowsToDelete, QueryHandler queryHandler) {
        if (!rowsToDelete.isEmpty()) {
            List<List<String>> rowObjects = new ArrayList<List<String>>();
            if (!getRowObjects(chosenTable, rowObjects, rowsToDelete, queryHandler)) {
                return false;
            }
            removeActualRows(chosenTable, rowObjects);
        }
        return true;
    }

    public boolean getRowObjects(Table table, List<List<String>> rowObjects, List<Integer> rows, QueryHandler handler) {
        for (Integer index : rows) {
            if (index == -1) {
                handler.setErrorLine("Requested column(s) in condition does not exist.");
                return false;
            }
            List<String> row = table.getTableFromList(index);
            rowObjects.add(row);
        }
        return true;
    }

    public void removeActualRows(Table chosenTable, List<List<String>> rowObjects) {
        for (List<String> row : rowObjects) {
            chosenTable.removeFromTableList(row);
        }
    }

}
