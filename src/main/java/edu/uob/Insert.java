package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Insert {


    public boolean insertIntoTable(DBServer server, Table chosenTable, List<String> valueParameters) throws IOException {

        if (chosenTable.accessColumnHeaders().size() == 1) {
            server.setErrorLine("Please insert at least one column header to the table using ALTER.");
            return false;
        }

        if (valueParameters.size() != chosenTable.accessColumnHeaders().size() - 1) {
            server.setErrorLine("Must insert " + (chosenTable.accessColumnHeaders().size() - 1)
                    + " values but you have inputted " + valueParameters.size() + " .");
            return false;
        }

        int rowID = chosenTable.getCurrentID();
        valueParameters.add(0, String.valueOf(rowID));
        chosenTable.accessTable().add(valueParameters);

        chosenTable.saveToFile(chosenTable.getTableFile());

        return true;
    }

}
