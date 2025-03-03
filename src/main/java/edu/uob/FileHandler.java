package edu.uob;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {

    public FileHandler() {
        // determine how to create instance of this class or if it's needed
    }

    public static void main(String args[]) throws IOException {
//        String filePath = File.separator + "Users" + File.separator + "areta_lee" + File.separator + "Desktop" + File.separator + "people.tab";
        String filePath = File.separator + "Users" + File.separator + "areta_lee" + File.separator + "Desktop" + File.separator + "sheds.tab";

//        readFile(filePath);
    }

    public static void readFile(File file, Table currentTable) throws IOException {

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // for buffered reader --> read all lines by checking if line is null

            String currentLine;
            int lineCounter = 1;

            while((currentLine = bufferedReader.readLine()) != null) {
                String[] splitLine = currentLine.split("\t");
                if(lineCounter == 1) {
                    for(String item : splitLine) {
                        currentTable.accessColumnHeaders().add(item);
                    }
                } else {
                    ArrayList<String> thisRow = new ArrayList<>(Arrays.asList(splitLine));
                    currentTable.accessTable().add(thisRow);
                }
                lineCounter++;
            }

            bufferedReader.close();

        } catch (FileNotFoundException e){
            throw new FileNotFoundException(file.getName());
        } catch (IOException e) {
            throw new IOException(file.getName());
        }

    }

    // should file parser be in here? or new class

    public static void writeTableToFile(File chosenFile, Table currentTable) throws IOException {

        try {
            FileWriter fileWriter = new FileWriter(chosenFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // for buffered reader --> read all lines by checking if line is null

            List<List<String>> tableContent = currentTable.accessTable();
            for (List<String> row : tableContent) {
                bufferedWriter.write(String.join("\t", row));
                bufferedWriter.newLine();
            }

            bufferedWriter.close();

        } catch (FileNotFoundException e){
            throw new FileNotFoundException(chosenFile.getName());
        } catch (IOException e) {
            throw new IOException(chosenFile.getName());
        }

        // remember to add in try-catch block
    }

//    public static void readFile(String fileName) throws IOException {
//
//        try {
//            File tableFile = new File(fileName);
//            FileReader fileReader = new FileReader(tableFile);
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            // for buffered reader --> read all lines by checking if line is null
//
//            String currentLine;
//            while((currentLine = bufferedReader.readLine()) != null) {
//                System.out.println(currentLine);
//            }
//
//            bufferedReader.close();
//
//        } catch (FileNotFoundException e){
//            throw new FileNotFoundException(fileName);
//        } catch (IOException e) {
//            throw new IOException(fileName);
//        }
//
//    }

//    public static void writeTableToFile(String fileName, Table currentTable) throws IOException {
//
//        try {
//            File tableFile = new File(fileName);
//            FileWriter fileWriter = new FileWriter(tableFile);
//            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//            // for buffered reader --> read all lines by checking if line is null
//
//            List<List<String>> tableContent = currentTable.accessTable();
//            for (List<String> row : tableContent) {
//                bufferedWriter.write(String.join("\t", row));
//                bufferedWriter.newLine();
//            }
//
//            bufferedWriter.close();
//
//        } catch (FileNotFoundException e){
//            throw new FileNotFoundException(fileName);
//        } catch (IOException e) {
//            throw new IOException(fileName);
//        }
//
//        // remember to add in try-catch block
//    }




}
