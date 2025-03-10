package edu.uob;

import java.util.List;

public class ColumnIndexFinder {

    public static int findColumnIndex(Table chosenTable, String chosenHeader) {
        int chosenIndex = -1;

        List<String> headerList = chosenTable.accessColumnHeaders();
        for(String header : headerList) {
            if((header).equalsIgnoreCase(chosenHeader)) {
                chosenIndex = headerList.indexOf(header);
            }
        }
        return chosenIndex;
    }

}
