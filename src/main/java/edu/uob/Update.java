package edu.uob;

import java.util.List;

public class Update {

    public boolean updateTable(Table chosenTable, List<String> nameValueList, List<List<String>> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToUpdate = conditionHandler.filterTable(chosenTable, conditionList, server);

        if (!editValues(chosenTable, nameValueList, rowsToUpdate, server)) {
            return false;
        }

        if (!chosenTable.saveToFile(chosenTable.getTableFile(), server)) {
            server.setErrorLine("Could not update table, please try again.");
            return false;
        }
        return true;
    }

    public boolean editValues(Table table, List<String> nameValueList, List<Integer> rowsToUpdate, DBServer server) {
        int attributeIndex = 0;
        int valueIndex = 1;

        if (!rowsToUpdate.isEmpty()) {
            for (Integer index : rowsToUpdate) {
                while (attributeIndex < nameValueList.size()) {
                    String headerName = nameValueList.get(attributeIndex);

                    if (headerName.equalsIgnoreCase("id")) {
                        server.setErrorLine("Cannot update id column.");
                        return false;
                    } else if (!table.hasRequestedHeader(headerName)) {
                        server.setErrorLine("Requested column(s) in SET does not exist.");
                        return false;
                    } else if (index == -1) {
                        server.setErrorLine("Requested column(s) in condition does not exist.");
                        return false;
                    }

                    int headerIndex = table.getHeaderIndex(headerName);
//                    tableList.get(index).set(headerIndex, nameValueList.get(valueIndex));
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
