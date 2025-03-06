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
    private boolean calledUseCommand = false;

    private List<Database> allDatabases = new ArrayList<Database>();
    private List<Table> allTables = new ArrayList<Table>();
    private String currentDatabase;

    private List<String> tableForPrinting = new ArrayList<String>();
    private boolean printTable = false;
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

            FileHandler fileHandler = new FileHandler();
            fileHandler.populateWithExistingFiles(this);
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

        QueryLexer queryLexer = new QueryLexer(command);
        queryLexer.setup();
        List<String> tokens = queryLexer.getTokens();

        if (!tokens.get(0).equalsIgnoreCase("use") && !tokens.get(0).equalsIgnoreCase("create")
                && !tokens.get(0).equalsIgnoreCase("database") && !calledUseCommand) {
            return "[ERROR]: Please call USE before attempting table-specific commands.";
        }

        QueryParser parser = new QueryParser();
        return returnStatement(parser.parseQuery(tokens, this));
    }

    public String returnStatement(boolean parserReturnValue) {
        String returnStatement = "";
        if (parserReturnValue) {
            // print [OK]
            returnStatement += "[OK]";
            // print table if needed
            if (tableForPrinting != null && printTable) {
                for (String row : tableForPrinting) {
                    returnStatement += row;
                }
                printTable = false;
            }
        } else {
            // print [Error] followed by error message
            returnStatement =  ("[ERROR]: " + errorLine);
        }
        return returnStatement;
    }

    public List<Table> getAllTables() {
        return this.allTables;
    }

    public List<Database> getAllDatabases() {
        return this.allDatabases;
    }

    public void addTable(Table table) {
        allTables.add(table);
    }

    public void removeTable(String tableName) {
        // should I make it null first?
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
           if (currentTable.getTableName().equalsIgnoreCase(tableName + ".tab")) {
               toBeReturned = currentTable;
           }
       }
        return toBeReturned;
    }

    public void setTableForPrinting(List<String> tableForPrinting) {
        this.tableForPrinting = tableForPrinting;
    }

    public void setPrintBoolean(boolean printTable) {
        this.printTable = printTable;
    }

    public void setErrorLine(String error) {
        this.errorLine = error;
    }

    public void setCalledUseCommand(boolean calledUseCommand) {
        this.calledUseCommand = calledUseCommand;
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
