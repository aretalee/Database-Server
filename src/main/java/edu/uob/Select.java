package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Select {

    // must call WHERE.java method before this to unsure conditions have already been checked

    public void selectRecords(DBServer server, Table chosenTable, List<String> chosenHeaders, List<List<String>> conditionList) {

        if(chosenHeaders.get(0).equals("*")) {
            // print all rows
            // assume table has already been filtered

            List<String> allRows = formatOutputTable(chosenTable);
            server.setTableForPrinting(allRows);
            server.setPrintBoolean(true);

        } else {
            for (String header : chosenHeaders) {
//                System.out.println(header);
                int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, header);

                if(chosenIndex != -1) { // remove magic number later
                    for(List<String> row : chosenTable.accessTable()) {
                        // assume table has already been filtered

                        // print to terminal
                        // probably need to be returned by handleCommand

                    }
            }


            }
        }

        // save back to filesystem
//        chosenTable.saveToFile(chosenTable.getTableFile());

    }

    public List<String> formatOutputTable(Table table) {

        List<String> chosenRows = new ArrayList<String>();
        List<List<String>> tableList = table.accessTable();

        chosenRows.add("\n" + String.join("\t", table.accessColumnHeaders()) + "\n");

        for (List<String> row : tableList) {
            chosenRows.add(String.join("\t", row) + "\n");
        }
        return chosenRows;
    }


}
