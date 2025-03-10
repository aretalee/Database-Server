package edu.uob;

import java.util.List;

public class Update {

    public boolean updateTable(Table chosenTable, List<String> nameValueList, List<String> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditionList, server);

        if (!checkValueListHeaders(chosenTable, nameValueList, server)
                || !editValues(chosenTable, nameValueList, rowsToUpdate, server)) {
            return false;
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile())) {
            server.setErrorLine("Could not update table, please try again.");
            return false;
        }
        return true;
    }

    public boolean checkValueListHeaders(Table table, List<String> nameValueList, DBServer server) {
        int attributeIndex = 0;
        String headerName = nameValueList.get(attributeIndex).toLowerCase();

        while (attributeIndex < nameValueList.size()) {
            if (headerName.equalsIgnoreCase("id")) {
                server.setErrorLine("Cannot update id column.");
                return false;
            } else if (!table.hasRequestedHeader(headerName)) {
                server.setErrorLine("Requested column(s) in SET does not exist.");
                return false;
            }
            attributeIndex += 2;
        }
        return true;
    }

    public boolean editValues(Table table, List<String> nameValueList, List<Integer> rowsToUpdate, DBServer server) {
        int attributeIndex = 0;
        int valueIndex = 1;

        if (!rowsToUpdate.isEmpty()) {
            for (Integer index : rowsToUpdate) {
                while (attributeIndex < nameValueList.size()) {
                    if (index == -1) {
                        server.setErrorLine("Requested column(s) in condition does not exist.");
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
