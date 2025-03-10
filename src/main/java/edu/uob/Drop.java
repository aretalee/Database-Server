package edu.uob;

import java.io.*;

public class Drop {

    public boolean dropFile(String filePath, QueryHandler queryHandler) {

        File file = new File(filePath);

        if (!file.exists()) {
            queryHandler.setErrorLine("The database/table does not exist");
            return false;
        }
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                file = clearDirectory(file, fileList, queryHandler);
                queryHandler.removeDatabase(file.getName());
            }
        } else { queryHandler.removeTable(file.getName(), queryHandler.getCurrentDatabase()); }

        quickDrop(file, queryHandler);
        return true;
    }

    public void quickDrop(File uneededFile, QueryHandler queryHandler) {
        if (!uneededFile.delete()) {
            queryHandler.setErrorLine("Could not delete file " + uneededFile.getName());
        }
    }

    public File clearDirectory(File directory, File[] fileList,  QueryHandler queryHandler) {

        for (File f : fileList) {
            dropFile(f.getAbsolutePath(), queryHandler);
        }
        return directory;
    }

}
