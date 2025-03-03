package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Insert {

    public static void main(String args[]) throws IOException {

        String folderPath = Paths.get("databases").toAbsolutePath().toString() + File.separator + "datatwo";

        List<String> attributeList = new ArrayList<String>();
        attributeList.add("name");
        attributeList.add("description");
        attributeList.add("type");

        Create create = new Create();
        Table newTable = create.createTable(folderPath, "NewTableFive", attributeList);

        List<String> valueParameters = new ArrayList<String>();
        valueParameters.add("Bob");
        valueParameters.add("tired");
        valueParameters.add("Blonde");

        Insert insert = new Insert();
        insert.insertIntoTable(newTable, valueParameters);

        List<List<String>> list = newTable.accessTable();
        for (List<String> row : list) {
            System.out.println(row);
        }

    }

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
