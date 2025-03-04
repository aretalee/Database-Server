package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Alter {

    public static void main(String args[]) throws IOException {

        String folderPath = Paths.get("databases").toAbsolutePath().toString() + File.separator + "datatwo";

        List<String> attributeList = new ArrayList<String>();
        attributeList.add("name");
        attributeList.add("description");
        attributeList.add("type");

        Create create = new Create();
        Table newTable = create.createTable(folderPath, "NewTableSix", attributeList);

        List<String> parameters = new ArrayList<String>();
        parameters.add("Bob");
        parameters.add("tired");
        parameters.add("Blonde");

        Insert insert = new Insert();
        insert.insertIntoTable(newTable, parameters);

        Alter alter = new Alter();
        alter.alterTable(newTable, "drop", "NAME");

        List<String> list = newTable.accessColumnHeaders();
        for (String header : list) {
            System.out.println(header);
        }

    }

    public void alterTable(Table chosenTable, String valueType, String chosenHeader) throws IOException {

        // best to separate out into smaller functions

        if(valueType.equalsIgnoreCase("add")) {

            chosenTable.accessColumnHeaders().add(chosenHeader);

            for(List<String> row : chosenTable.accessTable()) {
                row.add(null);
                // adding null parameter to each row for easy editing later
            }

        }

        if(valueType.equalsIgnoreCase("drop")) {
            int chosenIndex = ColumnIndexFinder.findColumnIndex(chosenTable, chosenHeader);
            chosenTable.accessColumnHeaders().remove(chosenIndex);

            if(chosenIndex != -1 && !chosenHeader.equalsIgnoreCase("id")) { // should change checking ID into error handling?
                for(List<String> row : chosenTable.accessTable()) {
                    row.remove(chosenIndex);
                }
            }

        }

        // save back to filesystem
        chosenTable.saveToFile(chosenTable.getTableFile());

    }

}
