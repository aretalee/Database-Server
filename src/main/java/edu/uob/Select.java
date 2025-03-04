package edu.uob;

import java.io.*;
import java.util.List;

public class Select {

    // must call WHERE.java method before this to unsure conditions have already been checked

    public void selectRecords(Table chosenTable, List<String> chosenHeaders) throws IOException {

        if(chosenHeaders.get(0).equals("*")) {
            // print all rows
            // assume table has already been filtered

            // probably need to be returned by handleCommand

        } else {
            for (String header : chosenHeaders) {
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
        chosenTable.saveToFile(chosenTable.getTableFile());

    }


}
