package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Select {

    public void selectRecords(DBServer server, Table chosenTable, List<String> chosenHeaders, List<List<String>> conditionList) {

        if (chosenHeaders.get(0).equals("*")) {
            // print all rows
            // assume table has already been filtered

            List<String> allRows = formatOutputTable(chosenTable.accessTable(), chosenTable.accessColumnHeaders());
            server.setTableForPrinting(allRows);
            server.setPrintBoolean(true);

        } else {
            List<Integer> headerIndexes = new ArrayList<Integer>();
            List<List<String>> toBePrinted = new ArrayList<List<String>>();

            for (String header : chosenHeaders) {
                headerIndexes.add(ColumnIndexFinder.findColumnIndex(chosenTable, header));
            }

            for (List<String> row : chosenTable.accessTable()) {
                // assume table has already been filtered
                List<String> rowValues = new ArrayList<String>();

                for (int currentIndex : headerIndexes) {
                    if (currentIndex != -1) {
                        rowValues.add(row.get(currentIndex));
                    }
                }
                toBePrinted.add(rowValues);
            }
            List<String> selectedRows = formatOutputTable(toBePrinted, chosenHeaders);
            server.setTableForPrinting(selectedRows);
            server.setPrintBoolean(true);
        }
        // save back to filesystem
//        chosenTable.saveToFile(chosenTable.getTableFile());
    }


    public List<String> formatOutputTable(List<List<String>> tableList, List<String> headers) {

        List<String> chosenRows = new ArrayList<String>();

        chosenRows.add("\n" + String.join("\t", headers) + "\n");

        for (List<String> row : tableList) {
            chosenRows.add(String.join("\t", row) + "\n");
        }
        return chosenRows;
    }


}
