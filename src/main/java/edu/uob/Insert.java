package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Insert {

    public void insertIntoTable(Table chosenTable, List<String> valueParameters) throws IOException {

//        List<List<String>> tableList;
//        tableList = chosenTable.accessTable();
//        tableList.add(valueParameters);
        int rowID = chosenTable.getCurrentID();
        // ensures 1st item in each row is always ID
        valueParameters.add(0, String.valueOf(rowID));
        chosenTable.accessTable().add(valueParameters);

        chosenTable.saveToFile(chosenTable.getTableFile());

        // save back to filesystem

    }

}
