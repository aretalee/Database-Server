package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class QueryHandler {

    private DBServer currentServer;
    private boolean populatedWithExistingFiles;

    private boolean calledUseCommand = false;
    private List<Database> allDatabases = new ArrayList<Database>();
    private List<Table> allTables = new ArrayList<Table>();
    private String currentDatabase;

    private List<String> tableForPrinting = new ArrayList<String>();
    private boolean printTable = false;
    private String errorLine;

    public QueryHandler(DBServer server) {
        currentServer = server;
        FileHandler fileHandler = new FileHandler();
        populatedWithExistingFiles = fileHandler.populateWithExistingFiles(this);
    }

    public String returnResponse(String command) {
        QueryLexer queryLexer = new QueryLexer(command);
        queryLexer.setup();
        List<String> tokens = queryLexer.getTokens();

        if (command.isEmpty()) {
            return "[ERROR]: No command specified.";
        } else if (!populatedWithExistingFiles) {
            return "[ERROR]: Unable to populate files.";
        } else if (!tokens.get(0).equalsIgnoreCase("use")
                && ((tokens.get(0).equalsIgnoreCase("create")
                || tokens.get(0).equalsIgnoreCase("drop"))
                && !tokens.get(1).equalsIgnoreCase("database")) && !calledUseCommand) {
            return "[ERROR]: Please call USE before attempting table-specific commands.";
        }
        QueryParser parser = new QueryParser();
        return returnStatement(parser.parseQuery(tokens, this));
    }

    public String returnStatement(boolean parserReturnValue) {
        StringBuilder returnStatement = new StringBuilder();
        if (parserReturnValue) {
            returnStatement.append("[OK]");
            if (tableForPrinting != null && printTable) {
                for (String row : tableForPrinting) {
                    returnStatement.append(row);
                }
                printTable = false;
            }
        } else {
            returnStatement = new StringBuilder(("[ERROR]: " + errorLine));
            errorLine = null;
        }
        return returnStatement.toString();
    }

    public String getErrorLine() {
        return errorLine;
    }

    public DBServer getCurrentServer() {
        return currentServer;
    }

    public List<Database> getAllDatabases() {
        return this.allDatabases;
    }

    public void addTable(Table table) {
        allTables.add(table);
    }

    public void removeTable(String tableName, String databaseName) {
        // should I make it null first?
        allTables.removeIf(table -> table.getWhichDatabase().equalsIgnoreCase(databaseName)
                && table.getTableName().equals(tableName));
    }

    public void addDatabase(Database database) {
        allDatabases.add(database);
    }

    public void removeDatabase(String databaseName) {
        allDatabases.removeIf(database -> database.getDatabaseName().equals(databaseName));
    }

    public String getCurrentDatabase() {
        return this.currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    public Table getTable(String tableName, String databaseName) {
        Table toBeReturned = null;

        for (Table currentTable: this.allTables) {
            if (currentTable.getWhichDatabase().equalsIgnoreCase(databaseName)
                    && currentTable.getTableName().equalsIgnoreCase(tableName + ".tab")) {
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

    public int findColumnIndex(Table chosenTable, String chosenHeader) {
        int chosenIndex = -1;

        List<String> headerList = chosenTable.accessColumnHeaders();
        for(String header : headerList) {
            if((header).equalsIgnoreCase(chosenHeader)) {
                chosenIndex = headerList.indexOf(header);
            }
        }
        return chosenIndex;
    }

}
