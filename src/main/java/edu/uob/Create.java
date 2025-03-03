package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Create {

    private File newFile;

    public File createDatabase(String filePath, String fileName) throws IOException {
        // same as CREATE

        // include directory path to parent folder (databases)
        // names must be case-insensitive
        // create in filesystem

        // need to create new instance in other method when calling this

        newFile = new File(filePath + fileName.toLowerCase());
        if(!newFile.mkdir()) {
            throw new IOException("Could not create directory " + filePath);
        }

        return this.newFile;
    }

    public File createTable(String filePath, String fileName, List<String> columnHeaders) throws IOException {
        // same as CREATE

        // include directory path to parent folder (databases)
        // names must be case-insensitive
        // create in filesystem

        // need to create new instance in other method when calling this

        newFile = new File(filePath + fileName.toLowerCase() + ".tab");
        // add column headings to file

        return this.newFile;
    }

}
