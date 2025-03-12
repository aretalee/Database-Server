package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Select {

    private boolean printAllRows = false;

    public boolean selectRecords(Table table, List<String> headers, List<String> conditions, QueryHandler handler) {
        if (table == null) {
            handler.setErrorLine("Requested table does not exist.");
            return false;
        }
        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToSelect = conditionHandler.filterTable(table, conditions, handler);

        if (conditions.isEmpty()) { printAllRows = true; }
        else if (!rowsToSelect.isEmpty() && rowsToSelect.get(0) == -1) { return false; }

        if (headers.isEmpty()) {
            handler.setErrorLine("Missing header from query.");
            return false;
        } else if (headers.get(0).equals("*")) {
            List<String> allColumns = formatOutputTable(table.accessTable(),
                    table.accessColumnHeaders(), rowsToSelect);
            handler.setTableForPrinting(allColumns);
            handler.setPrintBoolean(true);
        } else {
            List<Integer> headerIndexes = new ArrayList<Integer>();
            List<List<String>> toBePrinted = new ArrayList<List<String>>();

            for (String header : headers) {
                if (!table.hasRequestedHeader(header)) {
                    handler.setErrorLine("Requested column does not exist.");
                    return false;
                }
                headerIndexes.add(handler.findColumnIndex(table, header));
            }
            for (List<String> row : table.accessTable()) {
                List<String> rowValues = new ArrayList<String>();

                for (int currentIndex : headerIndexes) {
                    if (currentIndex != -1) {
                        rowValues.add(row.get(currentIndex));
                    }
                }
                toBePrinted.add(rowValues);
            }
            List<String> selectedColumns = formatOutputTable(toBePrinted, headers, rowsToSelect);
            handler.setTableForPrinting(selectedColumns);
            handler.setPrintBoolean(true);
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
