package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Delete {

    public boolean deleteRecord(Table chosenTable, List<String> conditionList, QueryHandler handler) {

        if (chosenTable == null) {
            handler.setErrorLine("Requested table does not exist.");
            return false;
        }
        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToDelete = conditionHandler.filterTable(chosenTable, conditionList, handler);

        if (!findAndDelete(chosenTable, rowsToDelete, handler)) { return false; }

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            handler.setErrorLine("Could not delete record, please try again.");
            return false;
        }
        return true;
    }

    public boolean findAndDelete(Table chosenTable, List<Integer> rowsToDelete, QueryHandler handler) {
        if (!rowsToDelete.isEmpty()) {
            List<List<String>> rowObjects = new ArrayList<List<String>>();
            if (!getRowObjects(chosenTable, rowObjects, rowsToDelete, handler)) {
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
