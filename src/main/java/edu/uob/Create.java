package edu.uob;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Create {

    private File newFile;

    public static void main(String args[]) throws IOException {
        String folderPath = Paths.get("databases").toAbsolutePath().toString();

        Create create = new Create();
        File newDatabase = create.createDatabase(folderPath, "DataOne");

//        String folderPath = Paths.get("databases").toAbsolutePath().toString() + File.separator + "datatwo";
//
//        List<String> attributeList = new ArrayList<String>();
//        attributeList.add("name");
//        attributeList.add("description");
//        attributeList.add("type");
//
//        Create create = new Create();
//        Table newTable = create.createTable(folderPath, "NewTableFour", attributeList);
//        System.out.println(newTable.getTableName());
//
//        List<String> list = newTable.accessColumnHeaders();
//        for (String header : list) {
//            System.out.println(header);
//        }
    }


    public File createDatabase(String filePath, String fileName) throws IOException {
        // same as CREATE

        // include directory path to parent folder (databases)
        // names must be case-insensitive
        // create in filesystem

        // need to create new instance in other method when calling this

        // change into try-catch block later!!!!!!

        newFile = new File(filePath + File.separator + fileName.toLowerCase());
        if (newFile.exists()) {
            throw new IOException("Directory already exists " + filePath);
        }
        else if (!newFile.mkdir()) {
            throw new IOException("Could not create directory " + filePath);
        }

        return newFile;
    }

    public Table createTable(String filePath, String fileName, List<String> attributeList) throws IOException {
        // same as CREATE

        // make sure database exists!!!! (error handling)

        // include directory path to parent folder (databases)
        // names must be case-insensitive
        // create in filesystem

        // need to create new instance in other method when calling this

        newFile = new File(filePath + File.separator + fileName.toLowerCase() + ".tab");
        if (newFile.exists()) {
            throw new IOException("Table already exists " + filePath);
        }
        else if (!newFile.createNewFile()) {
            throw new IOException("Could not create table " + filePath);
        }
        // add column headings to file
        Table newTable = new Table(newFile, attributeList);
        newTable.saveToFile(newFile);

        return newTable;
    }

}
