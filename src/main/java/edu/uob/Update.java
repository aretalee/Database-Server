package edu.uob;

import java.util.List;

public class Update {

    public boolean updateTable(Table chosenTable, List<String> nameValueList, List<String> conditionList, QueryHandler queryHandler) {

        if (chosenTable == null) {
            queryHandler.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditionList, queryHandler);

        if (!checkValueListHeaders(chosenTable, nameValueList, queryHandler)
                || !editValues(chosenTable, nameValueList, rowsToUpdate, queryHandler)) {
            return false;
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            queryHandler.setErrorLine("Could not update table, please try again.");
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

    public boolean editValues(Table table, List<String> nameValueList, List<Integer> rowsToUpdate, QueryHandler queryHandler) {
        int attributeIndex = 0;
        int valueIndex = 1;

        if (!rowsToUpdate.isEmpty()) {
            for (Integer index : rowsToUpdate) {
                while (attributeIndex < nameValueList.size()) {
                    if (index == -1) {
                        queryHandler.setErrorLine("Requested column(s) in condition does not exist.");
                        return false;
                    }
                    String headerName = nameValueList.get(attributeIndex);
                    int headerIndex = table.getHeaderIndex(headerName);
                    table.updateRowValue(index, headerIndex, nameValueList.get(valueIndex));
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
