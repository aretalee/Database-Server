package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Update {

    // must call Comparer.java method before this to unsure conditions have already been checked

    public void updateTable(Table chosenTable, List<String> nameValueList) throws IOException {

        int attributeIndex = 0;
        int valueIndex = 1;

        while (attributeIndex < nameValueList.size()) {
            int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, nameValueList.get(attributeIndex));

            if(chosenIndex != -1) { // remove magic number later
                for(List<String> row : chosenTable.accessTable()) {
                    // assume table has already been filtered
                    row.set(chosenIndex, nameValueList.get(valueIndex));
                }
            }
            attributeIndex += 2;
            valueIndex += 2;
        }




        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }


}
