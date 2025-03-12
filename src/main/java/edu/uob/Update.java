package edu.uob;

import java.util.List;

public class Update {

    public boolean updateTable(Table chosenTable, List<String> nValueList, List<String> conditions, QueryHandler handler) {

        if (chosenTable == null) {
            handler.setErrorLine("Requested table does not exist.");
            return false;
        }
        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditions, handler);

        if (!rowsToUpdate.isEmpty()) {
            if (!checkValueListHeaders(chosenTable, nValueList, handler)
                    || !editValues(chosenTable, nValueList, rowsToUpdate, handler)) {
                return false;
            }
        }
        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            handler.setErrorLine("Could not update table, please try again.");
            return false;
        }
        return true;
    }

    public boolean checkValueListHeaders(Table table, List<String> nameValueList, QueryHandler queryHandler) {
        int attributeIndex = 0;
        String headerName = nameValueList.get(attributeIndex).toLowerCase();

        while (attributeIndex < nameValueList.size()) {
            if (headerName.equalsIgnoreCase("id")) {
                queryHandler.setErrorLine("Cannot update id column.");
                return false;
            } else if (!table.hasRequestedHeader(headerName)) {
                queryHandler.setErrorLine("Requested column(s) in SET does not exist.");
                return false;
            }
            attributeIndex += 2;
        }
        return true;
    }

    public boolean editValues(Table table, List<String> nValueList, List<Integer> rowsToUpdate, QueryHandler handler) {
        for (Integer index : rowsToUpdate) {
            int attributeIndex = 0;
            int valueIndex = 1;

            while (attributeIndex < nValueList.size()) {
                if (index == -1) {
                    handler.setErrorLine("Requested column(s) in condition does not exist.");
                    return false;
                }
                String headerName = nValueList.get(attributeIndex);
                table.updateRowValue(index, table.getHeaderIndex(headerName), nValueList.get(valueIndex));
                attributeIndex += 2;
                valueIndex += 2;
            }
        }
        return true;
    }

}
