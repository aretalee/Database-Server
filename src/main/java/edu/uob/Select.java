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

        List<String> columnsToPrint;
        if (headers.isEmpty()) {
            handler.setErrorLine("Missing header from query.");
            return false;
        } else if (headers.get(0).equals("*")) {
            columnsToPrint = formatOutputTable(table.accessTable(), table.accessColumnHeaders(), rowsToSelect);
        } else {
            List<List<String>> toBePrinted = new ArrayList<List<String>>();
            if (!chooseRows(table, toBePrinted, headers, handler)) {
                return false;
            }
            columnsToPrint = formatOutputTable(toBePrinted, headers, rowsToSelect);
        }
        handler.setTableForPrinting(columnsToPrint);
        handler.setPrintBoolean(true);
        return true;
    }

    public boolean chooseRows(Table table, List<List<String>> toBePrinted, List<String> headers, QueryHandler handler) {
        List<Integer> headerIndexes = new ArrayList<Integer>();

        if (!findHeaderIndexes(table, headerIndexes, headers, handler)) { return false;}
        getRowValues(table, headerIndexes, toBePrinted);
        return true;
    }

    public boolean findHeaderIndexes(Table table, List<Integer> indexes, List<String> headers, QueryHandler handler) {
        for (String header : headers) {
            if (!table.hasRequestedHeader(header)) {
                handler.setErrorLine("Requested column does not exist.");
                return false;
            }
            indexes.add(handler.findColumnIndex(table, header));
        }
        return true;
    }

    public void getRowValues(Table table, List<Integer> headerIndexes, List<List<String>> toBePrinted) {
        for (List<String> row : table.accessTable()) {
            List<String> rowValues = new ArrayList<String>();
            for (int currentIndex : headerIndexes) {
                addToValueList(row, currentIndex, rowValues);
            }
            toBePrinted.add(rowValues);
        }
    }

    public void addToValueList(List<String> row, int currentIndex, List<String> rowValues) {
        if (currentIndex != -1) {
            rowValues.add(row.get(currentIndex));
        }
    }

    public List<String> formatOutputTable(List<List<String>> tableList, List<String> headers, List<Integer> chosenRows) {
        List<String> formattedRows = new ArrayList<String>();
        formattedRows.add("\n" + String.join("\t", headers) + "\n");

        if (printAllRows) {
            addAllRows(formattedRows, tableList);
            printAllRows = false;
        } else if (chosenRows.isEmpty()) { return null; }
        else { addSelectedRows(formattedRows, tableList, chosenRows); }

        return formattedRows;
    }

    public void addAllRows(List<String> formattedRows, List<List<String>> tableList) {
        for (List<String> row : tableList) {
            formattedRows.add(String.join("\t", row) + "\n");
        }
    }

    public void addSelectedRows(List<String> formattedRows, List<List<String>> tableList, List<Integer> chosenRows) {
        for (Integer index : chosenRows) {
            formattedRows.add(String.join("\t", tableList.get(index)) + "\n");
        }
    }

}
