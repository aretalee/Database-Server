package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;

    private List<Database> allDatabases = new ArrayList<Database>();
    private List<Table> allTables = new ArrayList<Table>();
    private String currentDatabase;

    private List<String> tableForPrinting = new ArrayList<String>();
    private String errorLine;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);

    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) throws IOException {
        // TODO implement your server logic here

        // receive query -> turn into string
        // tokenise + parse query
        // call corresponding operation
        // give response [OK]/[ERROR]
        // print table content in here (if needed)

        QueryLexer queryLexer = new QueryLexer(command);
        queryLexer.setup();
        List<String> tokens = queryLexer.getTokens();

        QueryParser parser = new QueryParser();
        return returnStatement(parser.parseQuery(tokens, this));


//        return "";
    }

    public String returnStatement(boolean parserReturnValue) {
        String returnStatement = "";
        if (parserReturnValue) {
            // is this allowed
            // print [OK]
            returnStatement += "[OK]";
            // print table if needed
            if (tableForPrinting != null) {
                for (String row : tableForPrinting) {
                    //each row needs to have been converted in respective methods
//                    System.out.println(row);
                    returnStatement += row;
                }
            }

        } else {
            // print [Error] followed by error message
//            System.out.println("[ERROR]: " + errorLine);
            returnStatement =  ("[ERROR]: " + errorLine);
        }
        return returnStatement;
    }

    public void addTable(Table table) {
        allTables.add(table);
    }

    public void removeTable(String tableName) {
        allTables.removeIf(table -> table.getTableName().equals(tableName));
    }

    public void addDatabase(Database database) {
        allDatabases.add(database);
    }

    public void removeDatabase(String databaseName) {
        allDatabases.removeIf(database -> database.getDatabaseName().equals(databaseName));
    }

    public String getStorageFolderPath() {
        return this.storageFolderPath;
    }

    public String getCurrentDatabase() {
        return this.currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    public Table getTable(String tableName) {
       Table toBeReturned = null;

        for (Table currentTable: this.allTables) {
           if (currentTable.getTableName().equals(tableName + ".tab")) {
               toBeReturned = currentTable;
           }
       }
        return toBeReturned;
    }

    public void setErrorLine(String error) {
        this.errorLine = error;
    }

    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
