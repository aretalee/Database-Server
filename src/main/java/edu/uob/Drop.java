package edu.uob;

import java.io.*;

public class Drop {

    public boolean dropFile(String filePath, QueryHandler handler) {

        File file = new File(filePath);
        if (!file.exists()) {
            handler.setErrorLine("The database/table does not exist");
            return false;
        }
        removeObject(file, handler);
        quickDrop(file, handler);
        return true;
    }

    public void removeObject(File file, QueryHandler handler) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                file = clearDirectory(file, fileList, handler);
                handler.removeDatabase(file.getName());
            }
        } else { handler.removeTable(file.getName(), handler.getCurrentDatabase()); }
    }

    public void quickDrop(File uneededFile, QueryHandler handler) {
        if (!uneededFile.delete()) {
            handler.setErrorLine("Could not delete file " + uneededFile.getName());
        }
    }

    public File clearDirectory(File directory, File[] fileList,  QueryHandler handler) {
        for (File f : fileList) {
            dropFile(f.getAbsolutePath(), handler);
        }
        return directory;
    }
}
