package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Drop {

    public void dropFile(File file) throws IOException {
//  public void dropFile(String directoryPath, File file) throws IOException {

        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        if(file.isDirectory()) {
            file = clearDirectory(file);
        }

        quickDrop(file);

    }

    public void quickDrop(File uneededFile) throws IOException {
        if(!uneededFile.delete()) {
            throw new IOException("Could not delete file " + uneededFile.getAbsolutePath());
        }
    }

    public File clearDirectory(File emptyDirectory) throws IOException {
        File[] fileList = emptyDirectory.listFiles();

        if (fileList == null) {
            throw new NullPointerException("allDatabases");
        } else if (fileList.length == 0) {
            throw new IOException("databases is empty");
        }

        for (File f : fileList) {
            dropFile(f);
        }
        return emptyDirectory;
    }

}
