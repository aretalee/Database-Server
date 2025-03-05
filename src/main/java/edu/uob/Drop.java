package edu.uob;

import java.io.*;
import java.nio.file.Paths;

public class Drop {

    public void dropFile(String filePath, DBServer server) throws IOException {

        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        if (file.isDirectory()) {
            file = clearDirectory(file, server);

            server.removeDatabase(file.getName());
        } else {
            server.removeTable(file.getName());
        }

        quickDrop(file);

    }

    public void quickDrop(File uneededFile) throws IOException {
        if (!uneededFile.delete()) {
            throw new IOException("Could not delete file " + uneededFile.getAbsolutePath());
        }
    }

    public File clearDirectory(File directory, DBServer server) throws IOException {
        File[] fileList = directory.listFiles();

        if (fileList == null) {
            throw new NullPointerException("allDatabases");
        }

        for (File f : fileList) {
            dropFile(f.getAbsolutePath(), server);
        }
        return directory;
    }

}
