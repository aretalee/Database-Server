package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Select {

    private boolean printAllRows = false;

    public boolean selectRecords(Table chosenTable, List<String> chosenHeaders, List<String> conditionList, QueryHandler queryHandler) {

        if (chosenTable == null) {
            queryHandler.setErrorLine("Requested table does not exist.");
            return false;
        }

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToSelect = conditionHandler.filterTable(chosenTable, conditionList, queryHandler);

        if (conditionList.isEmpty()) {
            printAllRows = true;
        } else if (!rowsToSelect.isEmpty() && rowsToSelect.get(0) == -1) { return false; }

        if (chosenHeaders.get(0).equals("*")) {
            List<String> allColumns = formatOutputTable(chosenTable.accessTable(),
                    chosenTable.accessColumnHeaders(), rowsToSelect);
            queryHandler.setTableForPrinting(allColumns);
            queryHandler.setPrintBoolean(true);

        } else {
            List<Integer> headerIndexes = new ArrayList<Integer>();
            List<List<String>> toBePrinted = new ArrayList<List<String>>();

            for (String header : chosenHeaders) {
                if (!chosenTable.hasRequestedHeader(header)) {
                    queryHandler.setErrorLine("Requested column does not exist.");
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
            queryHandler.setTableForPrinting(selectedColumns);
            queryHandler.setPrintBoolean(true);
        }
        return true;
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
