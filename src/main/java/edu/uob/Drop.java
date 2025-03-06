package edu.uob;

import java.io.*;
import java.nio.file.Paths;

public class Drop {

    public boolean dropFile(String filePath, DBServer server) {

        File file = new File(filePath);

        if (!file.exists()) {
            server.setErrorLine("The file " + file.getName() + " does not exist");
            return false;
        }
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();

            if (fileList != null) {
                file = clearDirectory(file, fileList, server);
                server.removeDatabase(file.getName());
            }
        } else { server.removeTable(file.getName()); }

        quickDrop(file, server);
        return true;
    }

    public void quickDrop(File uneededFile, DBServer server) {
        if (!uneededFile.delete()) {
            server.setErrorLine("Could not delete file " + uneededFile.getName());
        }
    }

    public File clearDirectory(File directory, File[] fileList,  DBServer server) {

        for (File f : fileList) {
            dropFile(f.getAbsolutePath(), server);
        }
        return directory;
    }

}
