package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Select {

    private boolean printAllRows = false;

    public boolean selectRecords(Table chosenTable, List<String> chosenHeaders, List<List<String>> conditionList, DBServer server) {

        if (chosenTable == null) {
            server.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandlerNewVersion conditionHandler = new ConditionHandlerNewVersion();
        List<Integer> rowsToSelect = conditionHandler.filterTable(chosenTable, conditionList, server);

        if (conditionHandler.isConditionListEmpty(conditionList)) {
            printAllRows = true;
        } else if (nonExistentColumn(rowsToSelect, server)) { return false; }

        if (chosenHeaders.get(0).equals("*")) {
            List<String> allColumns = formatOutputTable(chosenTable.accessTable(),
                    chosenTable.accessColumnHeaders(), rowsToSelect);
            server.setTableForPrinting(allColumns);
            server.setPrintBoolean(true);

        } else {
            List<Integer> headerIndexes = new ArrayList<Integer>();
            List<List<String>> toBePrinted = new ArrayList<List<String>>();

            for (String header : chosenHeaders) {
                if (!chosenTable.accessColumnHeaders().contains(header)) {
                    server.setErrorLine("Requested column does not exist.");
                    return false;
                }
                headerIndexes.add(ColumnIndexFinder.findColumnIndex(chosenTable, header));
            }

            for (List<String> row : chosenTable.accessTable()) {
                List<String> rowValues = new ArrayList<String>();

                for (int currentIndex : headerIndexes) {
                    if (currentIndex != -1) {
                        rowValues.add(row.get(currentIndex));
                    }
                }
                toBePrinted.add(rowValues);
            }
            List<String> selectedColumns = formatOutputTable(toBePrinted, chosenHeaders, rowsToSelect);
            server.setTableForPrinting(selectedColumns);
            server.setPrintBoolean(true);
        }
        return true;
    }

    public boolean nonExistentColumn(List<Integer> chosenRows, DBServer server) {
        for (Integer index : chosenRows) {
            if (index == -1) {
                server.setErrorLine("Requested column(s) in condition does not exist.");
                return true;
            }
        }
        return false;
    }

    public List<String> formatOutputTable(List<List<String>> tableList, List<String> headers, List<Integer> chosenRows) {

        List<String> formattedRows = new ArrayList<String>();
        formattedRows.add("\n" + String.join("\t", headers) + "\n");

        if (printAllRows) {
            for (List<String> row : tableList) {
                formattedRows.add(String.join("\t", row) + "\n");
            }
            printAllRows = false;
        } else if (chosenRows.isEmpty()) {
            return null;
        } else {
            for (Integer index : chosenRows) {
                formattedRows.add(String.join("\t", tableList.get(index)) + "\n");
            }
        }
        return formattedRows;
    }


}
