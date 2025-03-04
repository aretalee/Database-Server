package edu.uob;

import java.io.*;
import java.nio.file.Paths;

public class Drop {

    public static void main(String args[]) throws IOException {
        Drop drop = new Drop();

        String folderPath = Paths.get("databases").toAbsolutePath().toString() + File.separator + "datatwo" + File.separator + "newtablefour.tab";
//        drop.dropFile(folderPath);

    }

    public void dropFile(String filePath, DBServer server) throws IOException {
//  public void dropFile(String directoryPath, File file) throws IOException {

        File file = new File(filePath);

        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        if(file.isDirectory()) {
            file = clearDirectory(file, server);
            server.removeDatabase(file.getName());
        } else {
            server.removeTable(file.getName());
        }

        quickDrop(file);

    }

    public void quickDrop(File uneededFile) throws IOException {
        if(!uneededFile.delete()) {
            throw new IOException("Could not delete file " + uneededFile.getAbsolutePath());
        }
    }

    public File clearDirectory(File directory, DBServer server) throws IOException {
        File[] fileList = directory.listFiles();

        if (fileList == null) {
            throw new NullPointerException("allDatabases");
        }
//        else if (fileList.length == 0) {
//            throw new IOException("databases is already empty");
//        }

        if (fileList.length > 0) {
            for (File f : fileList) {
                dropFile(f.getAbsolutePath(), server);
            }
        }
        return directory;
    }

}
