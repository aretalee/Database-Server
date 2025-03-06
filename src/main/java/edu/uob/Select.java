package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Select {

    private boolean printAllRows = false;

    public void selectRecords(DBServer server, Table chosenTable, List<String> chosenHeaders, List<List<String>> conditionList) {

        ConditionHandler conditionHandler = new ConditionHandler();
        List<Integer> rowsToSelect = conditionHandler.filterTable(chosenTable, conditionList);

        if (conditionHandler.isConditionListEmpty(conditionList)) {
            printAllRows = true;
        }

        if (chosenHeaders.get(0).equals("*")) {
            List<String> allColumns = formatOutputTable(chosenTable.accessTable(), chosenTable.accessColumnHeaders(), rowsToSelect);
            server.setTableForPrinting(allColumns);
            server.setPrintBoolean(true);

        } else {
            List<Integer> headerIndexes = new ArrayList<Integer>();
            List<List<String>> toBePrinted = new ArrayList<List<String>>();

            for (String header : chosenHeaders) {
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
    }

    public List<String> formatOutputTable(List<List<String>> tableList, List<String> headers, List<Integer> chosenRows) {

        List<String> formattedRows = new ArrayList<String>();
        formattedRows.add("\n" + String.join("\t", headers) + "\n");

        int rowIndex = 0;

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
