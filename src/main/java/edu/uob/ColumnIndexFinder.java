package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
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
